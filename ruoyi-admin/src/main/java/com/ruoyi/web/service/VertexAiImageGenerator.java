package com.ruoyi.web.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

/**
 * 独立运行的 Vertex AI 生图工具（复刻 Python 脚本逻辑）
 *
 * 功能：
 * 1. 代理配置（指向新加坡 Squid）
 * 2. 认证配置（使用 ADC 文件）
 * 3. 调用 gemini-3-pro-image (Nano Banana Pro) 生成图片
 * 4. 保存图片到当前目录
 *
 * 运行方式（在项目根目录）：
 *   mvn -pl ruoyi-admin -am compile -DskipTests
 *   mvn -pl ruoyi-admin exec:java -Dexec.mainClass=com.ruoyi.web.service.VertexAiImageGenerator \
 *       -Dexec.args="'一张赛博朋克风格的机械猫，霓虹灯光，雨夜街道，电影感构图' 16:9 1K"
 *
 * 或手动运行：
 *   mvn -pl ruoyi-admin dependency:build-classpath -Dmdep.outputFile=/tmp/cp.txt
 *   java -cp "ruoyi-admin/target/classes:$(cat /tmp/cp.txt)" com.ruoyi.web.service.VertexAiImageGenerator
 */
public class VertexAiImageGenerator {

    // ============================================
    // 1. 代理配置（指向新加坡 Squid）
    // ============================================
    private static final String SINGAPORE_PROXY_IP = "43.160.202.223";
    private static final int SQUID_PORT = 3128;

    // ============================================
    // 2. 认证配置（使用现有的 ADC 文件）
    // ============================================
    private static final String ADC_PATH = "/home/ubuntu/.config/gcloud/application_default_credentials.json";
    private static final String PROJECT_ID = "project-56a8fdac-a9a9-4727-9bc";
    private static final String LOCATION = "global";
    private static final String MODEL = "gemini-3-pro-image";

    public static void main(String[] args) {
        // 提示词（可用命令行参数覆盖）
        String prompt = args.length > 0 ? args[0] : "一张赛博朋克风格的机械猫，霓虹灯光，雨夜街道，电影感构图";
        String aspectRatio = args.length > 1 ? args[1] : "16:9";
        String imageSize = args.length > 2 ? args[2] : "1K";

        System.out.println("🚀 初始化 Vertex AI 客户端...");
        long startTime = System.currentTimeMillis();

        try {
            // 1. 获取 access token（走代理）
            String accessToken = getAccessToken();
            System.out.println("✅ Vertex AI 认证成功");

            // 2. 调用生图接口
            System.out.println("🎨 正在调用 Nano Banana Pro...");
            String responseBody = generateImage(prompt, aspectRatio, imageSize, accessToken);
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println("✅ 生成完成，耗时: " + elapsed + "秒");

            // 3. 解析并保存图片
            int imgCount = saveImages(responseBody);
            if (imgCount == 0) {
                System.out.println("⚠️ 没有生成图片");
            } else {
                System.out.println("\n🎉 成功生成 " + imgCount + " 张图片！");
            }
        } catch (Exception e) {
            System.err.println("❌ 生成失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 获取 Vertex AI Bearer token
     * GoogleCredentials 内部使用 HttpURLConnection，需要设置系统属性走代理
     */
    private static String getAccessToken() throws Exception {
        // 设置代理（认证请求也走新加坡 Squid）
        System.setProperty("https.proxyHost", SINGAPORE_PROXY_IP);
        System.setProperty("https.proxyPort", String.valueOf(SQUID_PORT));

        GoogleCredentials credentials;
        File credFile = new File(ADC_PATH);
        if (credFile.exists()) {
            System.out.println("从 ADC 文件加载 credentials: " + ADC_PATH);
            try (FileInputStream fis = new FileInputStream(credFile)) {
                credentials = GoogleCredentials.fromStream(fis)
                        .createScoped("https://www.googleapis.com/auth/cloud-platform");
            }
        } else {
            System.out.println("ADC 文件不存在，回退到默认方式查找");
            credentials = GoogleCredentials.getApplicationDefault()
                    .createScoped("https://www.googleapis.com/auth/cloud-platform");
        }

        credentials.refreshIfExpired();
        String token = credentials.getAccessToken().getTokenValue();
        System.out.println("access token 获取成功, 前缀: " + token.substring(0, Math.min(10, token.length())) + "...");
        return token;
    }

    /**
     * 调用 Vertex AI Gemini 生成图片
     */
    private static String generateImage(String prompt, String aspectRatio, String imageSize, String accessToken)
            throws Exception {
        // 构建 Vertex AI Gemini API URL
        // 注意：
        // 1. gemini-3-pro-image (Nano Banana Pro) 仅支持 v1beta1 API 端点，v1 会返回 404
        // 2. location=global 时主机名不带区域前缀（aiplatform.googleapis.com），
        //    否则会拼出 global-aiplatform.googleapis.com 这个不存在的域名导致 404
        String host = "global".equalsIgnoreCase(LOCATION)
                ? "aiplatform.googleapis.com"
                : LOCATION + "-aiplatform.googleapis.com";
        String url = String.format(
                "https://%s/v1beta1/projects/%s/locations/%s/publishers/google/models/%s:generateContent",
                host, PROJECT_ID, LOCATION, MODEL);
        System.out.println("Vertex AI 端点: " + url);

        // 构建请求体
        JSONObject requestBody = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        content.put("role", "user");

        JSONArray parts = new JSONArray();
        JSONObject textPart = new JSONObject();
        textPart.put("text", prompt);
        parts.add(textPart);
        content.put("parts", parts);
        contents.add(content);
        requestBody.put("contents", contents);

        // 生图配置（对应 Python 的 GenerateContentConfig）
        JSONObject generationConfig = new JSONObject();
        JSONArray responseModalities = new JSONArray();
        responseModalities.add("TEXT");
        responseModalities.add("IMAGE");
        generationConfig.put("responseModalities", responseModalities);

        // imageConfig（对应 Python 的 ImageConfig）
        JSONObject imageConfig = new JSONObject();
        imageConfig.put("aspectRatio", aspectRatio);
        imageConfig.put("imageSize", imageSize);
        generationConfig.put("imageConfig", imageConfig);

        requestBody.put("generationConfig", generationConfig);

        System.out.println("请求参数: aspectRatio=" + aspectRatio + ", imageSize=" + imageSize);

        // 走代理的 HttpClient
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(60))
                .proxy(ProxySelector.of(new InetSocketAddress(SINGAPORE_PROXY_IP, SQUID_PORT)))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(120))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toJSONString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Vertex AI 响应状态: " + response.statusCode());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Vertex AI 调用失败: HTTP " + response.statusCode() + ", body: " + response.body());
        }
        return response.body();
    }

    /**
     * 从响应中提取图片并保存到当前目录
     */
    private static int saveImages(String responseBody) throws Exception {
        JSONObject respJson = JSON.parseObject(responseBody);
        JSONArray candidates = respJson.getJSONArray("candidates");
        if (candidates == null || candidates.isEmpty()) {
            System.out.println("⚠️ 响应中没有 candidates");
            return 0;
        }

        int imgCount = 0;
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
                    System.out.println("📝 模型说明: " + part.getString("text"));
                }
                // 图片
                if (part.containsKey("inlineData")) {
                    JSONObject inlineData = part.getJSONObject("inlineData");
                    String data = inlineData.getString("data");
                    String mimeType = inlineData.getString("mimeType");
                    if (data != null && !data.isEmpty()) {
                        byte[] imageBytes = Base64.getDecoder().decode(data);
                        String ext = mimeType != null && mimeType.contains("jpeg") ? "jpg" : "png";
                        String filename = "banana_" + System.currentTimeMillis() + "_" + imgCount + "." + ext;
                        try (FileOutputStream fos = new FileOutputStream(filename)) {
                            fos.write(imageBytes);
                        }
                        imgCount++;
                        System.out.println("✅ 图片已保存: " + filename + " (" + imageBytes.length + " bytes)");
                    }
                }
            }
        }
        return imgCount;
    }
}