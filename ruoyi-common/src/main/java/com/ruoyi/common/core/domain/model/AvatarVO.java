package com.ruoyi.common.core.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "头像上传VO")
public class AvatarVO {

    @Schema(description = "头像URL")
    private String imgUrl;
}
