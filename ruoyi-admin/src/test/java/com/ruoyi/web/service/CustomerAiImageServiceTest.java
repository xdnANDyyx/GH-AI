package com.ruoyi.web.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CustomerAiImageService 单元测试类
 * 验证 Vertex AI 在遇到 429 频控/配额耗尽等错误时的多模型自动降级、备用容灾与指数级退避重试机制
 */
public class CustomerAiImageServiceTest {

    @Spy
    @InjectMocks
    private CustomerAiImageService customerAiImageService;

    @Mock
    private HttpClient mockHttpClient;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // 初始化必要的配置属性值
        ReflectionTestUtils.setField(customerAiImageService, "vertexProjectId", "test-project-56a8fdac");
        ReflectionTestUtils.setField(customerAiImageService, "vertexLocation", "global");
        ReflectionTestUtils.setField(customerAiImageService, "vertexReverseModel", "gemini-2.5-flash");
        ReflectionTestUtils.setField(customerAiImageService, "vertexReverseFallbackModels", "gemini-1.5-flash,gemini-1.5-pro,gemini-2.5-pro");
        ReflectionTestUtils.setField(customerAiImageService, "vertexReadTimeout", 30);

        // 注入模拟的 HttpClient
        ReflectionTestUtils.setField(customerAiImageService, "vertexHttpClient", mockHttpClient);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testReversePromptFallbackSuccess() throws Exception {
        // 模拟 token 获取
        doReturn("mock-access-token-12345").when(customerAiImageService).getVertexAccessToken();

        // 模拟 429 响应（频控/配额资源耗尽）
        HttpResponse<String> mockResponse429 = mock(HttpResponse.class);
        when(mockResponse429.statusCode()).thenReturn(429);
        when(mockResponse429.body()).thenReturn("{\n" +
                "  \"error\": {\n" +
                "    \"code\": 429,\n" +
                "    \"message\": \"Resource exhausted. Please try again later.\",\n" +
                "    \"status\": \"RESOURCE_EXHAUSTED\"\n" +
                "  }\n" +
                "}");

        // 模拟 200 成功响应
        HttpResponse<String> mockResponse200 = mock(HttpResponse.class);
        when(mockResponse200.statusCode()).thenReturn(200);
        when(mockResponse200.body()).thenReturn("{\n" +
                "  \"candidates\": [{\n" +
                "    \"content\": {\n" +
                "      \"parts\": [{\n" +
                "        \"text\": \"An e-commerce product image of an elegant wooden chair in a bright modern living room.\"\n" +
                "      }]\n" +
                "    },\n" +
                "    \"finishReason\": \"STOP\"\n" +
                "  }]\n" +
                "}");

        // 设置模拟客户端调用行为：
        // 1. 调用主模型 gemini-2.5-flash (第1次 attempt 1) -> 失败 429
        // 2. 调用主模型 gemini-2.5-flash (第1次 attempt 2，退避重试) -> 失败 429
        // 3. 自动触发降级，调用首个备用模型 gemini-1.5-flash (第2次 attempt 1) -> 成功 200
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse429)  // gemini-2.5-flash attempt 1
                .thenReturn(mockResponse429)  // gemini-2.5-flash attempt 2
                .thenReturn(mockResponse200);  // gemini-1.5-flash attempt 1

        String result = customerAiImageService.reversePrompt("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==", "请反推这张图的提示词");

        assertNotNull(result);
        assertEquals("An e-commerce product image of an elegant wooden chair in a bright modern living room.", result);

        // 验证 HttpClient 调用了 3 次（符合主模型2次重试后，回退首个备用模型成功的机制）
        verify(mockHttpClient, times(3)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }
}
