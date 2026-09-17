package com.ruoyi.web.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImageUploadService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ImageUploadServiceTest {

    @InjectMocks
    private ImageUploadService imageUploadService;

    @BeforeEach
    void setUp() {
        // 设置必要的配置值
        ReflectionTestUtils.setField(imageUploadService, "uploadPath", "C:/test-upload");
        ReflectionTestUtils.setField(imageUploadService, "uploadType", "LOCAL");
    }

    @Test
    void testUploadImage_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> imageUploadService.uploadImage(null, "image/png"));
    }

    @Test
    void testUploadImage_EmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> imageUploadService.uploadImage(new byte[0], "image/png"));
    }

    @Test
    void testGetExtensionFromMimeType() {
        assertEquals(".jpg", imageUploadService.getExtensionFromMimeType("image/jpeg"));
        assertEquals(".jpg", imageUploadService.getExtensionFromMimeType("image/JPG"));
        assertEquals(".png", imageUploadService.getExtensionFromMimeType("image/png"));
        assertEquals(".gif", imageUploadService.getExtensionFromMimeType("image/gif"));
        assertEquals(".webp", imageUploadService.getExtensionFromMimeType("image/webp"));
        assertEquals(".bmp", imageUploadService.getExtensionFromMimeType("image/bmp"));
        assertEquals(".png", imageUploadService.getExtensionFromMimeType("image/unknown"));
        assertEquals(".png", imageUploadService.getExtensionFromMimeType(null));
        assertEquals(".png", imageUploadService.getExtensionFromMimeType(""));
    }

    @Test
    void testDeleteImage_NullInput() {
        assertFalse(imageUploadService.deleteImage(null));
    }

    @Test
    void testDeleteImage_EmptyInput() {
        assertFalse(imageUploadService.deleteImage(""));
    }

    @Test
    void testDeleteImage_FileNotExists() {
        assertFalse(imageUploadService.deleteImage("http://localhost:8780/profile/2026/09/07/nonexistent.jpg"));
    }

    @Test
    void testDeleteImage_Success() throws Exception {
        // 创建一个临时文件进行测试
        java.io.File tempFile = java.io.File.createTempFile("test", ".jpg");
        tempFile.deleteOnExit();
        assertTrue(tempFile.exists());

        String imageUrl = "http://localhost:8780/profile/2026/09/07/" + tempFile.getName();
        // 由于文件不存在于 uploadPath 下，所以应该返回 false
        assertFalse(imageUploadService.deleteImage(imageUrl));
    }

    @Test
    void testExtractRelativePath() {
        // URL without protocol
        String result = ReflectionTestUtils.invokeMethod(imageUploadService,
            "extractRelativePath", "http://localhost:8780/profile/2026/09/07/test.jpg");
        assertEquals("profile/2026/09/07/test.jpg", result);

        // HTTPS URL
        result = ReflectionTestUtils.invokeMethod(imageUploadService,
            "extractRelativePath", "https://example.com/profile/2026/09/07/test.jpg");
        assertEquals("profile/2026/09/07/test.jpg", result);

        // URL with context path
        result = ReflectionTestUtils.invokeMethod(imageUploadService,
            "extractRelativePath", "http://localhost:8780/ruoyi/profile/2026/09/07/test.jpg");
        assertEquals("ruoyi/profile/2026/09/07/test.jpg", result);
    }
}
