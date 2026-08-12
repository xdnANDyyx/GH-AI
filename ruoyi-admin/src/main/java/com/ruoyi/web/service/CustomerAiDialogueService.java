package com.ruoyi.web.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * AI 对话服务 — 对接 DeepSeek / Doubao / Qwen-VL 等对话模型
 * 配置信息从 application.yml 读取（spring.ai.deepseek / doubao / qwen-vl）
 */
@Service
public class CustomerAiDialogueService {

    private static final Logger log = LoggerFactory.getLogger(CustomerAiDialogueService.class);

    // ======================== DeepSeek 配置 ========================
    @Value("${spring.ai.deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${spring.ai.deepseek.base-url:}")
    private String deepseekBaseUrl;

    @Value("${spring.ai.deepseek.completions-path:/v1/chat/completions}")
    private String deepseekCompletionsPath;

    @Value("${spring.ai.deepseek.chat.options.model:deepseek-chat}")
    private String deepseekModel;

    @Value("${spring.ai.deepseek.chat.options.temperature:0.7}")
    private double deepseekTemperature;

    // ======================== Doubao 配置 ========================
    @Value("${spring.ai.doubao.api-key:}")
    private String doubaoApiKey;

    @Value("${spring.ai.doubao.base-url:}")
    private String doubaoBaseUrl;

    @Value("${spring.ai.doubao.completions-path:/api/v3/chat/completions}")
    private String doubaoCompletionsPath;

    @Value("${spring.ai.doubao.chat.options.model:doubao-1-5-pro-32k-250115}")
    private String doubaoModel;

    @Value("${spring.ai.doubao.chat.options.temperature:0.7}")
    private double doubaoTemperature;

    // ======================== Qwen-VL 配置 ========================
    @Value("${spring.ai.qwen-vl.api-key:}")
    private String qwenVlApiKey;

    @Value("${spring.ai.qwen-vl.base-url:}")
    private String qwenVlBaseUrl;

    @Value("${spring.ai.qwen-vl.completions-path:/chat/completions}")
    private String qwenVlCompletionsPath;

    @Value("${spring.ai.qwen-vl.chat.options.model:qwen3-vl-plus}")
    private String qwenVlModel;

    @Value("${spring.ai.qwen-vl.chat.options.temperature:0.2}")
    private double qwenVlTemperature;

    // ======================== LightCC 配置 ========================
    @Value("${spring.ai.lightcc.api-key:}")
    private String lightccApiKey;

    @Value("${spring.ai.lightcc.base-url:}")
    private String lightccBaseUrl;

    @Value("${spring.ai.lightcc.model:nano-banana-2}")
    private String lightccModel;

    // ======================== OpenAI 302 配置 ========================
    @Value("${spring.ai.openai302.api-key:}")
    private String openAi302ApiKey;

    @Value("${spring.ai.openai302.base-url:}")
    private String openAi302BaseUrl;

    @Value("${spring.ai.openai302.model:gpt-image-2}")
    private String openAi302Model;

    // 系统提示词（AI 助手角色设定）
    private static final String SYSTEM_PROMPT = "你是一个专业的设计助手，擅长帮助用户优化设计参数、推荐风格搭配、解答设计相关问题。" +
            "你的回答应该简洁、专业、有建设性，使用中文回复。" +
            "你是「光合AI」的设计助手，可以回答关于产品摄影、场景设计、电商视觉等方面的问题。";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    /**
     * 发送对话消息并获取 AI 回复
     *
     * @param messages 历史消息列表（可选）
     * @param content  当前用户输入内容
     * @param model    模型选择：deepseek / doubao / qwen-vl / lightcc / openai302
     * @return AI 回复内容
     */
    public String chat(List<Map<String, String>> messages, String content, String model) {
        if (StringUtils.isEmpty(content)) {
            return "请输入您的问题。";
        }

        // 构建消息列表
        JSONArray requestMessages = new JSONArray();

        // 添加系统提示词
        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", SYSTEM_PROMPT);
        requestMessages.add(systemMsg);

        // 添加历史消息
        if (messages != null) {
            int historyLimit = 10; // 限制历史轮数，避免超出上下文
            int startIdx = Math.max(0, messages.size() - historyLimit);
            for (int i = startIdx; i < messages.size(); i++) {
                Map<String, String> msg = messages.get(i);
                JSONObject historyMsg = new JSONObject();
                historyMsg.put("role", msg.getOrDefault("role", "user"));
                historyMsg.put("content", msg.getOrDefault("content", ""));
                requestMessages.add(historyMsg);
            }
        }

        // 添加当前用户消息
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", content);
        requestMessages.add(userMsg);

        // 根据模型选择调用
        switch (model.toLowerCase()) {
            case "doubao":
                return callChatApi(doubaoBaseUrl, doubaoCompletionsPath, doubaoApiKey,
                        doubaoModel, doubaoTemperature, requestMessages, "Doubao");
            case "qwen-vl":
            case "qwen":
                return callChatApi(qwenVlBaseUrl, qwenVlCompletionsPath, qwenVlApiKey,
                        qwenVlModel, qwenVlTemperature, requestMessages, "Qwen-VL");
            case "lightcc":
                return callChatApi(lightccBaseUrl, "/v1/chat/completions", lightccApiKey,
                        lightccModel, 0.7, requestMessages, "LightCC");
            case "openai302":
                return callChatApi(openAi302BaseUrl, "/v1/chat/completions", openAi302ApiKey,
                        openAi302Model, 0.7, requestMessages, "OpenAI302");
            case "deepseek":
            default:
                return callChatApi(deepseekBaseUrl, deepseekCompletionsPath, deepseekApiKey,
                        deepseekModel, deepseekTemperature, requestMessages, "DeepSeek");
        }
    }

    /**
     * 调用通用的 OpenAI 兼容格式的 Chat API
     */
    private String callChatApi(String baseUrl, String completionsPath, String apiKey,
                               String model, double temperature, JSONArray messages, String providerName) {
        if (StringUtils.isEmpty(apiKey) || StringUtils.isEmpty(baseUrl)) {
            log.warn("[{}] API Key 或 Base URL 未配置，回退到下一个可用模型", providerName);
            return fallbackChat(messages);
        }

        try {
            // 构建请求 URL
            String url = buildUrl(baseUrl, completionsPath);

            // 构建请求体
            JSONObject body = new JSONObject();
            body.put("model", model);
            body.put("messages", messages);
            body.put("temperature", temperature);
            body.put("max_tokens", 2048);

            log.info("[{}] 请求 URL: {}, model: {}", providerName, url, model);

            // 调试：打印请求体（不包含敏感信息）
            log.debug("[{}] 请求体: {}", providerName, body.toJSONString());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("[{}] API 响应状态: {}, 响应体: {}", providerName, response.statusCode(), response.body());

            if (response.statusCode() >= 300) {
                log.error("[{}] API 返回错误: status={}, body={}", providerName, response.statusCode(), response.body());
                
                // 检测是否是token失效错误
                String responseBody = response.body();
                if (response.statusCode() == 401 || 
                    (responseBody != null && responseBody.toLowerCase().contains("invalid token"))) {
                    log.error("[{}] API密钥已失效，请更换有效的API Key", providerName);
                    return "抱歉，" + providerName + "服务的API密钥已失效，请联系管理员更新系统配置。";
                }
                
                return fallbackChat(messages);
            }

            return parseChatResponse(response.body());

        } catch (Exception e) {
            log.error("[{}] 调用失败: {}", providerName, e.getMessage(), e);
            return fallbackChat(messages);
        }
    }

    /**
     * 解析 Chat API 响应，提取回复内容
     */
    private String parseChatResponse(String responseBody) {
        try {
            JSONObject json = JSON.parseObject(responseBody);

            // 检查是否有错误
            if (json.containsKey("error") && json.getJSONObject("error") != null) {
                String errMsg = json.getJSONObject("error").getString("message");
                log.error("API 返回错误: {}", errMsg);
                return "抱歉，AI 服务暂时不可用，请稍后再试。";
            }

            // 提取回复内容
            JSONArray choices = json.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                if (message != null) {
                    String reply = message.getString("content");
                    if (StringUtils.isNotEmpty(reply)) {
                        return reply.trim();
                    }
                }
            }

            log.warn("无法解析 AI 回复: {}", responseBody);
            return "抱歉，我暂时无法理解您的问题，请换个方式描述。";

        } catch (Exception e) {
            log.error("解析 AI 回复失败: {}", e.getMessage(), e);
            return "抱歉，处理回复时出现错误，请稍后再试。";
        }
    }

    /**
     * 回退方案：当所有 AI 模型都不可用时，返回预设回复
     */
    private String fallbackChat(JSONArray messages) {
        // 尝试从消息中提取用户最后的问题，返回带上下文的默认回复
        String lastUserContent = "";
        for (int i = messages.size() - 1; i >= 0; i--) {
            JSONObject msg = messages.getJSONObject(i);
            if ("user".equals(msg.getString("role"))) {
                lastUserContent = msg.getString("content");
                break;
            }
        }

        if (lastUserContent.contains("风格") || lastUserContent.contains("搭配")) {
            return "根据您的需求，建议您尝试「北欧简约」或「日式原木」风格，这两种风格适合大多数家居产品。" +
                    "如果需要更具体的建议，请提供产品类型或参考图片。";
        } else if (lastUserContent.contains("尺寸") || lastUserContent.contains("分辨率")) {
            return "推荐使用 2000x2000px 作为标准输出尺寸，" +
                    "这是各大电商平台推荐的通用尺寸，可以保证图片在各终端的显示效果。";
        } else if (lastUserContent.contains("灯光") || lastUserContent.contains("光线")) {
            return "对于产品拍摄，推荐使用柔和的自然侧光（45度角），" +
                    "这样可以很好地展现产品的材质和纹理细节，避免过强的阴影。";
        } else if (lastUserContent.contains("白底图")) {
            return "白底图的关键要求：1. 背景纯净无杂质；2. 产品边缘清晰无锯齿；" +
                    "3. 光影自然不突兀；4. 建议输出 PNG 格式以保证透明背景质量。";
        } else {
            return "您好！我是光合AI助手，可以帮您优化设计参数、推荐风格搭配、解答使用问题。" +
                    "请告诉我您的具体需求。";
        }
    }

    /**
     * 构建完整的 API URL
     */
    private String buildUrl(String baseUrl, String path) {
        String url = baseUrl.trim();
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        String p = path.trim();
        while (p.startsWith("/")) {
            p = p.substring(1);
        }
        return url + "/" + p;
    }
}