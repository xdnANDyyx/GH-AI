package com.ruoyi.web.controller.common;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.UploadBatchVO;
import com.ruoyi.common.core.domain.model.UploadVO;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.file.MimeTypeUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "公共接口", description = "文件上传下载等公共接口")
@RestController
@RequestMapping("/common")
public class CommonController {
    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    @Operation(summary = "文件下载", description = "通用文件下载接口")
    @GetMapping("/download")
    public void fileDownload(
            @Parameter(description = "文件名称") String fileName,
            @Parameter(description = "下载后是否删除") Boolean delete,
            HttpServletResponse response,
            HttpServletRequest request) {
        try {
            if (!FileUtils.checkAllowDownload(fileName)) {
                throw new Exception(StringUtils.format("文件名称({})非法，不允许下载。 ", fileName));
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = RuoYiConfig.getDownloadPath() + fileName;

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            FileUtils.writeBytes(filePath, response.getOutputStream());
            if (delete) {
                FileUtils.deleteFile(filePath);
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    @Operation(summary = "单文件上传", description = "通用单文件上传接口")
    @PostMapping("/upload")
    public AjaxResult<UploadVO> uploadFile(
            @Parameter(description = "上传的文件") @RequestParam("file") MultipartFile file) {
        try {
            String filePath = FileUploadUtils.upload(FileUploadUtils.getDefaultBaseDir(), file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
            UploadVO vo = new UploadVO();
            vo.setUrl(filePath);
            vo.setFileName(StringUtils.substringAfterLast(filePath, "/"));
            vo.setNewFileName(StringUtils.substringAfterLast(filePath, "/"));
            vo.setOriginalFilename(file.getOriginalFilename());
            return AjaxResult.success(vo);
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return AjaxResult.error(e.getMessage());
        }
    }

    @Operation(summary = "多文件上传", description = "通用多文件上传接口")
    @PostMapping("/uploads")
    public AjaxResult<UploadBatchVO> uploadFiles(
            @Parameter(description = "上传的文件列表") @RequestParam("files") List<MultipartFile> files) {
        try {
            UploadBatchVO vo = new UploadBatchVO();
            List<String> urls = new java.util.ArrayList<>();
            List<String> fileNames = new java.util.ArrayList<>();
            List<String> newFileNames = new java.util.ArrayList<>();
            List<String> originalFilenames = new java.util.ArrayList<>();
            for (MultipartFile file : files) {
                String filePath = FileUploadUtils.upload(FileUploadUtils.getDefaultBaseDir(), file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
                String newFileName = StringUtils.substringAfterLast(filePath, "/");
                urls.add(filePath);
                fileNames.add(newFileName);
                newFileNames.add(newFileName);
                originalFilenames.add(file.getOriginalFilename());
            }
            vo.setUrls(String.join(",", urls));
            vo.setFileNames(String.join(",", fileNames));
            vo.setNewFileNames(String.join(",", newFileNames));
            vo.setOriginalFilenames(String.join(",", originalFilenames));
            return AjaxResult.success(vo);
        } catch (Exception e) {
            log.error("批量上传文件失败", e);
            return AjaxResult.error(e.getMessage());
        }
    }

    @Operation(summary = "本地资源下载", description = "本地资源通用下载接口")
    @GetMapping("/download/resource")
    public void resourceDownload(
            @Parameter(description = "资源路径") String resource,
            HttpServletRequest request,
            HttpServletResponse response)
            throws Exception {
        try {
            if (!FileUtils.checkAllowDownload(resource)) {
                throw new Exception(StringUtils.format("资源文件({})非法，不允许下载。 ", resource));
            }
            String localPath = RuoYiConfig.getProfile();
            String downloadPath = localPath + FileUtils.stripPrefix(resource);
            String downloadName = StringUtils.substringAfterLast(downloadPath, "/");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, downloadName);
            FileUtils.writeBytes(downloadPath, response.getOutputStream());
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }
}
