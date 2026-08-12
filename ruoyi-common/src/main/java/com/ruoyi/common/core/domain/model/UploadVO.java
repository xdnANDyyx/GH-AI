package com.ruoyi.common.core.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "文件上传VO")
public class UploadVO {

    @Schema(description = "文件访问URL(相对路径)")
    private String url;

    @Schema(description = "文件访问全路径URL")
    private String fullUrl;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "新文件名")
    private String newFileName;

    @Schema(description = "原始文件名")
    private String originalFilename;
}
