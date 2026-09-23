package com.ruoyi.web.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 图片上传服务
 * 支持本地存储，可扩展支持MinIO/OSS
 */
@Slf4j
@Service
public class ImageUploadService {

    @Value("${ruoyi.profile:C:/GuangHeXDN/guanghe-server-xdn/uploadPath}")
    private String uploadPath;

    @Value("${customer.image-upload.type:LOCAL}")
    private String uploadType;

    private static final String CANVAS_EDITOR_PATH = "canvas-editor";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * 上传图片字节数组
     *
     * @param imageBytes 图片字节数组
     * @param mimeType   MIME类型
     * @return 图片访问URL
     * @throws IOException 上传失败
     */
    public String uploadImage(byte[] imageBytes, String mimeType) throws IOException {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("图片数据不能为空");
        }

        if (!StringUtils.hasText(uploadType) || "LOCAL".equalsIgnoreCase(uploadType)) {
            return uploadToLocal(imageBytes, mimeType);
        }

        return switch (uploadType.toUpperCase()) {
            case "OSS", "MINIO" -> {
                log.warn("{}上传功能尚未实现，使用本地存储", uploadType);
                yield uploadToLocal(imageBytes, mimeType);
            }
            default -> {
                log.warn("未知的上传类型: {}，使用本地存储", uploadType);
                yield uploadToLocal(imageBytes, mimeType);
            }
        };
    }

    /**
     * 上传到本地文件系统
     */
    private String uploadToLocal(byte[] imageBytes, String mimeType) throws IOException {
        // 1. 生成文件名
        String extension = getExtensionFromMimeType(mimeType);
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

        // 2. 生成日期路径
        String datePath = LocalDate.now().format(DATE_FORMATTER);

        // 3. 构建完整路径
        String relativePath = CANVAS_EDITOR_PATH + "/" + datePath + "/" + fileName;
        File targetFile = new File(uploadPath, relativePath);

        // 4. 确保目录存在
        if (!targetFile.getParentFile().exists()) {
            boolean created = targetFile.getParentFile().mkdirs();
            if (!created) {
                throw new IOException("创建目录失败: " + targetFile.getParentFile().getAbsolutePath());
            }
        }

        // 5. 写入文件
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            fos.write(imageBytes);
            fos.flush();
        }

        // 6. 构建访问URL
        String accessPath = getAccessPath(relativePath);

        log.info("图片上传成功: {} ({} bytes)", accessPath, imageBytes.length);
        return accessPath;
    }

    /**
     * 获取文件访问路径
     */
    private String getAccessPath(String relativePath) {
        // 返回相对路径 /profile/xxx，前端通过代理（vite /profile -> localhost:8780）或 Nginx 反代访问
        // 这样在开发环境和生产环境都能正常访问，避免硬编码 localhost
        return "/profile/" + relativePath;
    }

    /**
     * 根据MIME类型获取文件扩展名
     */
    String getExtensionFromMimeType(String mimeType) {
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
     * 删除图片
     */
    public boolean deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return false;
        }

        try {
            // 从URL中提取文件路径
            String relativePath = extractRelativePath(imageUrl);
            if (relativePath == null) {
                return false;
            }

            File file = new File(uploadPath, relativePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                log.info("删除图片: {} -> {}", imageUrl, deleted ? "成功" : "失败");
                return deleted;
            }

            return false;
        } catch (Exception e) {
            log.error("删除图片失败: {}", imageUrl, e);
            return false;
        }
    }

    /**
     * 从URL中提取相对路径
     */
    String extractRelativePath(String imageUrl) {
        try {
            // 移除协议和域名
            String path = imageUrl.replaceFirst("^https?://[^/]+", "");

            // 移除上下文路径
            path = path.replaceFirst("^/", "");

            return path;
        } catch (Exception e) {
            log.error("提取图片路径失败: {}", imageUrl, e);
            return null;
        }
    }
}
