package com.ruoyi.web.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * LightCC API 测试类
 * 用于验证 API key 和 base URL 是否有效
 *
 * 该测试需要启动完整 Spring 上下文（依赖数据库/Redis/外部 API 环境），
 * 本机构建时会失败，默认禁用；如需手动验证可在 IDE 中临时移除 @Disabled。
 */
@Disabled("依赖完整运行环境(数据库/Redis/外部API)，构建时跳过")
@SpringBootTest
public class LightCCApiTest {

    @Value("${spring.ai.lightcc.base-url:}")
    private String lightccImageBaseUrl;

    @Value("${spring.ai.lightcc.api-key:}")
    private String lightccImageApiKey;

    @Value("${spring.ai.lightcc.model:nano-banana-2}")
    private String lightccImageModel;

    @Test
    public void testLightCCApiConnection() {
        System.out.println("========== LightCC API 配置信息 ==========");
        System.out.println("Base URL: " + lightccImageBaseUrl);
        System.out.println("API Key: " + (lightccImageApiKey != null ? lightccImageApiKey.substring(0, 10) + "..." : "null"));
        System.out.println("Model: " + lightccImageModel);
        System.out.println("==========================================");

        if (lightccImageBaseUrl == null || lightccImageBaseUrl.isEmpty()) {
            System.out.println("❌ 错误: LightCC Base URL 未配置");
            return;
        }

        if (lightccImageApiKey == null || lightccImageApiKey.isEmpty()) {
            System.out.println("❌ 错误: LightCC API Key 未配置");
            return;
        }

        // 构建端点
        String endpoint = lightccImageBaseUrl.trim();
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        if (!endpoint.endsWith("/v1")) {
            endpoint = endpoint + "/v1/chat/completions";
        } else {
            endpoint = endpoint + "/chat/completions";
        }

        System.out.println("测试端点: " + endpoint);

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + lightccImageApiKey);

            // 构建简单的测试请求
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", lightccImageModel);
            requestBody.put("max_tokens", 10);

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");

            Map<String, Object> content = new HashMap<>();
            content.put("type", "text");
            content.put("text", "Hello, this is a test.");

            message.put("content", new Object[]{content});
            requestBody.put("messages", new Object[]{message});

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            System.out.println("\n发送测试请求...");
            long startTime = System.currentTimeMillis();

            ResponseEntity<String> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                request,
                String.class
            );

            long duration = System.currentTimeMillis() - startTime;

            System.out.println("✅ 请求成功!");
            System.out.println("状态码: " + response.getStatusCodeValue());
            System.out.println("耗时: " + duration + "ms");
            System.out.println("响应体: " + response.getBody());

        } catch (Exception e) {
            System.out.println("❌ 请求失败!");
            System.out.println("错误信息: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("原因: " + e.getCause().getMessage());
            }
            e.printStackTrace();
        }
    }

    @Test
    public void testLightCCApiWithImage() {
        System.out.println("\n========== 测试图片生成 API ==========");

        if (lightccImageBaseUrl == null || lightccImageBaseUrl.isEmpty() ||
            lightccImageApiKey == null || lightccImageApiKey.isEmpty()) {
            System.out.println("❌ 配置不完整，跳过测试");
            return;
        }

        String endpoint = lightccImageBaseUrl.trim();
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        if (!endpoint.endsWith("/v1")) {
            endpoint = endpoint + "/v1/chat/completions";
        } else {
            endpoint = endpoint + "/chat/completions";
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + lightccImageApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", lightccImageModel);
            requestBody.put("max_tokens", 4096);

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");

            // 添加文本
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", "Please generate a simple red circle on white background.");

            message.put("content", new Object[]{textContent});
            requestBody.put("messages", new Object[]{message});

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            System.out.println("发送图片生成测试请求...");
            long startTime = System.currentTimeMillis();

            ResponseEntity<String> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                request,
                String.class
            );

            long duration = System.currentTimeMillis() - startTime;

            System.out.println("✅ 图片生成 API 测试成功!");
            System.out.println("状态码: " + response.getStatusCodeValue());
            System.out.println("耗时: " + duration + "ms");
            System.out.println("响应体前500字符: " + 
                (response.getBody() != null && response.getBody().length() > 500 
                    ? response.getBody().substring(0, 500) + "..." 
                    : response.getBody()));

        } catch (Exception e) {
            System.out.println("❌ 图片生成 API 测试失败!");
            System.out.println("错误信息: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
