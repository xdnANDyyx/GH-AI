package com.ruoyi.common.core.domain.model;

import com.ruoyi.common.core.domain.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录用户返回 VO")
public class LoginUserVO {

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "用户账号")
    private String userName;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "手机号码")
    private String phonenumber;

    @Schema(description = "微信 UnionID")
    private String wechatUnionId;

    @Schema(description = "邀请码")
    private String inviteCode;

    @Schema(description = "用户性别")
    private String sex;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "账号状态（0 正常 1 停用）")
    private String status;

    public static LoginUserVO fromSysUser(SysUser sysUser) {
        if (sysUser == null) {
            return null;
        }
        
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setUserId(sysUser.getUserId());
        loginUserVO.setUserName(sysUser.getUserName());
        loginUserVO.setNickName(sysUser.getNickName());
        loginUserVO.setEmail(sysUser.getEmail());
        loginUserVO.setPhonenumber(sysUser.getPhonenumber());
        loginUserVO.setInviteCode(sysUser.getInviteCode());
        loginUserVO.setSex(sysUser.getSex());
        loginUserVO.setAvatar(sysUser.getAvatar());
        loginUserVO.setStatus(sysUser.getStatus());
        return loginUserVO;
    }
}
