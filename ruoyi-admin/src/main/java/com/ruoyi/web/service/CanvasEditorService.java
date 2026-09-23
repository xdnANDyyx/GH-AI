package com.ruoyi.web.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.auth.oauth2.GoogleCredentials;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.sql.SqlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;

/**
 * Canvas Editor 服务
 * 提供图片编辑功能：扩图、多角度、改文字、局部重绘、图层检测等
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CanvasEditorService {

    private final RuoYiConfig ruoYiConfig;
    private final JdbcTemplate jdbcTemplate;
    private final ImageUploadService imageUploadService;

    // HTTP 客户端（下载参考图片用，不走代理）
    private HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(60))
            .build();

    // ============================================
    // Vertex AI 配置
    // ============================================
    @Value("${vertex.ai.project-id:}")
    private String vertexProjectId;

    @Value("${vertex.ai.location:global}")
    private String vertexLocation;

    @Value("${vertex.ai.model:gemini-3-pro-image}")
    private String vertexModel;

    @Value("${vertex.ai.read-timeout:600}")
    private int vertexReadTimeout;

    @Value("${vertex.ai.credentials-path:}")
    private String credentialsPath;

    // ============================================
    // 代理配置
    // ============================================
    @Value("${vertex.ai.proxy.host:}")
    private String proxyHost;

    @Value("${vertex.ai.proxy.port:0}")
    private int proxyPort;

    // Vertex AI 生图请求用的 HttpClient（走代理）
    private HttpClient vertexHttpClient;

    private synchronized HttpClient getVertexHttpClient() {
        if (vertexHttpClient == null) {
            if (proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
                log.info("🔁 Canvas Editor 配置代理: {}:{}", proxyHost, proxyPort);
                vertexHttpClient = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(60))
                        .proxy(java.net.ProxySelector.of(
                                new java.net.InetSocketAddress(proxyHost, proxyPort)))
                        .build();
            } else {
                log.info("🔁 Canvas Editor 不使用代理");
                vertexHttpClient = httpClient;
            }
        }
        return vertexHttpClient;
    }

    @PostConstruct
    public void init() {
        try {
            httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .version(HttpClient.Version.HTTP_2)
                .build();

            // 检查并创建编辑历史表
            checkAndCreateHistoryTable();

            log.info("Canvas Editor Service 初始化成功");
        } catch (Exception e) {
            log.error("Canvas Editor Service 初始化失败", e);
        }
    }

    /**
     * 检查并创建编辑历史表
     */
    private void checkAndCreateHistoryTable() {
        try {
            // 检查表是否存在
            jdbcTemplate.execute("SELECT 1 FROM canvas_edit_history LIMIT 1");
            log.info("编辑历史表 canvas_edit_history 已存在");
        } catch (Exception e) {
            log.info("编辑历史表不存在，正在创建...");
            try {
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `canvas_edit_history` ("
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID', "
                    + "`image_id` VARCHAR(255) NOT NULL COMMENT '图片ID或URL', "
                    + "`user_id` BIGINT NOT NULL COMMENT '操作用户ID', "
                    + "`operation` VARCHAR(50) NOT NULL COMMENT '操作类型', "
                    + "`params` JSON COMMENT '操作参数(JSON格式)', "
                    + "`result_url` TEXT COMMENT '结果图片URL', "
                    + "`version` INT NOT NULL DEFAULT 1 COMMENT '版本号', "
                    + "`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', "
                    + "`update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', "
                    + "PRIMARY KEY (`id`), "
                    + "INDEX `idx_image_id` (`image_id`), "
                    + "INDEX `idx_user_id` (`user_id`), "
                    + "INDEX `idx_create_time` (`create_time`), "
                    + "UNIQUE KEY `uk_image_version` (`image_id`, `version`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画布编辑器编辑历史表'");
                log.info("编辑历史表创建成功");
            } catch (Exception ex) {
                log.error("创建编辑历史表失败", ex);
            }
        }
    }

    // ============================================
    // 1. 图片扩图
    // ============================================
    public Map<String, Object> extendImage(String imageUrl, String ratio, Integer width, Integer height) throws Exception {
        log.info("执行图片扩图: ratio={}, width={}, height={}", ratio, width, height);

        // 下载原图
        byte[] imageBytes = downloadImageBytes(imageUrl);

        // 构建扩图提示词
        String aspectRatio = ratio != null ? ratio : "1:1";
        String prompt = buildExtendPrompt(aspectRatio, width, height);

        // 调用Vertex AI进行扩图
        List<String> resultImages = callVertexAi(prompt, List.of(imageUrl), aspectRatio, "1K");

        if (resultImages == null || resultImages.isEmpty()) {
            throw new RuntimeException("扩图失败：Vertex AI未返回结果");
        }

        // 返回第一张图片
        String resultImage = resultImages.get(0);

        Map<String, Object> result = new HashMap<>();
        result.put("url", resultImage);
        result.put("width", width);
        result.put("height", height);
        result.put("ratio", aspectRatio);

        // 保存编辑历史
        Map<String, Object> params = new HashMap<>();
        params.put("imageUrl", imageUrl);
        params.put("ratio", ratio);
        params.put("width", width);
        params.put("height", height);
        saveEditHistory(imageUrl, "extend", params, resultImage);

        log.info("图片扩图成功");
        return result;
    }

    /**
     * 构建扩图提示词
     */
    private String buildExtendPrompt(String ratio, Integer width, Integer height) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Extend the canvas of this image to ").append(ratio).append(" aspect ratio. ");

        if (width != null && height != null) {
            prompt.append("Target size: ").append(width).append("x").append(height).append(" pixels. ");
        }

        prompt.append("Keep the original content intact and centered. ");
        prompt.append("Generate appropriate background/context on the extended areas that matches the original image style and lighting. ");
        prompt.append("The extension should look natural and seamless.");

        return prompt.toString();
    }

    // ============================================
    // 2. 多角度生成
    // ============================================
    public List<Map<String, Object>> generateMultiAngle(String imageUrl, int count, String type) throws Exception {
        log.info("执行多角度生成: count={}, type={}", count, type);

        // 下载原图
        byte[] imageBytes = downloadImageBytes(imageUrl);

        // 构建多角度提示词
        String prompt = buildMultiAnglePrompt(count, type);

        // 调用Vertex AI生成多张图片
        List<String> resultImages = callVertexAi(prompt, List.of(imageUrl), "1:1", "1K");

        if (resultImages == null || resultImages.isEmpty()) {
            throw new RuntimeException("多角度生成失败：Vertex AI未返回结果");
        }

        // 构建返回结果
        List<Map<String, Object>> images = new ArrayList<>();
        String[] angleDescriptions = getAngleDescriptions(type, count);

        for (int i = 0; i < Math.min(resultImages.size(), count); i++) {
            Map<String, Object> img = new HashMap<>();
            img.put("url", resultImages.get(i));
            img.put("angle", angleDescriptions[i % angleDescriptions.length]);
            img.put("type", type != null ? type : "rotate");
            images.add(img);
        }

        log.info("多角度生成成功，生成{}张图片", images.size());

        // 保存编辑历史
        Map<String, Object> params = new HashMap<>();
        params.put("imageUrl", imageUrl);
        params.put("count", count);
        params.put("type", type);
        saveEditHistory(imageUrl, "multi-angle", params, images.size() > 0 ? (String) images.get(0).get("url") : "");

        return images;
    }

    // ============================================
    // 2b. 多角度生成（自定义摄像机方位）
    // ============================================
    public List<Map<String, Object>> generateMultiAngleCustom(String imageUrl, int count, int horizontal, int vertical) throws Exception {
        log.info("执行自定义角度生成: count={}, horizontal={}, vertical={}", count, horizontal, vertical);

        // 构建自定义角度提示词
        String prompt = buildCustomAnglePrompt(horizontal, vertical);

        // 调用Vertex AI生成图片
        List<String> resultImages = callVertexAi(prompt, List.of(imageUrl), "1:1", "1K");

        if (resultImages == null || resultImages.isEmpty()) {
            throw new RuntimeException("自定义角度生成失败：Vertex AI未返回结果");
        }

        // 构建返回结果
        List<Map<String, Object>> images = new ArrayList<>();
        for (int i = 0; i < Math.min(resultImages.size(), count); i++) {
            Map<String, Object> img = new HashMap<>();
            img.put("url", resultImages.get(i));
            img.put("angle", "方位_" + horizontal + "_" + vertical);
            img.put("type", "custom");
            images.add(img);
        }

        log.info("自定义角度生成成功，生成{}张图片", images.size());

        // 保存编辑历史
        Map<String, Object> params = new HashMap<>();
        params.put("imageUrl", imageUrl);
        params.put("count", count);
        params.put("horizontal", horizontal);
        params.put("vertical", vertical);
        params.put("isCustomAngle", true);
        saveEditHistory(imageUrl, "multi-angle", params, images.size() > 0 ? (String) images.get(0).get("url") : "");

        return images;
    }

    /**
     * 构建自定义摄像机方位的提示词
     * horizontal: -180 ~ 180 (水平旋转角度)
     * vertical: -90 ~ 90 (垂直俯仰角度)
     */
    private String buildCustomAnglePrompt(int horizontal, int vertical) {
        StringBuilder prompt = new StringBuilder();

        // 描述水平方位
        String hDirection;
        if (horizontal == 0) {
            hDirection = "front view (0°)";
        } else if (horizontal > 0 && horizontal <= 45) {
            hDirection = "slightly right-front view (" + horizontal + "°)";
        } else if (horizontal > 45 && horizontal <= 90) {
            hDirection = "right side view (" + horizontal + "°)";
        } else if (horizontal > 90 && horizontal <= 135) {
            hDirection = "right-rear view (" + horizontal + "°)";
        } else if (horizontal > 135 && horizontal <= 180) {
            hDirection = "rear view (" + horizontal + "°)";
        } else if (horizontal < 0 && horizontal >= -45) {
            hDirection = "slightly left-front view (" + horizontal + "°)";
        } else if (horizontal < -45 && horizontal >= -90) {
            hDirection = "left side view (" + horizontal + "°)";
        } else if (horizontal < -90 && horizontal >= -135) {
            hDirection = "left-rear view (" + horizontal + "°)";
        } else {
            hDirection = "rear view (" + horizontal + "°)";
        }

        // 描述垂直方位
        String vDirection;
        if (vertical == 0) {
            vDirection = "eye-level";
        } else if (vertical > 30) {
            vDirection = "high angle / slightly top-down view (" + vertical + "°)";
        } else if (vertical > 0) {
            vDirection = "slightly elevated view (" + vertical + "°)";
        } else if (vertical < -30) {
            vDirection = "low angle / looking up view (" + vertical + "°)";
        } else {
            vDirection = "slightly lowered view (" + vertical + "°)";
        }

        prompt.append("Generate a product photo from a specific camera angle. ");
        prompt.append("Camera position: horizontal rotation ").append(horizontal).append("°, vertical tilt ").append(vertical).append("°. ");
        prompt.append("This corresponds to a ").append(hDirection).append(" and ").append(vDirection).append(". ");
        prompt.append("Maintain the product's key features, colors, and lighting consistency. ");
        prompt.append("Keep the background consistent and professional. ");
        prompt.append("The product should remain the same, only the camera angle changes.");

        return prompt.toString();
    }

    /**
     * 构建多角度生成提示词
     */
    private String buildMultiAnglePrompt(int count, String type) {
        StringBuilder prompt = new StringBuilder();

        switch (type) {
            case "rotate":
                prompt.append("Generate ").append(count).append(" different rotated views of this product at ");
                prompt.append("0°, 45°, 90°, 135°, 180°, 225°, 270°, 315° angles. ");
                break;
            case "multi-view":
                prompt.append("Generate ").append(count).append(" different views of this product: ");
                prompt.append("front view, side view, top view, back view, perspective view, close-up view, ");
                prompt.append("detail view, full view. ");
                break;
            case "360":
                prompt.append("Generate ").append(count).append(" frames for a 360° rotation view of this product. ");
                prompt.append("Each frame should be rotated ").append(360 / count).append("° from the previous one. ");
                break;
            default:
                prompt.append("Generate ").append(count).append(" different angles/views of this product. ");
        }

        prompt.append("Maintain the product's key features, colors, and lighting consistency across all angles. ");
        prompt.append("Keep the background consistent and professional.");

        return prompt.toString();
    }

    /**
     * 获取角度描述
     */
    private String[] getAngleDescriptions(String type, int count) {
        if ("rotate".equals(type)) {
            String[] rotations = {"0°", "45°", "90°", "135°", "180°", "225°", "270°", "315°"};
            return java.util.Arrays.copyOf(rotations, Math.min(count, rotations.length));
        } else if ("multi-view".equals(type)) {
            String[] views = {"前视图", "侧视图", "俯视图", "后视图", "透视视图", "特写视图", "细节视图", "全景视图"};
            return java.util.Arrays.copyOf(views, Math.min(count, views.length));
        } else if ("360".equals(type)) {
            String[] angles = new String[count];
            for (int i = 0; i < count; i++) {
                angles[i] = (i * 360 / count) + "°";
            }
            return angles;
        }
        return new String[count];
    }

    // ============================================
    // 3. 修改文字
    // ============================================
    public Map<String, Object> editImageText(String imageUrl, String originalText, String newText, String font) throws Exception {
        log.info("执行修改文字: originalText={}, newText={}, font={}", originalText, newText, font);

        // 下载原图
        byte[] imageBytes = downloadImageBytes(imageUrl);

        // 构建修改文字提示词
        String prompt = buildEditTextPrompt(originalText, newText, font);

        // 调用Vertex AI修改文字
        List<String> resultImages = callVertexAi(prompt, List.of(imageUrl), "1:1", "1K");

        if (resultImages == null || resultImages.isEmpty()) {
            throw new RuntimeException("修改文字失败：Vertex AI未返回结果");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("url", resultImages.get(0));
        result.put("originalText", originalText);
        result.put("newText", newText);
        result.put("font", font != null ? font : "Arial");

        // 保存编辑历史
        Map<String, Object> params = new HashMap<>();
        params.put("imageUrl", imageUrl);
        params.put("originalText", originalText);
        params.put("newText", newText);
        params.put("font", font);
        saveEditHistory(imageUrl, "edit-text", params, resultImages.get(0));

        log.info("修改文字成功");
        return result;
    }

    /**
     * 构建修改文字提示词
     */
    private String buildEditTextPrompt(String originalText, String newText, String font) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Replace the text \"").append(originalText).append("\" with \"").append(newText).append("\" in this image. ");

        if (font != null && !font.isEmpty()) {
            prompt.append("Use ").append(font).append(" font or a similar style. ");
        }

        prompt.append("Keep the rest of the image exactly the same. ");
        prompt.append("Maintain the original text position, size, color, and style as much as possible. ");
        prompt.append("The replacement should look natural and seamless.");

        return prompt.toString();
    }

    // ============================================
    // 4. 局部重绘
    // ============================================
    public Map<String, Object> partialRedraw(String imageUrl, String description, Map<String, Object> mask) throws Exception {
        log.info("执行局部重绘: description={}, mask={}", description, mask);

        // 下载原图
        byte[] imageBytes = downloadImageBytes(imageUrl);

        // 处理mask区域：在原图上标记mask区域
        byte[] imageWithMask;
        if (mask != null && mask.containsKey("x") && mask.containsKey("y")
                && mask.containsKey("width") && mask.containsKey("height")) {
            log.info("处理mask区域: 在原图上标记需要重绘的区域");
            imageWithMask = applyMaskToImage(imageBytes, mask);
        } else {
            log.info("未提供mask信息，直接使用原图");
            imageWithMask = imageBytes;
        }

        // 将处理后的图片转为data URI
        String mimeType = detectMimeType(imageWithMask);
        String base64 = Base64.getEncoder().encodeToString(imageWithMask);
        String imageDataUri = "data:" + mimeType + ";base64," + base64;

        // 构建局部重绘提示词
        String prompt = buildPartialRedrawPrompt(description, mask);

        // 调用Vertex AI进行局部重绘（传入带mask标记的图片）
        List<String> resultImages = callVertexAi(prompt, List.of(imageDataUri), "1:1", "1K");

        if (resultImages == null || resultImages.isEmpty()) {
            throw new RuntimeException("局部重绘失败：Vertex AI未返回结果");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("url", resultImages.get(0));
        result.put("description", description);

        // 保存编辑历史
        Map<String, Object> params = new HashMap<>();
        params.put("imageUrl", imageUrl);
        params.put("description", description);
        if (mask != null) params.put("mask", mask);
        saveEditHistory(imageUrl, "partial-redraw", params, resultImages.get(0));

        log.info("局部重绘成功");
        return result;
    }

    /**
     * 在原图上应用mask标记（用红色半透明遮罩标记需要重绘的区域）
     */
    byte[] applyMaskToImage(byte[] imageBytes, Map<String, Object> mask) throws Exception {
        try {
            // 解析mask参数
            int maskX = mask.get("x") instanceof Number ? ((Number) mask.get("x")).intValue() : 0;
            int maskY = mask.get("y") instanceof Number ? ((Number) mask.get("y")).intValue() : 0;
            int maskWidth = mask.get("width") instanceof Number ? ((Number) mask.get("width")).intValue() : 0;
            int maskHeight = mask.get("height") instanceof Number ? ((Number) mask.get("height")).intValue() : 0;

            log.info("应用mask: x={}, y={}, width={}, height={}", maskX, maskY, maskWidth, maskHeight);

            // 读取原图
            BufferedImage originalImage = ImageIO.read(new java.io.ByteArrayInputStream(imageBytes));
            if (originalImage == null) {
                log.warn("无法读取图片，跳过mask处理");
                return imageBytes;
            }

            int imgWidth = originalImage.getWidth();
            int imgHeight = originalImage.getHeight();

            // 确保mask不超出图片边界
            maskX = Math.max(0, Math.min(maskX, imgWidth - 1));
            maskY = Math.max(0, Math.min(maskY, imgHeight - 1));
            maskWidth = Math.min(maskWidth, imgWidth - maskX);
            maskHeight = Math.min(maskHeight, imgHeight - maskY);

            if (maskWidth <= 0 || maskHeight <= 0) {
                log.warn("mask尺寸无效，跳过mask处理");
                return imageBytes;
            }

            // 创建副本
            BufferedImage maskedImage = new BufferedImage(
                imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB
            );
            java.awt.Graphics2D g = maskedImage.createGraphics();

            // 绘制原图
            g.drawImage(originalImage, 0, 0, null);

            // 绘制半透明红色遮罩在mask区域
            g.setComposite(java.awt.AlphaComposite.getInstance(
                java.awt.AlphaComposite.SRC_OVER, 0.3f
            ));
            g.setColor(java.awt.Color.RED);
            g.fillRect(maskX, maskY, maskWidth, maskHeight);

            // 绘制边框（让AI更容易识别）
            g.setComposite(java.awt.AlphaComposite.getInstance(
                java.awt.AlphaComposite.SRC_OVER, 1.0f
            ));
            g.setColor(java.awt.Color.RED);
            g.setStroke(new java.awt.BasicStroke(3));
            g.drawRect(maskX, maskY, maskWidth, maskHeight);

            g.dispose();

            // 转换回字节数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(maskedImage, "png", baos);
            byte[] result = baos.toByteArray();

            log.info("Mask处理完成: 原图{}x{}, mask区域{}x{}",
                imgWidth, imgHeight, maskWidth, maskHeight);

            return result;

        } catch (Exception e) {
            log.error("应用mask失败，返回原图: {}", e.getMessage());
            return imageBytes;
        }
    }

    /**
     * 构建局部重绘提示词
     */
    private String buildPartialRedrawPrompt(String description, Map<String, Object> mask) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("In the highlighted area, ").append(description).append(". ");
        prompt.append("Keep the rest of the image exactly the same. ");
        prompt.append("Make the changes look natural and consistent with the original image style.");

        if (mask != null) {
            prompt.append("Mask area: x=").append(mask.get("x"))
                   .append(", y=").append(mask.get("y"))
                   .append(", width=").append(mask.get("width"))
                   .append(", height=").append(mask.get("height"));
        }

        return prompt.toString();
    }

    // ============================================
    // 5. 图层检测
    // ============================================
    public List<Map<String, Object>> detectLayers(String imageUrl) throws Exception {
        log.info("执行图层检测: imageUrl={}", imageUrl);

        // 使用AI进行图层检测和分析
        return detectLayersWithAI(imageUrl);
    }

    /**
     * 使用AI视觉模型检测图片中的图层
     */
    List<Map<String, Object>> detectLayersWithAI(String imageUrl) throws Exception {
        log.info("使用AI进行图层检测");

        try {
            String accessToken = getVertexAccessToken();

            // 下载并压缩图片
            byte[] imageBytes = downloadImageBytes(imageUrl);
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = detectMimeType(imageBytes);

            // 使用gemini-2.5-flash进行视觉理解
            String model = "gemini-2.5-flash";
            String url = buildVertexAiUrl(model);

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            content.put("role", "user");

            JSONArray parts = new JSONArray();

            // 添加文本提示词
            JSONObject textPart = new JSONObject();
            textPart.put("text", "请分析这张图片的图层结构，识别主要的视觉元素层次。\n\n"
                    + "以JSON格式返回图层列表，每个图层包含：\n"
                    + "{\n"
                    + "  \"name\": \"图层名称\",\n"
                    + "  \"type\": \"图层类型(background/product/text/decorative/overlay等)\",\n"
                    + "  \"description\": \"图层描述\",\n"
                    + "  \"position\": {\"x\": 相对x坐标(0-1000), \"y\": 相对y坐标(0-1000)},\n"
                    + "  \"size\": {\"width\": 相对宽度(0-1000), \"height\": 相对高度(0-1000)},\n"
                    + "  \"confidence\": 置信度(0-1)\n"
                    + "}\n\n"
                    + "返回一个JSON数组。只返回JSON，不要其他说明。\n"
                    + "示例：\n"
                    + "[{\"name\":\"背景\",\"type\":\"background\",\"description\":\"纯色背景\",\"position\":{\"x\":0,\"y\":0},\"size\":{\"width\":1000,\"height\":1000},\"confidence\":0.95}]");
            parts.add(textPart);

            // 添加图片
            JSONObject imagePart = new JSONObject();
            JSONObject inlineData = new JSONObject();
            inlineData.put("mimeType", mimeType);
            inlineData.put("data", base64);
            imagePart.put("inlineData", inlineData);
            parts.add(imagePart);

            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            // 文本生成配置
            JSONObject generationConfig = new JSONObject();
            JSONArray responseModalities = new JSONArray();
            responseModalities.add("TEXT");
            generationConfig.put("responseModalities", responseModalities);
            requestBody.put("generationConfig", generationConfig);

            log.info("调用 Vertex AI 检测图层, 模型: {}", model);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toJSONString(), java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = getVertexHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            log.info("Vertex AI 图层检测响应状态: {}", response.statusCode());

            if (response.statusCode() != 200) {
                log.error("图层检测失败, 状态码: {}, 响应: {}", response.statusCode(), response.body());
                throw new RuntimeException("图层检测失败: HTTP " + response.statusCode());
            }

            // 解析响应，提取图层信息
            return parseLayerDetectionResponse(response.body());

        } catch (Exception e) {
            log.error("AI图层检测失败", e);
            throw new RuntimeException("图层检测失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析图层检测响应
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseLayerDetectionResponse(String responseBody) throws Exception {
        List<Map<String, Object>> layers = new ArrayList<>();

        try {
            JSONObject respJson = JSON.parseObject(responseBody);
            JSONArray candidates = respJson.getJSONArray("candidates");

            if (candidates == null || candidates.isEmpty()) {
                log.warn("图层检测响应中没有candidates");
                return layers;
            }

            // 提取文本内容
            StringBuilder fullText = new StringBuilder();
            JSONObject candidate = candidates.getJSONObject(0);
            JSONObject content = candidate.getJSONObject("content");
            if (content != null) {
                JSONArray parts = content.getJSONArray("parts");
                if (parts != null) {
                    for (int i = 0; i < parts.size(); i++) {
                        JSONObject part = parts.getJSONObject(i);
                        if (part.containsKey("text")) {
                            fullText.append(part.getString("text"));
                        }
                    }
                }
            }

            // 尝试解析JSON格式的图层列表
            String responseText = fullText.toString().trim();
            log.info("图层检测返回文本: {}", responseText);

            // 提取JSON数组（可能被```json包裹）
            String jsonStr = responseText;
            if (responseText.startsWith("```json")) {
                jsonStr = responseText.substring(7);
            } else if (responseText.startsWith("```")) {
                jsonStr = responseText.substring(3);
            }
            if (jsonStr.endsWith("```")) {
                jsonStr = jsonStr.substring(0, jsonStr.length() - 3);
            }
            jsonStr = jsonStr.trim();

            // 解析JSON
            Object parsed = JSON.parse(jsonStr);
            if (parsed instanceof JSONArray) {
                JSONArray layerArray = (JSONArray) parsed;
                for (int i = 0; i < layerArray.size(); i++) {
                    Object item = layerArray.get(i);
                    if (item instanceof JSONObject) {
                        JSONObject layerObj = (JSONObject) item;

                        Map<String, Object> layer = new HashMap<>();
                        layer.put("id", "layer_" + (i + 1));
                        layer.put("name", layerObj.getString("name"));
                        layer.put("type", layerObj.getString("type"));
                        layer.put("description", layerObj.getString("description"));
                        layer.put("confidence", layerObj.getDouble("confidence"));

                        // 位置信息
                        JSONObject position = layerObj.getJSONObject("position");
                        if (position != null) {
                            Map<String, Object> posMap = new HashMap<>();
                            posMap.put("x", position.getInteger("x"));
                            posMap.put("y", position.getInteger("y"));
                            layer.put("position", posMap);
                        }

                        // 尺寸信息
                        JSONObject size = layerObj.getJSONObject("size");
                        if (size != null) {
                            Map<String, Object> sizeMap = new HashMap<>();
                            sizeMap.put("width", size.getInteger("width"));
                            sizeMap.put("height", size.getInteger("height"));
                            layer.put("size", sizeMap);
                        }

                        layers.add(layer);
                    }
                }
            }

            // 如果解析失败或为空，返回默认图层
            if (layers.isEmpty()) {
                log.warn("图层检测解析失败或未检测到图层，返回默认图层");
                Map<String, Object> defaultLayer = new HashMap<>();
                defaultLayer.put("id", "layer_1");
                defaultLayer.put("name", "主图层");
                defaultLayer.put("type", "main");
                defaultLayer.put("description", "主要内容图层");
                defaultLayer.put("confidence", 0.8);

                Map<String, Object> position = new HashMap<>();
                position.put("x", 0);
                position.put("y", 0);
                defaultLayer.put("position", position);

                Map<String, Object> size = new HashMap<>();
                size.put("width", 1000);
                size.put("height", 1000);
                defaultLayer.put("size", size);

                layers.add(defaultLayer);
            }

            log.info("图层检测成功，检测到{}个图层", layers.size());
            return layers;

        } catch (Exception e) {
            log.error("解析图层检测响应失败: {}", e.getMessage(), e);
            // 返回默认图层而不是抛出异常
            Map<String, Object> fallbackLayer = new HashMap<>();
            fallbackLayer.put("id", "layer_1");
            fallbackLayer.put("name", "主图层");
            fallbackLayer.put("type", "main");
            fallbackLayer.put("description", "默认图层");
            fallbackLayer.put("confidence", 0.5);
            layers.add(fallbackLayer);
            return layers;
        }
    }

    // ============================================
    // 6. 下载图层
    // ============================================
    public Map<String, Object> downloadLayers(String imageUrl, List<String> layers, String format) throws Exception {
        log.info("执行下载图层: layers={}, format={}", layers, format);

        // 下载原图
        byte[] imageBytes = downloadImageBytes(imageUrl);

        // 如果是单图层，直接返回原图
        if (layers == null || layers.size() <= 1) {
            String uploadedUrl = uploadImage(imageBytes);
            Map<String, Object> result = new HashMap<>();
            result.put("downloadUrl", uploadedUrl);
            result.put("format", format != null ? format : "png");
            result.put("layers", layers);
            return result;
        }

        // 调用AI进行图层分离
        log.info("开始进行多图层分离，目标层数: {}", layers.size());
        List<byte[]> separatedLayers = separateLayersWithAI(imageUrl, layers.size());

        if (separatedLayers == null || separatedLayers.isEmpty()) {
            log.warn("AI图层分离失败，返回原图");
            String uploadedUrl = uploadImage(imageBytes);
            Map<String, Object> result = new HashMap<>();
            result.put("downloadUrl", uploadedUrl);
            result.put("format", format != null ? format : "png");
            result.put("layers", layers);
            result.put("warning", "图层分离失败，返回原图");
            return result;
        }

        // 构建返回结果（包含每个分离后的图层URL）
        List<String> layerUrls = new ArrayList<>();
        for (byte[] layerBytes : separatedLayers) {
            String layerUrl = uploadImage(layerBytes);
            layerUrls.add(layerUrl);
        }

        // 打包所有图层为一个ZIP文件（如果有多个图层）
        String downloadUrl;
        if (layerUrls.size() > 1) {
            try {
                downloadUrl = packLayersToZip(layerUrls, layers);
                log.info("多图层打包成功: {}", downloadUrl);
            } catch (Exception e) {
                log.error("多图层打包失败，返回第一层", e);
                downloadUrl = layerUrls.get(0);
            }
        } else {
            downloadUrl = layerUrls.get(0);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("downloadUrl", downloadUrl);
        result.put("format", format != null ? format : "png");
        result.put("layers", layers);
        result.put("separatedLayers", layerUrls);
        result.put("count", separatedLayers.size());

        log.info("图层分离完成，成功分离{}层", separatedLayers.size());
        return result;
    }

    /**
     * 将多个图层打包成ZIP文件
     *
     * @param layerUrls 图层URL列表
     * @param layerNames 图层名称列表
     * @return ZIP文件URL
     * @throws Exception 打包失败
     */
    String packLayersToZip(List<String> layerUrls, List<String> layerNames) throws Exception {
        log.info("开始打包{}个图层为ZIP文件", layerUrls.size());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            // 为每个图层创建ZIP条目
            for (int i = 0; i < layerUrls.size(); i++) {
                String layerUrl = layerUrls.get(i);
                String layerName = (layerNames != null && i < layerNames.size())
                    ? layerNames.get(i)
                    : "layer_" + (i + 1);

                // 下载图层图片
                byte[] layerBytes = downloadImageBytes(layerUrl);

                // 从URL中提取文件扩展名
                String mimeType = detectMimeType(layerBytes);
                String extension = getExtensionFromMimeType(mimeType);

                // 创建ZIP条目
                String entryName = layerName + extension;
                ZipEntry zipEntry = new ZipEntry(entryName);
                zos.putNextEntry(zipEntry);
                zos.write(layerBytes);
                zos.closeEntry();

                log.debug("添加图层到ZIP: {} ({} bytes)", entryName, layerBytes.length);
            }

            // 完成ZIP文件
            zos.finish();
            byte[] zipBytes = baos.toByteArray();

            log.info("ZIP打包完成: {} layers, {} bytes", layerUrls.size(), zipBytes.length);

            // 上传ZIP文件
            return uploadImage(zipBytes);
        } catch (Exception e) {
            log.error("打包ZIP文件失败", e);
            throw new RuntimeException("打包ZIP文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据MIME类型获取文件扩展名
     */
    private String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null || mimeType.isEmpty()) {
            return ".png";
        }
        return switch (mimeType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/bmp" -> ".bmp";
            default -> ".png";
        };
    }

    /**
     * 使用AI进行图层分离
     */
    List<byte[]> separateLayersWithAI(String imageUrl, int targetCount) throws Exception {
        log.info("使用AI进行图层分离: targetCount={}", targetCount);

        try {
            String accessToken = getVertexAccessToken();

            // 下载图片
            byte[] imageBytes = downloadImageBytes(imageUrl);
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = detectMimeType(imageBytes);

            // 使用gemini-2.5-flash进行视觉理解，识别图层
            String model = "gemini-2.5-flash";
            String url = buildVertexAiUrl(model);

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            content.put("role", "user");

            JSONArray parts = new JSONArray();

            // 添加文本提示词
            JSONObject textPart = new JSONObject();
            textPart.put("text", "请分析这张图片中的图层结构。以JSON格式返回图层信息：\n"
                    + "1. 返回一个JSON数组，每个元素代表一个图层\n"
                    + "2. 每个元素包含：name(图层名称), type(图层类型：background/product/text/decorative等), "
                    + "description(图层描述)\n"
                    + "3. 只返回JSON数组，不要其他说明\n"
                    + "示例：[\"background\",\"product\",\"text\"]");
            parts.add(textPart);

            // 添加图片
            JSONObject imagePart = new JSONObject();
            JSONObject inlineData = new JSONObject();
            inlineData.put("mimeType", mimeType);
            inlineData.put("data", base64);
            imagePart.put("inlineData", inlineData);
            parts.add(imagePart);

            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            // 文本生成配置
            JSONObject generationConfig = new JSONObject();
            JSONArray responseModalities = new JSONArray();
            responseModalities.add("TEXT");
            generationConfig.put("responseModalities", responseModalities);
            requestBody.put("generationConfig", generationConfig);

            log.info("调用 Vertex AI 分析图层结构, 模型: {}", model);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toJSONString(), java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = getVertexHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("图层分析失败, 状态码: {}", response.statusCode());
                return null;
            }

            // 解析图层信息
            JSONObject respJson = JSON.parseObject(response.body());
            JSONArray candidates = respJson.getJSONArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return null;
            }

            // 提取文本
            StringBuilder fullText = new StringBuilder();
            JSONObject candidate = candidates.getJSONObject(0);
            JSONObject contentObj = candidate.getJSONObject("content");
            if (contentObj != null) {
                JSONArray partsArr = contentObj.getJSONArray("parts");
                if (partsArr != null) {
                    for (int i = 0; i < partsArr.size(); i++) {
                        JSONObject part = partsArr.getJSONObject(i);
                        if (part.containsKey("text")) {
                            fullText.append(part.getString("text"));
                        }
                    }
                }
            }

            // 解析图层列表
            String responseText = fullText.toString().trim();
            log.info("图层分析结果: {}", responseText);

            // 清理markdown标记
            String jsonStr = responseText;
            if (responseText.startsWith("```json")) {
                jsonStr = responseText.substring(7);
            } else if (responseText.startsWith("```")) {
                jsonStr = responseText.substring(3);
            }
            if (jsonStr.endsWith("```")) {
                jsonStr = jsonStr.substring(0, jsonStr.length() - 3);
            }
            jsonStr = jsonStr.trim();

            // 解析JSON数组
            Object parsed = JSON.parse(jsonStr);
            List<byte[]> layerImages = new ArrayList<>();

            if (parsed instanceof JSONArray) {
                JSONArray layers = (JSONArray) parsed;
                log.info("识别到{}个图层", layers.size());

                // 为每个图层生成分离后的图片
                for (int i = 0; i < Math.min(layers.size(), targetCount); i++) {
                    Object layerObj = layers.get(i);
                    String layerName = layerObj.toString();

                    log.info("分离图层[{}]: {}", i, layerName);

                    // 使用AI重新生成该图层
                    byte[] layerImage = generateLayerImage(imageUrl, layerName, i, targetCount);
                    if (layerImage != null) {
                        layerImages.add(layerImage);
                    }
                }
            }

            // 如果AI分离失败，至少返回原图
            if (layerImages.isEmpty()) {
                layerImages.add(imageBytes);
            }

            return layerImages;

        } catch (Exception e) {
            log.error("AI图层分离失败", e);
            return null;
        }
    }

    /**
     * 生成单个图层的图片
     */
    private byte[] generateLayerImage(String imageUrl, String layerName, int index, int totalLayers) throws Exception {
        try {
            String accessToken = getVertexAccessToken();

            // 下载原图
            byte[] imageBytes = downloadImageBytes(imageUrl);
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = detectMimeType(imageBytes);

            // 使用图片生成模型生成该图层
            String model = vertexModel;
            String url = buildVertexAiUrl(model);

            // 构建提示词
            String prompt = String.format(
                "Extract and regenerate only the %s layer from this image. "
                + "Layer %d of %d. "
                + "Remove all other layers and elements. "
                + "Keep the extracted layer's visual style, colors, and quality. "
                + "Generate a clean isolated version of this layer with transparent or white background.",
                layerName, index + 1, totalLayers
            );

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            content.put("role", "user");

            JSONArray parts = new JSONArray();

            // 添加文本prompt
            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);
            parts.add(textPart);

            // 添加原图
            JSONObject imagePart = new JSONObject();
            JSONObject inlineData = new JSONObject();
            inlineData.put("mimeType", mimeType);
            inlineData.put("data", base64);
            imagePart.put("inlineData", inlineData);
            parts.add(imagePart);

            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            // 生成配置
            JSONObject generationConfig = new JSONObject();
            JSONArray responseModalities = new JSONArray();
            responseModalities.add("TEXT");
            responseModalities.add("IMAGE");
            generationConfig.put("responseModalities", responseModalities);

            JSONObject imageConfig = new JSONObject();
            imageConfig.put("aspectRatio", "1:1");
            imageConfig.put("imageSize", "1K");
            generationConfig.put("imageConfig", imageConfig);

            requestBody.put("generationConfig", generationConfig);

            log.info("生成图层[{}]: {}, 模型: {}", index, layerName, model);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(vertexReadTimeout))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toJSONString(), java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = getVertexHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("生成图层失败, 状态码: {}", response.statusCode());
                return null;
            }

            // 提取图片
            List<String> images = extractImagesFromResponse(response.body());
            if (images != null && !images.isEmpty()) {
                String dataUri = images.get(0);
                String base64Data = dataUri.substring(dataUri.indexOf(",") + 1);
                return Base64.getDecoder().decode(base64Data);
            }

            return null;

        } catch (Exception e) {
            log.error("生成图层失败: {}", layerName, e);
            return null;
        }
    }

    // ============================================
    // 7. OCR文字识别
    // ============================================
    public List<Map<String, Object>> recognizeText(String imageUrl) throws Exception {
        log.info("执行OCR识别: imageUrl={}", imageUrl);

        // 使用视觉模型识别图片中的文字
        return callOcrModel(imageUrl);
    }

    /**
     * 调用视觉模型识别图片中的文字
     * 使用 gemini-2.5-flash 等支持视觉+文本的模型
     */
    List<Map<String, Object>> callOcrModel(String imageUrl) throws Exception {
        log.info("调用OCR模型: {}", imageUrl);

        try {
            String accessToken = getVertexAccessToken();

            // 下载并压缩图片
            byte[] imageBytes = downloadImageBytes(imageUrl);
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = detectMimeType(imageBytes);

            // 使用 gemini-2.5-flash 进行视觉理解
            String ocrModel = "gemini-2.5-flash";
            String url = buildVertexAiUrl(ocrModel);

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            content.put("role", "user");

            JSONArray parts = new JSONArray();

            // 添加文本提示词
            JSONObject textPart = new JSONObject();
            textPart.put("text", "请识别这张图片中的所有文字，并以JSON格式返回。要求：\n"
                    + "1. 返回一个JSON数组，每个元素代表一个文字区域\n"
                    + "2. 每个元素包含：text(文字内容), confidence(置信度0-1), position({x, y}), size({width, height})\n"
                    + "3. position和size请使用相对坐标（0-1000范围）\n"
                    + "4. 只返回JSON，不要其他说明\n"
                    + "示例格式：\n"
                    + "[{\"text\":\"Hello\",\"confidence\":0.95,\"position\":{\"x\":100,\"y\":200},\"size\":{\"width\":150,\"height\":50}}]");
            parts.add(textPart);

            // 添加图片
            JSONObject imagePart = new JSONObject();
            JSONObject inlineData = new JSONObject();
            inlineData.put("mimeType", mimeType);
            inlineData.put("data", base64);
            imagePart.put("inlineData", inlineData);
            parts.add(imagePart);

            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            // 文本生成配置 - 只需要文本输出
            JSONObject generationConfig = new JSONObject();
            JSONArray responseModalities = new JSONArray();
            responseModalities.add("TEXT");
            generationConfig.put("responseModalities", responseModalities);
            requestBody.put("generationConfig", generationConfig);

            log.info("调用 Vertex AI OCR, 模型: {}, 图片大小: {} bytes", ocrModel, imageBytes.length);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(vertexReadTimeout))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toJSONString(), java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = getVertexHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            log.info("Vertex AI OCR 响应状态: {}", response.statusCode());

            if (response.statusCode() != 200) {
                log.error("Vertex AI OCR 调用失败, 状态码: {}, 响应: {}", response.statusCode(), response.body());
                throw new RuntimeException("OCR识别失败: HTTP " + response.statusCode());
            }

            // 解析响应，提取文字信息
            return parseOcrResponse(response.body());

        } catch (Exception e) {
            log.error("OCR识别异常", e);
            throw new RuntimeException("OCR识别异常: " + e.getMessage(), e);
        }
    }

    /**
     * 解析OCR响应，提取文字和位置信息
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseOcrResponse(String responseBody) throws Exception {
        List<Map<String, Object>> texts = new ArrayList<>();

        try {
            JSONObject respJson = JSON.parseObject(responseBody);
            JSONArray candidates = respJson.getJSONArray("candidates");

            if (candidates == null || candidates.isEmpty()) {
                log.warn("OCR响应中没有candidates");
                return texts;
            }

            // 提取文本内容
            StringBuilder fullText = new StringBuilder();
            JSONObject candidate = candidates.getJSONObject(0);
            JSONObject content = candidate.getJSONObject("content");
            if (content != null) {
                JSONArray parts = content.getJSONArray("parts");
                if (parts != null) {
                    for (int i = 0; i < parts.size(); i++) {
                        JSONObject part = parts.getJSONObject(i);
                        if (part.containsKey("text")) {
                            String text = part.getString("text");
                            fullText.append(text);
                        }
                    }
                }
            }

            // 尝试解析JSON格式的文字列表
            String responseText = fullText.toString().trim();
            log.info("OCR返回文本: {}", responseText);

            // 提取JSON数组（可能被```json包裹）
            String jsonStr = responseText;
            if (responseText.startsWith("```json")) {
                jsonStr = responseText.substring(7);
            } else if (responseText.startsWith("```")) {
                jsonStr = responseText.substring(3);
            }
            if (jsonStr.endsWith("```")) {
                jsonStr = jsonStr.substring(0, jsonStr.length() - 3);
            }
            jsonStr = jsonStr.trim();

            // 解析JSON
            Object parsed = JSON.parse(jsonStr);
            if (parsed instanceof JSONArray) {
                JSONArray textArray = (JSONArray) parsed;
                for (int i = 0; i < textArray.size(); i++) {
                    Object item = textArray.get(i);
                    if (item instanceof JSONObject) {
                        JSONObject textObj = (JSONObject) item;
                        Map<String, Object> textMap = new HashMap<>();
                        textMap.put("text", textObj.getString("text"));
                        textMap.put("confidence", textObj.getDouble("confidence"));
                        textMap.put("position", textObj.getJSONObject("position"));
                        textMap.put("size", textObj.getJSONObject("size"));
                        texts.add(textMap);
                    }
                }
            }

            // 如果解析失败，将整个文本作为单个文字块返回
            if (texts.isEmpty() && !responseText.isEmpty()) {
                Map<String, Object> text = new HashMap<>();
                text.put("text", responseText);
                text.put("confidence", 0.9);

                Map<String, Object> position = new HashMap<>();
                position.put("x", 0);
                position.put("y", 0);
                text.put("position", position);

                Map<String, Object> size = new HashMap<>();
                size.put("width", 200);
                size.put("height", 50);
                text.put("size", size);

                texts.add(text);
            }

            log.info("OCR识别成功，提取到{}个文字区域", texts.size());
            return texts;

        } catch (Exception e) {
            log.error("解析OCR响应失败: {}", e.getMessage(), e);
            // 返回空列表而不是抛出异常，保证服务可用性
            return texts;
        }
    }

    // ============================================
    // 8. 编辑历史
    // ============================================
    public List<Map<String, Object>> getEditHistory(String imageId) throws Exception {
        log.info("查询编辑历史: imageId={}", imageId);

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, image_id, user_id, operation, params, result_url, version, create_time "
                + "FROM canvas_edit_history WHERE image_id = ? ORDER BY version DESC",
                imageId
            );

            log.info("查询到{}条编辑历史记录", rows.size());
            return rows;
        } catch (Exception e) {
            log.error("查询编辑历史失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 保存编辑历史记录
     */
    private void saveEditHistory(String imageId, String operation, Map<String, Object> params, String resultUrl) throws Exception {
        try {
            // 如果 imageId 过长（如 base64 编码的图片数据），生成 MD5 哈希作为 imageId
            if (imageId != null && imageId.length() > 255) {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
                byte[] digest = md.digest(imageId.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder("hash_");
                for (byte b : digest) {
                    sb.append(String.format("%02x", b));
                }
                imageId = sb.toString();
            }
            // 获取当前用户ID
            Long userId = getCurrentUserId();

            // 获取下一个版本号
            int version = getNextVersion(imageId);

            // 保存到数据库
            jdbcTemplate.update(
                "INSERT INTO canvas_edit_history (image_id, user_id, operation, params, result_url, version, create_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, NOW())",
                imageId, userId, operation, JSON.toJSONString(params), resultUrl, version
            );

            log.debug("保存编辑历史成功: imageId={}, operation={}, version={}", imageId, operation, version);
        } catch (Exception e) {
            log.error("保存编辑历史失败: imageId={}, operation={}", imageId, operation, e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            log.warn("获取当前用户ID失败，使用默认值0: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * 获取下一个版本号
     */
    private int getNextVersion(String imageId) throws Exception {
        try {
            Integer maxVersion = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(version), 0) FROM canvas_edit_history WHERE image_id = ?",
                Integer.class,
                imageId
            );
            return (maxVersion != null ? maxVersion : 0) + 1;
        } catch (Exception e) {
            log.warn("获取版本号失败，使用默认值1: {}", e.getMessage());
            return 1;
        }
    }

    // ============================================
    // 9. 撤销编辑
    // ============================================
    public Map<String, Object> revertImage(String imageId, int version) throws Exception {
        log.info("执行撤销编辑: imageId={}, version={}", imageId, version);

        try {
            // 从数据库查询该版本的result_url
            Map<String, Object> history = jdbcTemplate.queryForMap(
                "SELECT result_url, operation, params FROM canvas_edit_history WHERE image_id = ? AND version = ?",
                imageId, version
            );

            String resultUrl = (String) history.get("result_url");
            String operation = (String) history.get("operation");

            log.info("撤销编辑成功: imageId={}, version={}, operation={}", imageId, version, operation);

            Map<String, Object> result = new HashMap<>();
            result.put("url", resultUrl);
            result.put("version", version);
            result.put("operation", operation);
            result.put("timestamp", System.currentTimeMillis());

            return result;
        } catch (Exception e) {
            log.error("撤销编辑失败: imageId={}, version={}", imageId, version, e);
            Map<String, Object> result = new HashMap<>();
            result.put("url", "");
            result.put("version", version);
            result.put("error", "未找到指定版本的编辑记录");
            return result;
        }
    }

    /**
     * 调用 Vertex AI Gemini 生成图片
     */
    List<String> callVertexAi(String prompt, List<String> imageUrls, String aspectRatio, String imageSize) throws Exception {
        try {
            String accessToken = getVertexAccessToken();

            // 构建 Vertex AI URL
            String url = buildVertexAiUrl(vertexModel);

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            content.put("role", "user");

            JSONArray parts = new JSONArray();

            // 添加文本prompt
            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);
            parts.add(textPart);

            // 添加参考图片
            if (imageUrls != null && !imageUrls.isEmpty()) {
                for (String imageUrl : imageUrls) {
                    byte[] imageBytes = downloadImageBytes(imageUrl);
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

            // 生成配置
            JSONObject generationConfig = new JSONObject();
            JSONArray responseModalities = new JSONArray();
            responseModalities.add("TEXT");
            responseModalities.add("IMAGE");
            generationConfig.put("responseModalities", responseModalities);

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
            log.info("调用 Vertex AI, 模型: {}, prompt 长度: {}, 参考图片数: {}, aspectRatio: {}, imageSize: {}",
                    vertexModel, prompt.length(), imageUrls != null ? imageUrls.size() : 0, aspectRatio, imageSize);

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
                log.error("Vertex AI 调用失败, 状态码: {}, URL: {}, 响应: {}", response.statusCode(), url, responseBody);
                throw new RuntimeException("Vertex AI 调用失败: HTTP " + response.statusCode());
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
     */
    private List<String> extractImagesFromResponse(String responseBody) {
        List<String> dataUriImages = new ArrayList<>();
        try {
            JSONObject respJson = JSON.parseObject(responseBody);
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

                        byte[] imageBytes;
                        try {
                            imageBytes = Base64.getMimeDecoder().decode(data);
                        } catch (IllegalArgumentException de) {
                            log.error("Vertex AI 返回的 base64 无法解码, 长度: {}", data.length());
                            continue;
                        }

                        try {
                            validateImageBytes(imageBytes, "vertex-ai-response");
                        } catch (RuntimeException ve) {
                            log.error("Vertex AI 返回图片字节校验失败: {}", ve.getMessage());
                            continue;
                        }

                        String realMime = detectMimeType(imageBytes);
                        if (mimeType != null && !mimeType.isEmpty() && !mimeType.equals(realMime)) {
                            log.warn("Vertex AI 声明 mimeType: {} 与文件头实际类型不符, 实际: {}", mimeType, realMime);
                        }

                        // 上传图片到文件存储，返回真实URL（避免 Data URI 过大导致前端无法显示和数据库存储失败）
                        String imageUrl;
                        try {
                            imageUrl = imageUploadService.uploadImage(imageBytes, realMime);
                        } catch (Exception uploadEx) {
                            log.error("图片上传失败, 回退为 Data URI, 字节数: {}", imageBytes.length, uploadEx);
                            imageUrl = "data:" + realMime + ";base64," + data;
                        }
                        dataUriImages.add(imageUrl);
                        log.info("提取到图片, base64长度: {}, 解码字节数: {}, 上传后URL: {}", data.length(), imageBytes.length, imageUrl);
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

    /**
     * 构建 Vertex AI API URL
     */
    private String buildVertexAiUrl(String model) {
        String host;
        if ("global".equalsIgnoreCase(vertexLocation)) {
            host = "aiplatform.googleapis.com";
        } else {
            host = vertexLocation + "-aiplatform.googleapis.com";
        }
        String url = String.format(
                "https://%s/v1beta1/projects/%s/locations/%s/publishers/google/models/%s:generateContent",
                host, vertexProjectId, vertexLocation, model);
        log.info("Vertex AI 端点: {}", url);
        return url;
    }

    /**
     * 获取 Vertex AI Access Token
     * 参考 CustomerAiImageService.java 实现
     */
    private String getVertexAccessToken() throws Exception {
        String oldHttpsProxyHost = System.getProperty("https.proxyHost");
        String oldHttpsProxyPort = System.getProperty("https.proxyPort");

        try {
            // 设置代理
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
            // 恢复原来的代理设置
            if (oldHttpsProxyHost != null) {
                System.setProperty("https.proxyHost", oldHttpsProxyHost);
            } else {
                System.clearProperty("https.proxyHost");
            }
            if (oldHttpsProxyPort != null) {
                System.setProperty("https.proxyPort", String.valueOf(oldHttpsProxyPort));
            } else {
                System.clearProperty("https.proxyPort");
            }
        }
    }

    /**
     * 下载图片（支持URL、Base64和/profile/相对路径）
     */
    byte[] downloadImageBytes(String imageUrl) throws Exception {
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new RuntimeException("图片URL不能为空");
        }

        // 1. data URL
        if (imageUrl.startsWith("data:")) {
            String base64Part = imageUrl.substring(imageUrl.indexOf(",") + 1);
            byte[] bytes = Base64.getDecoder().decode(base64Part);
            validateImageBytes(bytes, imageUrl);
            return compressImage(bytes);
        }

        // 2. /profile/ 相对路径（本地上传的图片，直接从文件系统读取）
        if (imageUrl.startsWith("/profile/")) {
            String relativePath = imageUrl.substring("/profile/".length());
            java.io.File file = new java.io.File(ruoYiConfig.getProfile(), relativePath);
            if (!file.exists()) {
                throw new RuntimeException("图片文件不存在: " + file.getAbsolutePath());
            }
            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            validateImageBytes(bytes, imageUrl);
            return compressImage(bytes);
        }

        // 3. 网络URL
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .timeout(Duration.ofSeconds(30))
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("User-Agent", "Mozilla/5.0 (compatible; GuangheStudio)")
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                throw new RuntimeException("下载图片失败, 状态码: " + response.statusCode());
            }

            byte[] bytes = response.body();
            validateImageBytes(bytes, imageUrl);
            return compressImage(bytes);
        } catch (Exception e) {
            log.error("下载图片异常: {}", imageUrl, e);
            throw new RuntimeException("下载图片失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证图片字节
     */
    private void validateImageBytes(byte[] bytes, String source) {
        if (bytes == null || bytes.length < 12) {
            throw new RuntimeException("图片数据无效(太小): " + source + ", 长度: " + (bytes == null ? 0 : bytes.length));
        }
        boolean valid = (bytes[0] == (byte) 0x89 && bytes[1] == (byte) 0x50 && bytes[2] == (byte) 0x4E && bytes[3] == (byte) 0x47) // PNG
                || (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF) // JPEG
                || (bytes.length > 12 && bytes[0] == (byte) 'R' && bytes[1] == (byte) 'I' && bytes[2] == (byte) 'F' && bytes[3] == (byte) 'F'
                        && bytes[8] == (byte) 'W' && bytes[9] == (byte) 'E' && bytes[10] == (byte) 'B' && bytes[11] == (byte) 'P') // WebP
                || (bytes[0] == (byte) 'G' && bytes[1] == (byte) 'I' && bytes[2] == (byte) 'F' && bytes[3] == (byte) '8'); // GIF
        if (!valid) {
            throw new RuntimeException("图片数据不是有效图片: " + source + ", 长度: " + bytes.length);
        }
    }

    /**
     * 检测图片MIME类型
     */
    private String detectMimeType(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length < 4) {
            return "image/png";
        }

        // PNG
        if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == (byte) 0x50
                && imageBytes[2] == (byte) 0x4E && imageBytes[3] == (byte) 0x47) {
            return "image/png";
        }
        // JPEG
        if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8 && imageBytes[2] == (byte) 0xFF) {
            return "image/jpeg";
        }
        // WebP
        if (imageBytes.length > 12 && imageBytes[0] == (byte) 'R' && imageBytes[1] == (byte) 'I'
                && imageBytes[2] == (byte) 'F' && imageBytes[3] == (byte) 'F'
                && imageBytes[8] == (byte) 'W' && imageBytes[9] == (byte) 'E'
                && imageBytes[10] == (byte) 'B' && imageBytes[11] == (byte) 'P') {
            return "image/webp";
        }
        // GIF
        if (imageBytes[0] == (byte) 'G' && imageBytes[1] == (byte) 'I'
                && imageBytes[2] == (byte) 'F' && imageBytes[3] == (byte) '8') {
            return "image/gif";
        }

        return "image/png"; // 默认
    }

    /**
     * 压缩图片
     * 压缩失败时降级返回原始字节，不影响主流程。
     */
    private byte[] compressImage(byte[] originalBytes) {
        return compressImage(originalBytes, 4 * 1024 * 1024); // 默认4MB限制
    }

    /**
     * 压缩图片到指定限制（以字节为单位），通过逐步缩小尺寸。
     * 压缩失败时降级返回原始字节，不影响主流程。
     */
    private byte[] compressImage(byte[] originalBytes, int maxBytesLimit) {
        if (originalBytes == null || originalBytes.length == 0) {
            return originalBytes;
        }
        // 未超过限制，无需压缩
        if (originalBytes.length <= maxBytesLimit) {
            log.debug("图片 {} bytes 未超过 {} 字节阈值，跳过压缩", originalBytes.length, maxBytesLimit);
            return originalBytes;
        }
        try {
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(originalBytes);
            BufferedImage original = ImageIO.read(bais);
            if (original == null) {
                log.warn("压缩图片: ImageIO.read 返回 null，无法识别图片格式，跳过压缩");
                return originalBytes;
            }

            String mimeType = detectMimeType(originalBytes);
            String formatName;
            if ("image/png".equals(mimeType)) {
                formatName = "png";
            } else if ("image/gif".equals(mimeType)) {
                formatName = "png"; // GIF 转为 PNG 传给 Vertex AI
            } else {
                formatName = "jpg"; // JPEG / WebP / 其他都输出为 JPEG
            }

            int origWidth = original.getWidth();
            int origHeight = original.getHeight();
            byte[] compressed = null;

            // 逐步降低缩放比例，直到压缩后 <= maxBytesLimit 或缩到最小比例 0.1
            for (double scale = 0.9; scale >= 0.1; scale -= 0.1) {
                int newWidth = Math.max(1, (int) (origWidth * scale));
                int newHeight = Math.max(1, (int) (origHeight * scale));

                BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = scaled.createGraphics();
                g.drawImage(original, 0, 0, newWidth, newHeight, null);
                g.dispose();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(scaled, formatName, baos);
                compressed = baos.toByteArray();

                log.info("图片压缩尝试 scale={}: {} bytes -> {} bytes ({}x{} -> {}x{})",
                        scale, originalBytes.length, compressed.length,
                        origWidth, origHeight, newWidth, newHeight);

                if (compressed.length <= maxBytesLimit) {
                    break;
                }
            }

            // 如果所有比例都无法压到限制以内，取最后一次（最小尺寸）的结果
            if (compressed == null || compressed.length == 0) {
                log.warn("压缩图片: 所有缩放比例均失败，使用原始图片");
                return originalBytes;
            }

            if (compressed.length >= originalBytes.length) {
                log.warn("压缩图片: 压缩后 {} bytes 反而比原始 {} bytes 大，使用原始图片",
                        compressed.length, originalBytes.length);
                return originalBytes;
            }

            log.info("图片压缩完成: 原始 {} bytes -> 压缩后 {} bytes ({}x{} -> 最终格式: {})",
                    originalBytes.length, compressed.length, origWidth, origHeight, formatName);
            return compressed;
        } catch (Exception e) {
            log.warn("压缩图片失败，使用原始图片: {}", e.getMessage());
            return originalBytes;
        }
    }

    /**
     * 上传图片
     */
    String uploadImage(byte[] imageBytes) throws Exception {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("图片数据不能为空");
        }

        try {
            String mimeType = detectMimeType(imageBytes);
            return imageUploadService.uploadImage(imageBytes, mimeType);
        } catch (IOException e) {
            log.error("图片上传失败", e);
            throw new RuntimeException("图片上传失败: " + e.getMessage(), e);
        }
    }

    // ============================================
    // 工具方法
    // ============================================

    /**
     * 带重试机制的操作执行器
     *
     * @param operationName 操作名称（用于日志）
     * @param maxRetries    最大重试次数
     * @param operation     要执行的操作
     * @param <T>           返回值类型
     * @return 操作结果
     * @throws Exception 所有重试失败后抛出最后一次异常
     */
    <T> T executeWithRetry(String operationName, int maxRetries, RetryOperation<T> operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("执行操作[{}]，尝试 {}/{}", operationName, attempt, maxRetries);
                T result = operation.execute();
                if (attempt > 1) {
                    log.info("操作[{}]在第{}次尝试后成功", operationName, attempt);
                }
                return result;
            } catch (Exception e) {
                lastException = e;
                log.warn("操作[{}]第{}次尝试失败: {}", operationName, attempt, e.getMessage());

                if (attempt < maxRetries) {
                    // 计算退避时间（指数退避：1s, 2s, 4s, 8s...）
                    long backoffMs = (long) Math.pow(2, attempt - 1) * 1000;
                    log.info("等待 {}ms 后进行第{}次重试...", backoffMs, attempt + 1);
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试等待被中断", ie);
                    }
                }
            }
        }

        log.error("操作[{}]所有{}次尝试均失败", operationName, maxRetries);
        throw new RuntimeException(
            String.format("操作[%s]失败，已重试%d次: %s", operationName, maxRetries, lastException.getMessage()),
            lastException
        );
    }

    /**
     * 函数式接口，用于重试操作
     */
    @FunctionalInterface
    interface RetryOperation<T> {
        T execute() throws Exception;
    }

    /**
     * 判断是否为网络超时错误
     */
    private boolean isTimeoutError(Exception e) {
        String message = e.getMessage();
        return message != null && (
            message.contains("timeout") ||
            message.contains("Timeout") ||
            message.contains("timed out") ||
            e instanceof java.net.http.HttpTimeoutException
        );
    }

    /**
     * 判断是否为HTTP 429错误（限流）
     */
    private boolean isRateLimitError(Exception e) {
        String message = e.getMessage();
        return message != null && message.contains("429");
    }

    /**
     * 判断是否为HTTP 5xx错误（服务端错误）
     */
    private boolean isServerError(Exception e) {
        String message = e.getMessage();
        if (message == null) return false;
        return message.contains("500") || message.contains("502") ||
               message.contains("503") || message.contains("504");
    }

    /**
     * 判断错误是否可重试
     */
    private boolean isRetryableError(Exception e) {
        return isTimeoutError(e) || isRateLimitError(e) || isServerError(e);
    }
}
