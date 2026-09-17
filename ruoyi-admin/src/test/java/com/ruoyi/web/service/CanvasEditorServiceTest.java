package com.ruoyi.web.service;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.jdbc.core.JdbcTemplate;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CanvasEditorService 单元测试类
 * 验证画布编辑器各项功能的业务逻辑
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class CanvasEditorServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private RuoYiConfig ruoYiConfig;

    @Mock
    private ImageUploadService imageUploadService;

    private CanvasEditorService canvasEditorService;

    @BeforeEach
    public void setUp() throws Exception {
        // Mock SecurityContextHolder 以支持 getCurrentUserId()
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(1L);
        lenient().when(authentication.getPrincipal()).thenReturn(loginUser);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // 手动构造 Spy，确保构造函数参数被正确注入
        canvasEditorService = spy(new CanvasEditorService(ruoYiConfig, jdbcTemplate, imageUploadService));

        ReflectionTestUtils.setField(canvasEditorService, "vertexProjectId", "test-project");
        ReflectionTestUtils.setField(canvasEditorService, "vertexLocation", "global");
        ReflectionTestUtils.setField(canvasEditorService, "vertexModel", "gemini-3-pro-image");
        ReflectionTestUtils.setField(canvasEditorService, "vertexReadTimeout", 30);
        ReflectionTestUtils.setField(canvasEditorService, "httpClient", mockHttpClient);
    }

    @Test
    public void testSaveEditHistory() throws Exception {
        // 准备测试数据
        String imageId = "test-image-123";
        String operation = "extend";
        Map<String, Object> params = Map.of("ratio", "16:9", "width", 1920, "height", 1080);
        String resultUrl = "data:image/png;base64,test123";

        // Mock JdbcTemplate
        doReturn(1).when(jdbcTemplate).update(anyString(), any(), any(), any(), any(), any(), anyInt());
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString())).thenReturn(1);

        // 执行测试
        // 通过反射调用私有方法
        ReflectionTestUtils.invokeMethod(canvasEditorService, "saveEditHistory",
            imageId, operation, params, resultUrl);

        // 验证
        verify(jdbcTemplate, times(1)).update(
            contains("INSERT INTO canvas_edit_history"),
            eq(imageId), any(), eq(operation), any(), eq(resultUrl), anyInt()
        );
    }

    @Test
    public void testGetNextVersion() throws Exception {
        String imageId = "test-image-123";

        // Mock返回最大版本号为5
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString())).thenReturn(5);

        // 通过反射调用私有方法
        int version = (int) ReflectionTestUtils.invokeMethod(canvasEditorService,
            "getNextVersion", imageId);

        assertEquals(6, version);
    }

    @Test
    public void testGetNextVersionWhenNoHistory() throws Exception {
        String imageId = "new-image-456";

        // Mock返回null（无历史记录）
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString())).thenReturn(null);

        // 通过反射调用私有方法
        int version = (int) ReflectionTestUtils.invokeMethod(canvasEditorService,
            "getNextVersion", imageId);

        assertEquals(1, version);
    }

    @Test
    public void testGetEditHistory() throws Exception {
        String imageId = "test-image-789";

        // Mock返回历史记录
        List<Map<String, Object>> mockHistory = List.of(
            Map.of("id", 1, "operation", "extend", "version", 1),
            Map.of("id", 2, "operation", "multi-angle", "version", 2)
        );
        when(jdbcTemplate.queryForList(anyString(), anyString())).thenReturn(mockHistory);

        // 执行测试
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(canvasEditorService,
            "getEditHistory", imageId);

        // 验证
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(jdbcTemplate, times(1)).queryForList(
            contains("SELECT"),
            eq(imageId)
        );
    }

    @Test
    public void testRevertImageSuccess() throws Exception {
        String imageId = "test-revert-123";
        int version = 2;

        // Mock返回历史记录
        Map<String, Object> mockHistory = Map.of(
            "result_url", "data:image/png;base64,reverted123",
            "operation", "extend",
            "params", "{}"
        );
        when(jdbcTemplate.queryForMap(anyString(), anyString(), anyInt())).thenReturn(mockHistory);

        // 执行测试
        Map<String, Object> result = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "revertImage", imageId, version);

        // 验证
        assertNotNull(result);
        assertEquals("data:image/png;base64,reverted123", result.get("url"));
        assertEquals(version, result.get("version"));
        assertEquals("extend", result.get("operation"));
    }

    @Test
    public void testRevertImageNotFound() throws Exception {
        String imageId = "test-notfound-456";
        int version = 999;

        // Mock抛出异常（记录不存在）
        when(jdbcTemplate.queryForMap(anyString(), anyString(), anyInt()))
            .thenThrow(new RuntimeException("记录不存在"));

        // 执行测试
        Map<String, Object> result = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "revertImage", imageId, version);

        // 验证
        assertNotNull(result);
        assertEquals("", result.get("url"));
        assertTrue(result.containsKey("error"));
    }

    @Test
    public void testDetectMimeType() {
        // PNG
        byte[] pngBytes = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        assertEquals("image/png", ReflectionTestUtils.invokeMethod(canvasEditorService,
            "detectMimeType", pngBytes));

        // JPEG
        byte[] jpegBytes = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
        assertEquals("image/jpeg", ReflectionTestUtils.invokeMethod(canvasEditorService,
            "detectMimeType", jpegBytes));

        // Default (unknown)
        byte[] unknownBytes = new byte[]{0x00, 0x01, 0x02, 0x03};
        assertEquals("image/png", ReflectionTestUtils.invokeMethod(canvasEditorService,
            "detectMimeType", unknownBytes));
    }

    @Test
    public void testValidateImageBytes() {
        // Valid PNG (minimum 12 bytes required by validateImageBytes)
        byte[] validPng = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44};
        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(canvasEditorService,
            "validateImageBytes", validPng, "test"));

        // Invalid (too small - less than 12 bytes)
        byte[] tooSmall = new byte[]{0x00, 0x01};
        assertThrows(RuntimeException.class, () -> ReflectionTestUtils.invokeMethod(canvasEditorService,
            "validateImageBytes", tooSmall, "test"));

        // Null
        assertThrows(RuntimeException.class, () -> ReflectionTestUtils.invokeMethod(canvasEditorService,
            "validateImageBytes", (byte[]) null, "test"));
    }

    @Test
    public void testCompressImageNullInput() {
        byte[] result = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "compressImage", (byte[]) null);
        assertNull(result);
    }

    @Test
    public void testCompressImageEmptyInput() {
        byte[] result = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "compressImage", new byte[0]);
        assertEquals(0, result.length);
    }

    @Test
    public void testCompressImageSmallImage() {
        byte[] smallImage = new byte[100]; // 小于4MB，不应压缩
        byte[] result = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "compressImage", smallImage);
        assertArrayEquals(smallImage, result);
    }

    @Test
    public void testBuildExtendPrompt() {
        String prompt = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "buildExtendPrompt", "16:9", 1920, 1080);

        assertNotNull(prompt);
        assertTrue(prompt.contains("16:9"));
        assertTrue(prompt.contains("1920"));
        assertTrue(prompt.contains("1080"));
    }

    @Test
    public void testBuildExtendPromptWithoutDimensions() {
        String prompt = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "buildExtendPrompt", "1:1", null, null);

        assertNotNull(prompt);
        assertTrue(prompt.contains("1:1"));
        assertFalse(prompt.contains("Target size"));
    }

    @Test
    public void testBuildMultiAnglePromptRotate() {
        String prompt = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "buildMultiAnglePrompt", 4, "rotate");

        assertNotNull(prompt);
        assertTrue(prompt.contains("4"));
        assertTrue(prompt.contains("rotated"));
    }

    @Test
    public void testBuildMultiAnglePromptMultiView() {
        String prompt = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "buildMultiAnglePrompt", 3, "multi-view");

        assertNotNull(prompt);
        assertTrue(prompt.contains("views"));
        assertTrue(prompt.contains("front view"));
    }

    @Test
    public void testBuildMultiAnglePrompt360() {
        String prompt = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "buildMultiAnglePrompt", 8, "360");

        assertNotNull(prompt);
        assertTrue(prompt.contains("360"));
        assertTrue(prompt.contains("frames"));
    }

    @Test
    public void testGetAngleDescriptionsRotate() {
        String[] descriptions = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "getAngleDescriptions", "rotate", 4);

        assertNotNull(descriptions);
        assertEquals(4, descriptions.length);
        assertEquals("0°", descriptions[0]);
        assertEquals("45°", descriptions[1]);
    }

    @Test
    public void testGetAngleDescriptionsMultiView() {
        String[] descriptions = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "getAngleDescriptions", "multi-view", 3);

        assertNotNull(descriptions);
        assertEquals(3, descriptions.length);
    }

    @Test
    public void testGetAngleDescriptions360() {
        String[] descriptions = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "getAngleDescriptions", "360", 4);

        assertNotNull(descriptions);
        assertEquals(4, descriptions.length);
        assertEquals("0°", descriptions[0]);
        assertEquals("90°", descriptions[1]);
        assertEquals("180°", descriptions[2]);
        assertEquals("270°", descriptions[3]);
    }

    @Test
    public void testBuildEditTextPrompt() {
        String prompt = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "buildEditTextPrompt", "Hello", "World", "Arial");

        assertNotNull(prompt);
        assertTrue(prompt.contains("Hello"));
        assertTrue(prompt.contains("World"));
        assertTrue(prompt.contains("Arial"));
    }

    @Test
    public void testBuildEditTextPromptWithoutFont() {
        String prompt = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "buildEditTextPrompt", "Hello", "World", null);

        assertNotNull(prompt);
        assertTrue(prompt.contains("Hello"));
        assertTrue(prompt.contains("World"));
    }

    @Test
    public void testBuildPartialRedrawPromptWithMask() {
        Map<String, Object> mask = Map.of(
            "x", 100, "y", 200, "width", 300, "height", 400
        );

        String prompt = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "buildPartialRedrawPrompt", "make it red", mask);

        assertNotNull(prompt);
        assertTrue(prompt.contains("make it red"));
        assertTrue(prompt.contains("100"));
        assertTrue(prompt.contains("200"));
    }

    @Test
    public void testBuildPartialRedrawPromptWithoutMask() {
        String prompt = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "buildPartialRedrawPrompt", "remove background", null);

        assertNotNull(prompt);
        assertTrue(prompt.contains("remove background"));
    }

    @Test
    public void testExecuteWithRetrySuccess() throws Exception {
        // 第一次失败，第二次成功
        CanvasEditorService.RetryOperation<Object> mockOperation = mock(CanvasEditorService.RetryOperation.class);
        doThrow(new RuntimeException("First attempt failed"))
            .doReturn(null)
            .when(mockOperation).execute();

        Object result = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "executeWithRetry", "testOperation", 3, mockOperation);

        // 验证操作被执行了2次
        verify(mockOperation, times(2)).execute();
    }

    @Test
    public void testExecuteWithRetryAllFailures() throws Exception {
        // 所有尝试都失败
        CanvasEditorService.RetryOperation<Object> mockOperation = mock(CanvasEditorService.RetryOperation.class);
        doThrow(new RuntimeException("Always fails")).when(mockOperation).execute();

        // 应该抛出异常
        assertThrows(RuntimeException.class, () ->
            ReflectionTestUtils.invokeMethod(canvasEditorService,
                "executeWithRetry", "testOperation", 3, mockOperation)
        );

        // 验证操作被执行了3次
        verify(mockOperation, times(3)).execute();
    }

    @Test
    public void testIsTimeoutError() {
        Exception timeoutException = new RuntimeException("Connection timeout");
        assertEquals(true, ReflectionTestUtils.invokeMethod(canvasEditorService,
            "isTimeoutError", timeoutException));

        Exception normalException = new RuntimeException("Connection refused");
        assertEquals(false, ReflectionTestUtils.invokeMethod(canvasEditorService,
            "isTimeoutError", normalException));
    }

    @Test
    public void testIsRateLimitError() {
        Exception rateLimitException = new RuntimeException("HTTP 429 Too Many Requests");
        assertEquals(true, ReflectionTestUtils.invokeMethod(canvasEditorService,
            "isRateLimitError", rateLimitException));

        Exception normalException = new RuntimeException("HTTP 200 OK");
        assertEquals(false, ReflectionTestUtils.invokeMethod(canvasEditorService,
            "isRateLimitError", normalException));
    }

    @Test
    public void testIsServerError() {
        Exception serverError500 = new RuntimeException("HTTP 500 Internal Server Error");
        assertEquals(true, ReflectionTestUtils.invokeMethod(canvasEditorService,
            "isServerError", serverError500));

        Exception serverError503 = new RuntimeException("HTTP 503 Service Unavailable");
        assertEquals(true, ReflectionTestUtils.invokeMethod(canvasEditorService,
            "isServerError", serverError503));

        Exception normalException = new RuntimeException("HTTP 400 Bad Request");
        assertEquals(false, ReflectionTestUtils.invokeMethod(canvasEditorService,
            "isServerError", normalException));
    }

    // ============================================
    // 集成测试：模拟完整的 API 调用链路
    // ============================================

    /**
     * 模拟 extendImage 完整流程：
     * 下载图片 → Vertex AI 扩图 → 保存编辑历史 → 返回结果
     */
    @Test
    public void testExtendImageIntegration() throws Exception {
        String imageUrl = "https://example.com/product.jpg";
        String ratio = "16:9";
        int width = 1920;
        int height = 1080;

        // Mock 下载图片
        byte[] fakeImageBytes = createFakePngBytes(100);
        doReturn(fakeImageBytes).when(canvasEditorService).downloadImageBytes(anyString());

        // Mock Vertex AI 返回扩图结果
        String generatedImage = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        doReturn(List.of(generatedImage)).when(canvasEditorService).callVertexAi(anyString(), anyList(), anyString(), anyString());

        // Mock 数据库操作
        doReturn(1).when(jdbcTemplate).update(anyString(), any(), any(), any(), any(), any(), anyInt());
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any())).thenReturn(1);

        // 通过反射调用私有方法（因为方法本身是 private）
        Map<String, Object> result = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "extendImage", imageUrl, ratio, width, height);

        assertNotNull(result);
        assertEquals(generatedImage, result.get("url"));
        assertEquals(ratio, result.get("ratio"));
        assertEquals(width, result.get("width"));
        assertEquals(height, result.get("height"));
        verify(canvasEditorService, times(1)).downloadImageBytes(eq(imageUrl));
        verify(canvasEditorService, times(1)).callVertexAi(anyString(), anyList(), eq(ratio), anyString());
        verify(jdbcTemplate, times(1)).update(anyString(), any(), any(), any(), any(), any(), anyInt());
    }

    /**
     * 模拟 generateMultiAngle 完整流程
     */
    @Test
    public void testGenerateMultiAngleIntegration() throws Exception {
        String imageUrl = "https://example.com/product.jpg";
        int count = 4;
        String type = "rotate";

        doReturn(createFakePngBytes(100)).when(canvasEditorService).downloadImageBytes(anyString());
        doReturn(List.of(
            "data:image/png;base64,img1",
            "data:image/png;base64,img2",
            "data:image/png;base64,img3",
            "data:image/png;base64,img4"
        )).when(canvasEditorService).callVertexAi(anyString(), anyList(), anyString(), anyString());
        doReturn(1).when(jdbcTemplate).update(anyString(), any(), any(), any(), any(), any(), anyInt());
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString())).thenReturn(1);

        List<Map<String, Object>> result = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "generateMultiAngle", imageUrl, count, type);

        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals("data:image/png;base64,img1", result.get(0).get("url"));
        assertEquals("0°", result.get(0).get("angle"));
    }

    /**
     * 模拟 editImageText 完整流程
     */
    @Test
    public void testEditImageTextIntegration() throws Exception {
        String imageUrl = "https://example.com/ad.jpg";
        String originalText = "旧文字";
        String newText = "新文字";
        String font = "Arial";

        doReturn(createFakePngBytes(100)).when(canvasEditorService).downloadImageBytes(anyString());
        doReturn(List.of("data:image/png;base64,result")).when(canvasEditorService).callVertexAi(anyString(), anyList(), anyString(), anyString());
        doReturn(1).when(jdbcTemplate).update(anyString(), any(), any(), any(), any(), any(), anyInt());
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString())).thenReturn(1);

        Map<String, Object> result = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "editImageText", imageUrl, originalText, newText, font);

        assertNotNull(result);
        assertEquals("data:image/png;base64,result", result.get("url"));
        verify(jdbcTemplate, times(1)).update(contains("INSERT INTO canvas_edit_history"), eq(imageUrl), any(), any(), any(), any(), anyInt());
    }

    /**
     * 模拟 partialRedraw 完整流程（含 Mask 可视化）
     */
    @Test
    public void testPartialRedrawIntegration() throws Exception {
        String imageUrl = "https://example.com/product.jpg";
        String description = "把文字改成红色";
        Map<String, Object> mask = new HashMap<>();
        mask.put("x", 100);
        mask.put("y", 200);
        mask.put("width", 300);
        mask.put("height", 400);

        byte[] originalImage = createFakePngBytes(200);
        doReturn(originalImage).when(canvasEditorService).downloadImageBytes(anyString());

        // applyMaskToImage 应该返回标记后的图片（略小于原图因为画了边框）
        doAnswer(invocation -> {
            byte[] input = invocation.getArgument(0);
            return input; // 简化返回，验证调用链即可
        }).when(canvasEditorService).applyMaskToImage(any(byte[].class), any(Map.class));

        doReturn(List.of("data:image/png;base64,redrawn")).when(canvasEditorService).callVertexAi(anyString(), anyList(), anyString(), anyString());
        doReturn(1).when(jdbcTemplate).update(anyString(), any(), any(), any(), any(), any(), anyInt());
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString())).thenReturn(1);

        Map<String, Object> result = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "partialRedraw", imageUrl, description, mask);

        assertNotNull(result);
        assertEquals("data:image/png;base64,redrawn", result.get("url"));
        assertEquals(description, result.get("description"));
        verify(canvasEditorService, times(1)).downloadImageBytes(eq(imageUrl));
        verify(canvasEditorService, times(1)).applyMaskToImage(any(byte[].class), eq(mask));
    }

    /**
     * 模拟 detectLayers 完整流程
     */
    @Test
    public void testDetectLayersIntegration() throws Exception {
        String imageUrl = "https://example.com/ad.jpg";

        doReturn(List.of(
            Map.of("name", "背景", "type", "background", "x", 0, "y", 0, "width", 1080, "height", 1080, "confidence", 0.95),
            Map.of("name", "产品", "type", "product", "x", 400, "y", 300, "width", 300, "height", 400, "confidence", 0.92)
        )).when(canvasEditorService).detectLayersWithAI(anyString());

        List<Map<String, Object>> result = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "detectLayers", imageUrl);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("背景", result.get(0).get("name"));
        assertEquals("产品", result.get(1).get("name"));
        verify(canvasEditorService, times(1)).detectLayersWithAI(anyString());
    }

    /**
     * 模拟 downloadLayers 完整流程（含 ZIP 打包）
     */
    @Test
    public void testDownloadLayersIntegration() throws Exception {
        String imageUrl = "https://example.com/ad.jpg";
        List<String> layers = List.of("背景", "产品", "文字");

        doReturn(createFakePngBytes(100)).when(canvasEditorService).downloadImageBytes(anyString());
        doReturn(List.of(
            createFakePngBytes(50),
            createFakePngBytes(60),
            createFakePngBytes(70)
        )).when(canvasEditorService).separateLayersWithAI(anyString(), anyInt());
        doReturn("http://localhost:8780/profile/2026/09/07/layers.zip").when(canvasEditorService).packLayersToZip(Mockito.<String>anyList(), Mockito.<String>anyList());
        doReturn("http://localhost:8780/profile/2026/09/07/layers.zip")
            .when(canvasEditorService).uploadImage(any(byte[].class));

        Map<String, Object> result = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "downloadLayers", imageUrl, layers, "png");

        assertNotNull(result);
        assertEquals("http://localhost:8780/profile/2026/09/07/layers.zip", result.get("downloadUrl"));
        assertEquals("png", result.get("format"));
        assertEquals(3, result.get("count"));
        verify(canvasEditorService, times(1)).separateLayersWithAI(anyString(), eq(layers.size()));
        verify(canvasEditorService, times(1)).packLayersToZip(Mockito.<String>anyList(), eq(layers));
    }

    /**
     * 模拟 recognizeText (OCR) 完整流程
     */
    @Test
    public void testRecognizeTextIntegration() throws Exception {
        String imageUrl = "https://example.com/ad.jpg";

        doReturn(List.of(
            Map.of("text", "Hello", "x", 10, "y", 20, "width", 100, "height", 30, "confidence", 0.98),
            Map.of("text", "World", "x", 50, "y", 60, "width", 80, "height", 25, "confidence", 0.95)
        )).when(canvasEditorService).callOcrModel(anyString());

        List<Map<String, Object>> result = ReflectionTestUtils.invokeMethod(canvasEditorService,
            "recognizeText", imageUrl);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Hello", result.get(0).get("text"));
        assertEquals("World", result.get(1).get("text"));
    }

    // ============================================
    // 辅助方法
    // ============================================

    /**
     * 构造最小有效 PNG 文件头（用于测试占位）
     */
    private byte[] createFakePngBytes(int size) {
        byte[] bytes = new byte[8 + size];
        // PNG magic bytes
        bytes[0] = (byte) 0x89;
        bytes[1] = 0x50;
        bytes[2] = 0x4E;
        bytes[3] = 0x47;
        bytes[4] = 0x0D;
        bytes[5] = 0x0A;
        bytes[6] = 0x1A;
        bytes[7] = 0x0A;
        // 填充剩余
        for (int i = 8; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }
        return bytes;
    }
}
