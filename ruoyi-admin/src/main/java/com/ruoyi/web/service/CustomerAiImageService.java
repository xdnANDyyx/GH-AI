package com.ruoyi.web.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.google.auth.oauth2.GoogleCredentials;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * AI 图片生成服务
 * 统一使用 Vertex AI gemini-3-pro-image 模型
 */
@Service
public class CustomerAiImageService {

    private static final Logger log = LoggerFactory.getLogger(CustomerAiImageService.class);

    // ============================================
    // Vertex AI 配置
    // ============================================
    @Value("${vertex.ai.project-id:}")
    private String vertexProjectId;

    @Value("${vertex.ai.location:global}")
    private String vertexLocation;

    @Value("${vertex.ai.model:gemini-3-pro-image}")
    private String vertexModel;

    @Value("${vertex.ai.read-timeout:120}")
    private int vertexReadTimeout;

    @Value("${vertex.ai.credentials-path:}")
    private String credentialsPath;

    @Value("${vertex.ai.default-aspect-ratio:1:1}")
    private String defaultAspectRatio;

    @Value("${vertex.ai.default-image-size:1K}")
    private String defaultImageSize;

    // ============================================
    // 代理配置（仅 Vertex AI 生图接口走新加坡 Squid）
    // ============================================
    @Value("${vertex.ai.proxy.host:}")
    private String proxyHost;

    @Value("${vertex.ai.proxy.port:0}")
    private int proxyPort;

    // 保留旧配置用于降级（可选）
    @Value("${spring.ai.openai302.base-url:}")
    private String openAi302BaseUrl;

    @Value("${spring.ai.openai302.api-key:}")
    private String openAi302ApiKey;

    @Value("${spring.ai.openai302.model:gpt-image-2}")
    private String openAi302Model;

    @Value("${spring.ai.lightcc.base-url:}")
    private String lightccImageBaseUrl;

    @Value("${spring.ai.lightcc.api-key:}")
    private String lightccImageApiKey;

    @Value("${spring.ai.lightcc.model:nano-banana-2}")
    private String lightccImageModel;

    @Value("${OPENAI_BASE_URL:}")
    private String openAiEnvBaseUrl;

    @Value("${OPENAI_API_KEY:}")
    private String openAiEnvApiKey;

    // 下载图片用的 HttpClient（不走代理）
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(60))
            .build();

    // Vertex AI 生图请求用的 HttpClient（走代理）
    private HttpClient vertexHttpClient;

    private synchronized HttpClient getVertexHttpClient() {
        if (vertexHttpClient == null) {
            if (proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
                log.info("🔁 Vertex AI 图片生成接口配置代理: {}:{}", proxyHost, proxyPort);
                vertexHttpClient = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(60))
                        .proxy(java.net.ProxySelector.of(
                                new java.net.InetSocketAddress(proxyHost, proxyPort)))
                        .build();
            } else {
                log.info("🔁 Vertex AI 图片生成接口不使用代理");
                vertexHttpClient = httpClient;
            }
        }
        return vertexHttpClient;
    }

    // ============================================
    // Vertex AI 核心调用
    // ============================================

    /**
     * 获取 Vertex AI Bearer token
     * 优先级：
     * 1. 配置项 vertex.ai.credentials-path 指定的文件
     * 2. 环境变量 GOOGLE_APPLICATION_CREDENTIALS
     * 3. GoogleCredentials.getApplicationDefault() 默认路径查找
     * GoogleCredentials 内部使用 HttpURLConnection，需要设置系统属性走代理
     */
    private String getVertexAccessToken() {
        String oldHttpsProxyHost = System.getProperty("https.proxyHost");
        String oldHttpsProxyPort = System.getProperty("https.proxyPort");

        try {
            if (proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
                System.setProperty("https.proxyHost", proxyHost);
                System.setProperty("https.proxyPort", String.valueOf(proxyPort));
            }

            GoogleCredentials credentials;

            // 1. 优先使用配置项指定的凭证文件
            if (credentialsPath != null && !credentialsPath.isEmpty()) {
                File credFile = new File(credentialsPath);
                if (credFile.exists()) {
                    log.info("从配置的凭证文件加载 credentials: {}", credentialsPath);
                    try (FileInputStream fis = new FileInputStream(credFile)) {
                        credentials = GoogleCredentials.fromStream(fis)
                                .createScoped("https://www.googleapis.com/auth/cloud-platform");
                    }
                } else {
                    log.warn("配置的凭证文件不存在: {}，回退到默认方式", credentialsPath);
                    credentials = GoogleCredentials.getApplicationDefault()
                            .createScoped("https://www.googleapis.com/auth/cloud-platform");
                }
            } else {
                // 2. 没有显式配置，走环境变量或默认路径
                log.info("未配置 credentials-path，使用默认方式查找 ADC");
                credentials = GoogleCredentials.getApplicationDefault()
                        .createScoped("https://www.googleapis.com/auth/cloud-platform");
            }

            credentials.refreshIfExpired();
            String token = credentials.getAccessToken().getTokenValue();
            log.info("Vertex AI access token 获取成功, 前缀: {}...", token.substring(0, Math.min(10, token.length())));
            return token;
        } catch (Exception e) {
            log.error("获取 Vertex AI access token 失败", e);
            throw new RuntimeException("Vertex AI 认证失败: " + e.getMessage(), e);
        } finally {
            // 恢复原来的代理设置，不影响其他请求
            if (oldHttpsProxyHost != null) {
                System.setProperty("https.proxyHost", oldHttpsProxyHost);
            } else {
                System.clearProperty("https.proxyHost");
            }
            if (oldHttpsProxyPort != null) {
                System.setProperty("https.proxyPort", oldHttpsProxyPort);
            } else {
                System.clearProperty("https.proxyPort");
            }
        }
    }

    /**
     * 调用 Vertex AI Gemini 生成图片（使用默认宽高比与尺寸）
     * @param prompt 文本提示词
     * @param imageUrls 参考图片 URL 列表（可为 null 或空）
     * @return 生成的图片 data URI 列表（形如 data:image/png;base64,...，可直接用作 img src）
     */
    private List<String> callVertexAi(String prompt, List<String> imageUrls) {
        return callVertexAi(prompt, imageUrls, defaultAspectRatio, defaultImageSize);
    }

    /**
     * 调用 Vertex AI Gemini 生成图片
     * @param prompt 文本提示词
     * @param imageUrls 参考图片 URL 列表（可为 null 或空）
     * @param aspectRatio 宽高比，如 "1:1"、"16:9"、"9:16" 等
     * @param imageSize 图片尺寸，如 "1K"、"2K" 等
     * @return 生成的图片 data URI 列表（形如 data:image/png;base64,...，可直接用作 img src）
     */
    private List<String> callVertexAi(String prompt, List<String> imageUrls, String aspectRatio, String imageSize) {
        try {
            String accessToken = getVertexAccessToken();

            // 构建 Vertex AI Gemini API URL
            // 注意：
            // 1. gemini-3-pro-image (Nano Banana Pro) 仅支持 v1beta1 API 端点，v1 会返回 404
            // 2. location=global 时主机名不带区域前缀（aiplatform.googleapis.com），
            //    否则会拼出 global-aiplatform.googleapis.com 这个不存在的域名导致 404
            String url = buildVertexAiUrl();

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            content.put("role", "user");

            JSONArray parts = new JSONArray();

            // 添加文本 prompt
            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);
            parts.add(textPart);

            // 添加参考图片（如果有）
            if (imageUrls != null && !imageUrls.isEmpty()) {
                for (String imageUrl : imageUrls) {
                    byte[] imageBytes = downloadImage(imageUrl);
                    String base64 = Base64.getEncoder().encodeToString(imageBytes);
                    String mimeType = detectMimeType(imageBytes);

                    JSONObject imagePart = new JSONObject();
                    JSONObject inlineData = new JSONObject();
                    inlineData.put("mimeType", mimeType);
                    inlineData.put("data", base64);
                    imagePart.put("inlineData", inlineData);
                    parts.add(imagePart);
                }
            }

            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            // 生图配置
            JSONObject generationConfig = new JSONObject();
            JSONArray responseModalities = new JSONArray();
            responseModalities.add("TEXT");
            responseModalities.add("IMAGE");
            generationConfig.put("responseModalities", responseModalities);

            // imageConfig 配置（参考官方 Python SDK：genai.Client(vertexai=True) 的 GenerateContentConfig.image_config）
            JSONObject imageConfig = new JSONObject();
            if (aspectRatio != null && !aspectRatio.isEmpty()) {
                imageConfig.put("aspectRatio", aspectRatio);
            }
            if (imageSize != null && !imageSize.isEmpty()) {
                imageConfig.put("imageSize", imageSize);
            }
            generationConfig.put("imageConfig", imageConfig);

            requestBody.put("generationConfig", generationConfig);

            String jsonBody = requestBody.toJSONString();
            log.info("调用 Vertex AI, prompt 长度: {}, 参考图片数: {}, aspectRatio: {}, imageSize: {}",
                    prompt.length(), imageUrls != null ? imageUrls.size() : 0, aspectRatio, imageSize);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(vertexReadTimeout))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = getVertexHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            log.info("Vertex AI 响应状态: {}, body 长度: {}", response.statusCode(),
                    response.body() != null ? response.body().length() : 0);

            if (response.statusCode() != 200) {
                String responseBody = response.body();
                String errorDetail = "";
                if (responseBody != null && !responseBody.isEmpty()) {
                    // 尝试提取 Google API 标准错误格式中的 message，便于全链路排查
                    try {
                        JSONObject respJson = JSON.parseObject(responseBody);
                        JSONObject error = respJson.getJSONObject("error");
                        if (error != null && error.getString("message") != null) {
                            errorDetail = ", message: " + error.getString("message");
                        }
                    } catch (Exception ignore) {
                        errorDetail = ", body: " + (responseBody.length() > 500 ? responseBody.substring(0, 500) : responseBody);
                    }
                }
                log.error("Vertex AI 调用失败, 状态码: {}, URL: {}, 响应: {}", response.statusCode(), url, responseBody);
                throw new RuntimeException("Vertex AI 调用失败: HTTP " + response.statusCode() + errorDetail);
            }

            // 解析响应，提取图片
            return extractImagesFromResponse(response.body());

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Vertex AI 调用异常", e);
            throw new RuntimeException("Vertex AI 调用异常: " + e.getMessage(), e);
        }
    }

    /**
     * 从 Vertex AI 响应中提取图片列表
     * 返回 data URI（data:image/xxx;base64,...），前端可直接用作 img src / fetch 下载
     */
    private List<String> extractImagesFromResponse(String responseBody) {
        List<String> dataUriImages = new ArrayList<>();
        try {
            JSONObject respJson = JSON.parseObject(responseBody);
            // 诊断日志：记录响应顶层结构，便于排查 body 大小与提取数据不符的问题
            log.info("Vertex AI 响应顶层字段: {}", respJson.keySet());
            JSONArray candidates = respJson.getJSONArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
                log.warn("Vertex AI 响应中没有 candidates");
                return dataUriImages;
            }

            for (int i = 0; i < candidates.size(); i++) {
                JSONObject candidate = candidates.getJSONObject(i);
                JSONObject content = candidate.getJSONObject("content");
                if (content == null) continue;

                JSONArray parts = content.getJSONArray("parts");
                if (parts == null) continue;

                for (int j = 0; j < parts.size(); j++) {
                    JSONObject part = parts.getJSONObject(j);
                    // 文本说明
                    if (part.containsKey("text") && part.getString("text") != null) {
                        log.info("Vertex AI 文本说明: {}", part.getString("text"));
                    }
                    // 图片
                    if (part.containsKey("inlineData")) {
                        JSONObject inlineData = part.getJSONObject("inlineData");
                        String data = inlineData.getString("data");
                        String mimeType = inlineData.getString("mimeType");
                        if (data == null || data.isEmpty()) {
                            continue;
                        }

                        // 关键修复：不能把裸 base64 直接返回给前端！
                        // 前端会把图片字符串直接用作 <img src> 或 fetch 下载，
                        // 裸 base64 没有 data: 前缀时浏览器会当成相对路径 URL，
                        // 请求到 404/错误页，表现为"图片已破损打不开"。
                        // 这里解码校验后包装成标准 data URI 再返回。
                        byte[] imageBytes;
                        try {
                            // MIME 解码器容忍 base64 中的换行/空白字符
                            imageBytes = Base64.getMimeDecoder().decode(data);
                        } catch (IllegalArgumentException de) {
                            log.error("Vertex AI 返回的 base64 无法解码, 长度: {}", data.length());
                            continue;
                        }
                        try {
                            // 魔数校验：确认拿到的是完整图片字节（可及时发现截断/分片问题）
                            validateImageBytes(imageBytes, "vertex-ai-response");
                        } catch (RuntimeException ve) {
                            log.error("Vertex AI 返回图片字节校验失败: {}", ve.getMessage());
                            continue;
                        }
                        String realMime = detectMimeType(imageBytes);
                        if (mimeType != null && !mimeType.isEmpty() && !mimeType.equals(realMime)) {
                            log.warn("Vertex AI 声明 mimeType: {} 与文件头实际类型不符, 实际: {}", mimeType, realMime);
                        }

                        dataUriImages.add("data:" + realMime + ";base64," + data);
                        log.info("提取到图片, 声明mimeType: {}, 实际mimeType: {}, base64长度: {}, 解码字节数: {}",
                                mimeType, realMime, data.length(), imageBytes.length);
                    }
                }
            }

            log.info("从 Vertex AI 响应中共提取 {} 张图片", dataUriImages.size());
            return dataUriImages;

        } catch (Exception e) {
            log.error("解析 Vertex AI 响应失败: {}", e.getMessage(), e);
            throw new RuntimeException("解析 Vertex AI 响应失败: " + e.getMessage(), e);
        }
    }

    // ============================================
    // 公开方法（保持与原有接口兼容）
    // ============================================

    /**
     * 反推提示词：发送图片+文本提示给 Gemini，返回文本描述
     * @param imageBase64OrUrl 图片 base64 data URI 或 URL
     * @param prompt 文本提示（用户可编辑的默认提示词）
     * @return AI 生成的提示词文本
     */
    public String reversePrompt(String imageBase64OrUrl, String prompt) {
        try {
            String accessToken = getVertexAccessToken();
            String url = buildVertexAiUrl();

            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            content.put("role", "user");

            JSONArray parts = new JSONArray();

            // 添加文本 prompt
            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt != null && !prompt.isEmpty() ? prompt : "请描述这张图片的提示词");
            parts.add(textPart);

            // 添加图片
            if (imageBase64OrUrl != null && !imageBase64OrUrl.isEmpty()) {
                byte[] imageBytes;
                String mimeType;
                if (imageBase64OrUrl.startsWith("data:")) {
                    // data URI 格式：data:image/png;base64,xxxx
                    int commaIdx = imageBase64OrUrl.indexOf(",");
                    String meta = imageBase64OrUrl.substring(5, commaIdx);
                    mimeType = meta.split(";")[0];
                    String base64Data = imageBase64OrUrl.substring(commaIdx + 1);
                    imageBytes = Base64.getMimeDecoder().decode(base64Data);
                } else {
                    // URL 格式
                    imageBytes = downloadImage(imageBase64OrUrl);
                    mimeType = detectMimeType(imageBytes);
                }
                String base64 = Base64.getEncoder().encodeToString(imageBytes);
                JSONObject imagePart = new JSONObject();
                JSONObject inlineData = new JSONObject();
                inlineData.put("mimeType", mimeType);
                inlineData.put("data", base64);
                imagePart.put("inlineData", inlineData);
                parts.add(imagePart);
            }

            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            // 文本生成配置（不需要 IMAGE）
            JSONObject generationConfig = new JSONObject();
            JSONArray responseModalities = new JSONArray();
            responseModalities.add("TEXT");
            generationConfig.put("responseModalities", responseModalities);
            requestBody.put("generationConfig", generationConfig);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(vertexReadTimeout))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toJSONString(), java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = getVertexHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("反推提示词 Vertex AI 调用失败, 状态码: {}", response.statusCode());
                throw new RuntimeException("AI 分析失败: HTTP " + response.statusCode());
            }

            // 提取文本
            JSONObject respJson = JSON.parseObject(response.body());
            JSONArray candidates = respJson.getJSONArray("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                JSONObject candidate = candidates.getJSONObject(0);
                JSONObject respContent = candidate.getJSONObject("content");
                if (respContent != null) {
                    JSONArray respParts = respContent.getJSONArray("parts");
                    if (respParts != null) {
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < respParts.size(); j++) {
                            JSONObject part = respParts.getJSONObject(j);
                            if (part.containsKey("text") && part.getString("text") != null) {
                                sb.append(part.getString("text"));
                            }
                        }
                        if (sb.length() > 0) return sb.toString();
                    }
                }
            }
            return "AI 分析完成，但未返回文本结果";
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("反推提示词调用异常", e);
            throw new RuntimeException("反推提示词调用异常: " + e.getMessage(), e);
        }
    }

    /**
     * 基础生图
     */
    public List<String> generateImage(String prompt) {
        log.info("开始 Vertex AI 基础生图, prompt 长度: {}", prompt.length());
        List<String> base64Images = callVertexAi(prompt, null);
        log.info("Vertex AI 基础生图完成, 生成 {} 张图片", base64Images.size());
        return base64Images;
    }

    /**
     * 白底图生成：上传的商品图去掉背景,放在纯白背景上
     * prompt 约定与 lightcc 类似：Remove the background of the product and place it on a pure white background
     */
    public List<String> generateWhiteBgImage(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            throw new RuntimeException("原图不能为空");
        }
        log.info("开始 Vertex AI 白底图生成, 原图数: {}", imageUrls.size());

        String prompt = "Remove the background of the product in the image and place it on a pure white background (#FFFFFF). Maintain the original product details, shadows, and reflections. Output a clean product photo on white background. Do not change the product itself.";
        List<String> base64Images = callVertexAi(prompt, imageUrls);
        log.info("Vertex AI 白底图生成完成, 生成 {} 张图片", base64Images.size());
        return base64Images;
    }

    /**
     * 场景图 / 背景图生成
     */
    public List<String> generateBackgroundImage(List<String> referenceImageUrls, String sceneDescription,
            String stylePrompt) {
        log.info("开始 Vertex AI 背景图生成, 参考图数: {}, 场景: {}", 
                referenceImageUrls != null ? referenceImageUrls.size() : 0, sceneDescription);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Generate a product scene image. ");
        if (sceneDescription != null && !sceneDescription.isEmpty()) {
            promptBuilder.append("Place the product in this scene: ").append(sceneDescription).append(". ");
        }
        if (stylePrompt != null && !stylePrompt.isEmpty()) {
            promptBuilder.append("Style: ").append(stylePrompt).append(". ");
        }
        promptBuilder.append("Keep the product looking natural and photorealistic. Generate a high-quality e-commerce product image.");

        List<String> base64Images = callVertexAi(promptBuilder.toString(), referenceImageUrls);
        log.info("Vertex AI 背景图生成完成, 生成 {} 张图片", base64Images.size());
        return base64Images;
    }

    /**
     * 参考图生图
     */
    public List<String> generateWithReferenceImages(String prompt, List<String> referenceImageUrls) {
        log.info("开始 Vertex AI 参考图生图, 参考图数: {}", referenceImageUrls != null ? referenceImageUrls.size() : 0);
        List<String> base64Images = callVertexAi(prompt, referenceImageUrls);
        log.info("Vertex AI 参考图生图完成, 生成 {} 张图片", base64Images.size());
        return base64Images;
    }

    /**
     * 详情页生成
     */
    public List<String> generateDetailImage(List<String> referenceImageUrls, String styleDescription,
            String detailRequirements) {
        log.info("开始 Vertex AI 详情页生成, 参考图数: {}",
                referenceImageUrls != null ? referenceImageUrls.size() : 0);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Generate an e-commerce product detail page image. ");
        if (styleDescription != null && !styleDescription.isEmpty()) {
            promptBuilder.append("Design style: ").append(styleDescription).append(". ");
        }
        if (detailRequirements != null && !detailRequirements.isEmpty()) {
            promptBuilder.append("Requirements: ").append(detailRequirements).append(". ");
        }
        promptBuilder.append("Create a professional, clean, detailed showcase suitable for e-commerce listing.");

        List<String> base64Images = callVertexAi(promptBuilder.toString(), referenceImageUrls);
        log.info("Vertex AI 详情页生成完成, 生成 {} 张图片", base64Images.size());
        return base64Images;
    }

    /**
     * 产品精修图生成
     */
    public List<String> generateRetouchImage(List<String> imageUrls, String retouchRequirements) {
        log.info("开始 Vertex AI 精修图生成, 原图数: {}", imageUrls != null ? imageUrls.size() : 0);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Professional product photo retouching. Enhance the product image with perfect lighting, clean details, and professional e-commerce quality. ");
        if (retouchRequirements != null && !retouchRequirements.isEmpty()) {
            promptBuilder.append("Specific requirements: ").append(retouchRequirements).append(". ");
        }
        promptBuilder.append("Do not change the product itself, only enhance photo quality.");

        List<String> base64Images = callVertexAi(promptBuilder.toString(), imageUrls);
        log.info("Vertex AI 精修图生成完成, 生成 {} 张图片", base64Images.size());
        return base64Images;
    }

    /**
     * AI 模特图生成
     */
    public List<String> generateAiModelImage(List<String> productImageUrls, String modelDescription,
            String poseDescription) {
        log.info("开始 Vertex AI 模特图生成, 商品图数: {}",
                productImageUrls != null ? productImageUrls.size() : 0);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Create a fashion e-commerce model photo. Show a model wearing/using the product naturally. ");
        if (modelDescription != null && !modelDescription.isEmpty()) {
            promptBuilder.append("Model description: ").append(modelDescription).append(". ");
        }
        if (poseDescription != null && !poseDescription.isEmpty()) {
            promptBuilder.append("Pose and setting: ").append(poseDescription).append(". ");
        }
        promptBuilder.append("Professional fashion photography style, high quality, suitable for e-commerce listing.");

        List<String> base64Images = callVertexAi(promptBuilder.toString(), productImageUrls);
        log.info("Vertex AI 模特图生成完成, 生成 {} 张图片", base64Images.size());
        return base64Images;
    }

    /**
     * 尺寸标记图生成
     */
    public List<String> generateSizeMarkImage(List<String> imageUrls, String sizeInfo) {
        log.info("开始 Vertex AI 尺寸标记图生成, 原图数: {}", imageUrls != null ? imageUrls.size() : 0);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Create a product dimension/size guide image. Clearly show the product with measurement lines and size labels overlay. ");
        if (sizeInfo != null && !sizeInfo.isEmpty()) {
            promptBuilder.append("Size details: ").append(sizeInfo).append(". ");
        }
        promptBuilder.append("Clean, professional product dimension visualization.");

        List<String> base64Images = callVertexAi(promptBuilder.toString(), imageUrls);
        log.info("Vertex AI 尺寸标记图生成完成, 生成 {} 张图片", base64Images.size());
        return base64Images;
    }

    // ============================================
    // 兼容旧接口的方法（Controller 层调用）
    // ============================================

    /**
     * 调用 GPT 生图（兼容旧接口，实际走 Vertex AI）
     */
    public List<String> generateGptImage(String prompt, List<String> imageUrls) {
        return generateWithReferenceImages(prompt, imageUrls);
    }

    /**
     * 调用 LightCC 生图（兼容旧接口，实际走 Vertex AI）
     */
    public List<String> generateLightCCImage(String prompt, List<String> imageUrls) {
        return generateWithReferenceImages(prompt, imageUrls);
    }

    /**
     * 统一生成入口（兼容 Controller 层调用）
     * 从 params 中读取前端传参：
     * - productImages / referenceImages / imageUrls：参考图片 URL 列表
     * - extraOptions.aspect_ratio：宽高比（如 "1:1"、"16:9"、"9:16"）
     * - extraOptions.image_size / size：图片尺寸（如 "1K"、"2K"）
     * - n：生成数量
     */
    public List<String> generateImages(String prompt, int n, String size, Map<String, Object> params) {
        // 1. 收集参考图片
        List<String> imageUrls = new ArrayList<>();
        if (params != null) {
            List<String> productImages = castStringList(params.get("productImages"));
            List<String> referenceImages = castStringList(params.get("referenceImages"));
            List<String> legacyImageUrls = castStringList(params.get("imageUrls"));
            if (productImages != null) imageUrls.addAll(productImages);
            if (referenceImages != null) imageUrls.addAll(referenceImages);
            if (legacyImageUrls != null) imageUrls.addAll(legacyImageUrls);
        }

        // 2. 解析宽高比与图片尺寸（非法值回退默认值，避免 Vertex AI 400 INVALID_ARGUMENT）
        String aspectRatio = sanitizeAspectRatio(defaultAspectRatio);
        String imageSize = sanitizeImageSize(defaultImageSize);
        if (params != null) {
            Object extraOptionsObj = params.get("extraOptions");
            if (extraOptionsObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> extraOptions = (Map<String, Object>) extraOptionsObj;
                Object ar = extraOptions.get("aspect_ratio");
                if (ar != null && !ar.toString().isEmpty()) {
                    aspectRatio = sanitizeAspectRatio(ar.toString());
                }
                Object is = extraOptions.get("image_size");
                if (is != null && !is.toString().isEmpty()) {
                    imageSize = sanitizeImageSize(is.toString());
                }
            }
            // 兼容顶层 size 参数
            if (size != null && !size.isEmpty()) {
                imageSize = sanitizeImageSize(size);
            }
        }

        log.info("统一生成入口, prompt 长度: {}, 参考图数: {}, aspectRatio: {}, imageSize: {}, n: {}",
                prompt.length(), imageUrls.size(), aspectRatio, imageSize, n);

        // 3. 调用 Vertex AI（n 由模型端生成，这里单次调用返回多图）
        return callVertexAi(prompt, imageUrls.isEmpty() ? null : imageUrls, aspectRatio, imageSize);
    }

    /**
     * 将 Object 安全转换为 List<String>，非 List 或 null 返回 null
     */
    @SuppressWarnings("unchecked")
    private List<String> castStringList(Object obj) {
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            List<String> result = new ArrayList<>();
            for (Object item : list) {
                if (item != null) {
                    result.add(item.toString());
                }
            }
            return result;
        }
        return null;
    }

    // ============================================
    // 工具方法
    // ============================================

    /**
     * 白名单校验宽高比，非法值回退默认值
     * gemini-3-pro-image (Nano Banana Pro) 支持的合法值：
     * 1:1、2:3、3:2、3:4、4:3、4:5、5:4、9:16、16:9、21:9
     * 前端若传 "61" 等非法枚举会直接触发 Vertex AI 400 INVALID_ARGUMENT
     */
    private String sanitizeAspectRatio(String value) {
        if (value != null) {
            String v = value.trim();
            if (isLegalAspectRatio(v)) {
                return v;
            }
            log.warn("非法 aspectRatio: {}, 回退默认值: {}", value, defaultAspectRatio);
        }
        // 默认值本身也可能非法（如 YAML 未加引号被六十进制解析成 61），此时兜底 1:1
        String d = defaultAspectRatio == null ? "" : defaultAspectRatio.trim();
        return isLegalAspectRatio(d) ? d : "1:1";
    }

    private boolean isLegalAspectRatio(String v) {
        switch (v) {
            case "1:1":
            case "2:3":
            case "3:2":
            case "3:4":
            case "4:3":
            case "4:5":
            case "5:4":
            case "9:16":
            case "16:9":
            case "21:9":
                return true;
            default:
                return false;
        }
    }

    /**
     * 白名单校验图片尺寸，非法值回退默认值
     * gemini-3-pro-image (Nano Banana Pro) 支持：1K、2K
     */
    private String sanitizeImageSize(String value) {
        if (value == null) {
            return defaultImageSize;
        }
        String v = value.trim();
        if ("1K".equals(v) || "2K".equals(v)) {
            return v;
        }
        log.warn("非法 imageSize: {}, 回退默认值: {}", value, defaultImageSize);
        return defaultImageSize;
    }

    /**
     * 构建 Vertex AI generateContent 端点 URL
     *
     * 关键规则：location=global 时主机名不能带区域前缀！
     * - location=global      -> https://aiplatform.googleapis.com/v1beta1/...
     * - location=us-central1 -> https://us-central1-aiplatform.googleapis.com/v1beta1/...
     *
     * 如果 location=global 也拼成 global-aiplatform.googleapis.com，
     * 该域名不存在，Google 会返回 404 "URL was not found on this server"。
     * Python SDK(genai.Client(vertexai=True)) 内部自动处理了这个特例，所以 Python 能跑通。
     */
    private String buildVertexAiUrl() {
        String host;
        if ("global".equalsIgnoreCase(vertexLocation)) {
            host = "aiplatform.googleapis.com";
        } else {
            host = vertexLocation + "-aiplatform.googleapis.com";
        }
        String url = String.format(
                "https://%s/v1beta1/projects/%s/locations/%s/publishers/google/models/%s:generateContent",
                host, vertexProjectId, vertexLocation, vertexModel);
        log.info("Vertex AI 端点: {}", url);
        return url;
    }

    /**
     * 下载参考图片字节
     *
     * 处理策略：
     * 1. data: 前缀 -> 直接解码 base64（前端可能直接传 base64）
     * 2. /profile/ 本地路径 -> 直接读本地磁盘文件（避免回环 HTTP 请求被 Tomcat 拒绝、
     *    以及双斜杠 URL 导致 400/404 的问题）
     * 3. 其他 http(s) URL -> 使用 HTTP/1.1 下载（避免 JDK HttpClient 默认 h2c upgrade
     *    被服务端拒绝导致 400）
     * 4. 对下载结果做图片字节校验（文件头魔数），防止把 HTML/错误页 base64 后传给
     *    Vertex AI 导致 "Provided image is not valid"
     */
    private byte[] downloadImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new RuntimeException("参考图片 URL 不能为空");
        }

        // 1. data URL
        if (imageUrl.startsWith("data:")) {
            String base64Part = imageUrl.substring(imageUrl.indexOf(",") + 1);
            byte[] bytes = Base64.getDecoder().decode(base64Part);
            validateImageBytes(bytes, imageUrl);
            return bytes;
        }

        // 2. 本地 /profile/ 路径直读磁盘，避免回环 HTTP 请求
        String localPath = resolveLocalPath(imageUrl);
        if (localPath != null) {
            File file = new File(localPath);
            if (file.exists() && file.isFile()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] bytes = fis.readAllBytes();
                    validateImageBytes(bytes, imageUrl);
                    log.info("参考图片从本地磁盘读取成功: {} ({})", localPath, bytes.length);
                    return bytes;
                } catch (IOException e) {
                    throw new RuntimeException("读取本地参考图片失败: " + localPath + ", 原因: " + e.getMessage(), e);
                }
            } else {
                log.warn("本地参考图片不存在: {}，尝试 HTTP 下载: {}", localPath, imageUrl);
            }
        }

        // 3. 网络 URL（规范化双斜杠 + HTTP/1.1）
        try {
            String normalizedUrl = normalizeUrl(imageUrl);
            log.info("下载参考图片: {}", normalizedUrl);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(normalizedUrl))
                    .timeout(Duration.ofSeconds(30))
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("User-Agent", "Mozilla/5.0 (compatible; GuangheStudio)")
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                throw new RuntimeException("下载图片失败, 状态码: " + response.statusCode() + ", URL: " + normalizedUrl);
            }

            byte[] bytes = response.body();
            validateImageBytes(bytes, normalizedUrl);
            return bytes;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("下载图片异常: {}", imageUrl, e);
            throw new RuntimeException("下载图片失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将 /profile/ 开头的 URL 解析为本地磁盘路径（含双斜杠容错）
     * 非 /profile/ 路径返回 null，交给 HTTP 下载处理
     */
    private String resolveLocalPath(String imageUrl) {
        String path = imageUrl;
        // 去掉协议/主机部分，如 http://localhost:8780/profile/... -> /profile/...
        int idx = path.indexOf("/profile");
        if (idx >= 0) {
            path = path.substring(idx);
        }
        // 容错双斜杠：//profile -> /profile
        if (path.startsWith("//")) {
            path = path.substring(1);
        }
        if (!path.startsWith("/profile")) {
            return null;
        }
        // /profile/2026/08/05/xxx.jpg -> {profile}/2026/08/05/xxx.jpg
        String relative = path.substring("/profile".length());
        while (relative.startsWith("/") || relative.startsWith("\\")) {
            relative = relative.substring(1);
        }
        String profile = RuoYiConfig.getProfile().replace('\\', '/');
        while (profile.endsWith("/")) {
            profile = profile.substring(0, profile.length() - 1);
        }
        return profile + "/" + relative;
    }

    /**
     * 规范化 URL：合并路径中连续斜杠，避免 // 被 Tomcat 直接 400 拒绝
     * 保留协议头 http:// 或 https:// 的双斜杠
     */
    private String normalizeUrl(String imageUrl) {
        if (imageUrl == null) {
            return null;
        }
        return imageUrl.replaceAll("(?<!:)//+", "/");
    }

    /**
     * 校验下载/解码得到的字节是否为有效图片
     * 防止把 HTML/XML 错误页 base64 后传给 Vertex AI 导致 "Provided image is not valid"
     */
    private void validateImageBytes(byte[] bytes, String source) {
        if (bytes == null || bytes.length < 12) {
            throw new RuntimeException("参考图片数据无效(太小): " + source + ", 长度: " + (bytes == null ? 0 : bytes.length));
        }
        boolean valid = (bytes[0] == (byte) 0x89 && bytes[1] == (byte) 0x50 && bytes[2] == (byte) 0x4E && bytes[3] == (byte) 0x47) // PNG
                || (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF) // JPEG
                || (bytes.length > 12 && bytes[0] == (byte) 'R' && bytes[1] == (byte) 'I' && bytes[2] == (byte) 'F' && bytes[3] == (byte) 'F'
                        && bytes[8] == (byte) 'W' && bytes[9] == (byte) 'E' && bytes[10] == (byte) 'B' && bytes[11] == (byte) 'P') // WebP
                || (bytes[0] == (byte) 'G' && bytes[1] == (byte) 'I' && bytes[2] == (byte) 'F' && bytes[3] == (byte) '8'); // GIF
        if (!valid) {
            String preview = new String(bytes, 0, Math.min(64, bytes.length), java.nio.charset.StandardCharsets.UTF_8);
            throw new RuntimeException("参考图片数据不是有效图片: " + source + ", 长度: " + bytes.length + ", 内容预览: " + preview);
        }
    }

    /**
     * 根据图片字节检测 MIME 类型
     */
    private String detectMimeType(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length < 4) {
            return "image/png";
        }

        // PNG: 89 50 4E 47
        if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == (byte) 0x50
                && imageBytes[2] == (byte) 0x4E && imageBytes[3] == (byte) 0x47) {
            return "image/png";
        }
        // JPEG: FF D8 FF
        if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8 && imageBytes[2] == (byte) 0xFF) {
            return "image/jpeg";
        }
        // WebP: 52 49 46 46 ... 57 45 42 50
        if (imageBytes.length > 12 && imageBytes[0] == (byte) 'R' && imageBytes[1] == (byte) 'I'
                && imageBytes[2] == (byte) 'F' && imageBytes[3] == (byte) 'F'
                && imageBytes[8] == (byte) 'W' && imageBytes[9] == (byte) 'E'
                && imageBytes[10] == (byte) 'B' && imageBytes[11] == (byte) 'P') {
            return "image/webp";
        }
        // GIF: 47 49 46 38
        if (imageBytes[0] == (byte) 'G' && imageBytes[1] == (byte) 'I'
                && imageBytes[2] == (byte) 'F' && imageBytes[3] == (byte) '8') {
            return "image/gif";
        }

        return "image/png"; // 默认
    }

    /**
     * 测试 AI 模型是否可用
     */
    public void testAiModel() {
        if (StringUtils.isNotEmpty(vertexProjectId)) {
            testVertexAi();
            return;
        }

        // 降级测试 LightCC
        if (StringUtils.isNotEmpty(lightccImageApiKey) && StringUtils.isNotEmpty(lightccImageBaseUrl)) {
            testLightCCModel();
            return;
        }

        // 降级测试 OpenAI 302
        if (StringUtils.isNotEmpty(openAi302ApiKey) && StringUtils.isNotEmpty(openAi302BaseUrl)) {
            testOpenAiModel(openAi302BaseUrl, openAi302ApiKey, openAi302Model);
            return;
        }

        // 降级测试 OpenAI 环境变量
        if (StringUtils.isNotEmpty(openAiEnvApiKey) && StringUtils.isNotEmpty(openAiEnvBaseUrl)) {
            testOpenAiModel(openAiEnvBaseUrl, openAiEnvApiKey, openAi302Model);
            return;
        }

        throw new IllegalStateException("未配置可用的 AI 图片生成服务，请设置 Vertex AI 或 API Key");
    }

    /**
     * 测试 Vertex AI：无参考图真实生图，验证全链路（认证 + 请求 + 图片返回）
     * 旧实现只验证 token 认证，无法发现参数非法导致的 400 INVALID_ARGUMENT
     */
    private void testVertexAi() {
        try {
            List<String> images = callVertexAi(
                    "Test image generation: a simple red apple on a white table, photorealistic.",
                    null,
                    sanitizeAspectRatio(defaultAspectRatio),
                    sanitizeImageSize(defaultImageSize));
            if (images == null || images.isEmpty()) {
                throw new RuntimeException("Vertex AI 测试生图未返回图片");
            }
            log.info("✅ Vertex AI 测试生图成功, 返回 {} 张图片", images.size());
        } catch (RuntimeException e) {
            log.error("❌ Vertex AI 测试生图失败", e);
            throw e;
        } catch (Exception e) {
            log.error("❌ Vertex AI 测试生图异常", e);
            throw new RuntimeException("Vertex AI 测试生图异常: " + e.getMessage(), e);
        }
    }

    // ============================================
    // 以下为旧版的测试/降级方法（保留以备不时之需）
    // ============================================

    private void testLightCCModel() {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", lightccImageModel);
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            JSONArray contentArray = new JSONArray();
            JSONObject textContent = new JSONObject();
            textContent.put("type", "text");
            textContent.put("text", "test");
            contentArray.add(textContent);
            message.put("content", contentArray);
            messages.add(message);
            requestBody.put("messages", messages);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(lightccImageBaseUrl + "/v1/chat/completions"))
                    .header("Authorization", "Bearer " + lightccImageApiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toJSONString(), java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("LightCC 模型测试结果: status={}", response.statusCode());
        } catch (Exception e) {
            log.error("LightCC 模型测试失败", e);
            throw new RuntimeException("LightCC 模型不可用: " + e.getMessage(), e);
        }
    }

    private void testOpenAiModel(String baseUrl, String apiKey, String model) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", "test");
            messages.add(message);
            requestBody.put("messages", messages);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toJSONString(), java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("OpenAI 模型测试结果: status={}", response.statusCode());
        } catch (Exception e) {
            log.error("OpenAI 模型测试失败", e);
            throw new RuntimeException("OpenAI 模型不可用: " + e.getMessage(), e);
        }
    }
}