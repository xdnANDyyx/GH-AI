package com.ruoyi.common.core.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户登录对象")
public class LoginBody {
    
    @Schema(description = "用户名", required = true, example = "admin")
    private String username;

    @Schema(description = "用户密码", required = true, example = "admin123")
    private String password;

    @Schema(description = "验证码", example = "1234")
    private String code;

    @Schema(description = "验证码唯一标识", example = "uuid-xxx-xxx")
    private String uuid;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
