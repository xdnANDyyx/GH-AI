package com.ruoyi.common.core.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "批量文件上传VO")
public class UploadBatchVO {

    @Schema(description = "文件访问URL列表(逗号分隔,相对路径)")
    private String urls;

    @Schema(description = "文件访问全路径URL列表(逗号分隔)")
    private String fullUrls;

    @Schema(description = "文件名称列表(逗号分隔)")
    private String fileNames;

    @Schema(description = "新文件名列表(逗号分隔)")
    private String newFileNames;

    @Schema(description = "原始文件名列表(逗号分隔)")
    private String originalFilenames;
}
