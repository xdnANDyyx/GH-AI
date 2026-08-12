package com.ruoyi.web.controller.customer;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.MimeTypeUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 客户文件上传控制器
 * 处理前端 /customer/file/upload 等文件上传接口
 */
@Tag(name = "客户文件上传", description = "客户端的文件上传接口")
@RestController
@RequestMapping("/customer/file")
public class CustomerFileController {
    private static final Logger log = LoggerFactory.getLogger(CustomerFileController.class);

    @Value("${server.port:8780}")
    private String serverPort;

    @Value("${ruoyi.profile:D:/ruoyi/uploadPath}")
    private String uploadPath;

    /**
     * 单文件上传 - 供前端直接上传图片使用
     * 对应前端 api/customer.js 中的 uploadFile 函数
     */
    @Operation(summary = "文件上传")
    @PostMapping("/upload")
    public AjaxResult<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "source", required = false) String source,
            HttpServletRequest request) {
        try {
            // 使用文件上传工具类上传
            String filePath = FileUploadUtils.upload(FileUploadUtils.getDefaultBaseDir(), file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);

            // 构建完整的URL（包含协议、域名、端口）
            String fullUrl = buildFullUrl(request, filePath);

            Map<String, Object> data = new HashMap<>();
            data.put("url", fullUrl);
            data.put("fileName", StringUtils.substringAfterLast(filePath, "/"));
            data.put("originalFilename", file.getOriginalFilename());
            data.put("source", source);

            log.info("文件上传成功: {} -> fullUrl={}", filePath, fullUrl);
            return AjaxResult.success(data);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return AjaxResult.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量文件上传
     */
    @Operation(summary = "批量文件上传")
    @PostMapping("/uploads")
    public AjaxResult<Map<String, Object>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "source", required = false) String source,
            HttpServletRequest request) {
        try {
            Map<String, Object> data = new HashMap<>();
            java.util.List<String> urls = new java.util.ArrayList<>();
            java.util.List<String> fullUrls = new java.util.ArrayList<>();
            java.util.List<String> fileNames = new java.util.ArrayList<>();

            for (MultipartFile file : files) {
                String filePath = FileUploadUtils.upload(FileUploadUtils.getDefaultBaseDir(), file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
                urls.add(filePath);
                fullUrls.add(buildFullUrl(request, filePath));
                fileNames.add(StringUtils.substringAfterLast(filePath, "/"));
            }

            data.put("urls", fullUrls);
            data.put("fileNames", fileNames);
            data.put("source", source);

            log.info("批量文件上传成功: {} files", files.length);
            return AjaxResult.success(data);
        } catch (Exception e) {
            log.error("批量文件上传失败", e);
            return AjaxResult.error("批量文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 构建完整的文件访问URL
     * 将相对路径转换为包含协议、域名、端口的完整URL
     */
    private String buildFullUrl(HttpServletRequest request, String relativePath) {
        try {
            String scheme = request.getScheme(); // http 或 https
            String serverName = request.getServerName(); // 域名或IP
            int port = request.getServerPort(); // 端口
            String contextPath = request.getContextPath(); // 应用上下文路径

            // 构建基础URL
            StringBuilder baseUrl = new StringBuilder();
            baseUrl.append(scheme).append("://").append(serverName);

            // 添加端口（如果不是默认端口）
            if ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443)) {
                baseUrl.append(":").append(port);
            }

            // 添加上下文路径（去除末尾多余的 /，避免与文件路径拼出双斜杠）
            if (StringUtils.isNotEmpty(contextPath)) {
                String normalizedContextPath = contextPath;
                while (normalizedContextPath.length() > 1 && normalizedContextPath.endsWith("/")) {
                    normalizedContextPath = normalizedContextPath.substring(0, normalizedContextPath.length() - 1);
                }
                baseUrl.append(normalizedContextPath);
            }

            // 规范化文件路径：去除开头的多余 /，统一以单 / 拼接
            String normalizedRelativePath = relativePath;
            while (normalizedRelativePath.startsWith("/")) {
                normalizedRelativePath = normalizedRelativePath.substring(1);
            }
            baseUrl.append("/").append(normalizedRelativePath);

            return baseUrl.toString();
        } catch (Exception e) {
            log.warn("构建完整URL失败，返回相对路径: {}", relativePath, e);
            return relativePath;
        }
    }
}
