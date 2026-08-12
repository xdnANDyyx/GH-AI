package com.ruoyi.web.controller.customer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.business.domain.GhPointsPackage;
import com.ruoyi.business.domain.GhPointsRecord;
import com.ruoyi.business.service.IGhPointsPackageService;
import com.ruoyi.business.service.IGhPointsRecordService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/customer/points")
public class CustomerPointsController {

    @Autowired
    private IGhPointsRecordService pointsRecordService;

    @Autowired
    private IGhPointsPackageService pointsPackageService;

    @GetMapping("/remaining")
    public AjaxResult<Integer> getRemainingPoints() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getUser() == null) {
            return AjaxResult.error("用户未登录，请先登录");
        }

        SysUser user = loginUser.getUser();
        if (!"2".equals(user.getUserType())) {
            return AjaxResult.success(0);
        }

        GhPointsRecord latestRecord = pointsRecordService.getOne(
            new LambdaQueryWrapper<GhPointsRecord>()
                .eq(GhPointsRecord::getUserId, user.getUserId())
                .orderByDesc(GhPointsRecord::getCreateTime)
                .last("limit 1"),
            false
        );

        int remainingPoints = latestRecord != null && latestRecord.getBalanceAfter() != null
            ? latestRecord.getBalanceAfter()
            : 0;

        return AjaxResult.success(remainingPoints);
    }

    /**
     * 获取上架的积分套餐列表（客户端）
     * 返回 status=0（上架）的套餐（含赠送套餐和正常套餐），按 sort 排序
     */
    @GetMapping("/package/list")
    public AjaxResult<List<GhPointsPackage>> getPackageList() {
        List<GhPointsPackage> list = pointsPackageService.list(
            new LambdaQueryWrapper<GhPointsPackage>()
                .eq(GhPointsPackage::getStatus, "0")
                .orderByAsc(GhPointsPackage::getSort)
        );
        return AjaxResult.success(list);
    }
}
