package com.ruoyi.web.controller.customer;

import com.ruoyi.business.domain.GhPromptLibrary;
import com.ruoyi.business.service.IGhPromptLibraryService;
import com.ruoyi.common.core.domain.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提示词选项库 Controller（C端工作室）
 * 前端用户在生图前按分类拉取启用中的提示词选项，以列表选择形式呈现
 *
 * @author guanghe
 */
@Tag(name = "客户端提示词选项库", description = "C端按分类拉取提示词选项")
@RestController
@RequestMapping("/customer/promptLibrary")
@RequiredArgsConstructor
public class CustomerPromptLibraryController {

    private final IGhPromptLibraryService promptLibraryService;

    /**
     * 按分类拉取启用中的提示词选项列表
     *
     * @param category 提示词库分类（function/platform/product/material/scene/style/selling/size/quality/negative/camera/option）
     * @param scope    适用功能（可选，如 white_bg、change_bg、main_image、ai_model、retouch、detail、dimension、banner、batch）
     */
    @Operation(summary = "按分类拉取提示词选项", description = "C端生图前按分类获取启用中的提示词选项列表")
    @GetMapping("/list")
    public AjaxResult<List<GhPromptLibrary>> list(
            @Parameter(description = "提示词库分类") @RequestParam String category,
            @Parameter(description = "适用功能（可选）") @RequestParam(required = false) String scope) {
        return AjaxResult.success(promptLibraryService.listEnabled(category, scope));
    }

    /**
     * 批量按分类拉取提示词选项（一次请求多个分类，减少前端并发）
     *
     * @param categories 多个分类（逗号分隔）
     * @param scope      适用功能（可选）
     */
    @Operation(summary = "批量按分类拉取提示词选项", description = "一次请求多个分类的提示词选项，返回按分类分组的Map")
    @GetMapping("/listBatch")
    public AjaxResult<Map<String, List<GhPromptLibrary>>> listBatch(
            @Parameter(description = "多个分类（逗号分隔）") @RequestParam String categories,
            @Parameter(description = "适用功能（可选）") @RequestParam(required = false) String scope) {
        Map<String, List<GhPromptLibrary>> result = new HashMap<>();
        for (String category : categories.split(",")) {
            String trimmed = category.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            result.put(trimmed, promptLibraryService.listEnabled(trimmed, scope));
        }
        return AjaxResult.success(result);
    }
}