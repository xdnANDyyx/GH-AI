package com.ruoyi.common.service;

import com.ruoyi.common.core.domain.entity.SysUser;

/**
 * 客户邀请奖励服务。
 * <p>
 * 该接口下沉到公共模块，供注册链路直接调用，避免 framework 反向依赖 customer 模块。
 */
public interface ICustomerInviteRewardService {

    /**
     * 生成唯一邀请码。
     */
    String generateUniqueInviteCode();

    /**
     * 绑定邀请关系并发放双向邀请奖励。
     *
     * @param inviteeUser 被邀请人
     * @param inviteCode  邀请码
     */
    void registerInviteReward(SysUser inviteeUser, String inviteCode);
}
