package com.ruoyi.web.controller.customer;

import com.ruoyi.business.domain.GhPromptTemplate;
import com.ruoyi.business.service.IGhPromptTemplateService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.service.CustomerAiImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/customer/ai/image", "/customer/ai-image"})
@RequiredArgsConstructor
public class CustomerAiImageController {

    private final IGhPromptTemplateService promptTemplateService;
    private final CustomerAiImageService customerAiImageService;

    @GetMapping("/session-type/prompt-info")
    public AjaxResult<Map<String, Object>> getSessionTypePromptInfo(@RequestParam String sessionType) {
        GhPromptTemplate template = promptTemplateService.lambdaQuery()
            .eq(GhPromptTemplate::getModule, sessionType)
            .eq(GhPromptTemplate::getStatus, "0")
            .orderByDesc(GhPromptTemplate::getIsDefault)
            .orderByAsc(GhPromptTemplate::getSort)
            .last("limit 1")
            .one();

        Map<String, Object> data = new HashMap<>();
        data.put("sessionType", sessionType);
        data.put("prompt", template != null ? template.getContent() : "");
        data.put("templateId", template != null ? template.getId() : null);
        data.put("templateName", template != null ? template.getName() : "");
        return AjaxResult.success(data);
    }

    @PostMapping("/generate/omni-image")
    public AjaxResult<Map<String, Object>> generateOmniImage(@RequestBody Map<String, Object> params) {
        Object sessionIdValue = params.get("sessionId");
        String sessionId = sessionIdValue != null ? sessionIdValue.toString() : UUID.randomUUID().toString();
        Object promptValue = params.get("prompt");
        String prompt = promptValue != null ? promptValue.toString() : "";
        Object nValue = params.get("n");
        int n = 1;
        if (nValue != null) {
            try {
                n = Integer.parseInt(nValue.toString());
            } catch (NumberFormatException ignored) {
            }
        }
        Object sizeValue = params.get("size");
        String size = sizeValue != null ? sizeValue.toString() : null;

        List<String> images = customerAiImageService.generateImages(prompt, n, size, params);

        Map<String, Object> data = new HashMap<>();
        data.put("id", UUID.randomUUID().toString());
        data.put("sessionId", sessionId);
        data.put("prompt", prompt);
        data.put("status", "2");
        data.put("images", images);
        return AjaxResult.success(data);
    }

    @GetMapping("/result/{recordId}")
    public AjaxResult<Map<String, Object>> queryGenerateResult(@PathVariable String recordId) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", recordId);
        data.put("sessionId", UUID.randomUUID().toString());
        data.put("status", "2");
        data.put("images", List.of("https://via.placeholder.com/1024x1024.png?text=AI+Result"));
        return AjaxResult.success(data);
    }

    @PostMapping("/prompt/reverse")
    public AjaxResult<Map<String, Object>> reversePrompt(@RequestBody Map<String, Object> params) {
        Object promptValue = params.get("prompt");
        String prompt = promptValue != null ? promptValue.toString() : "";
        Object imageValue = params.get("image");
        String image = imageValue != null ? imageValue.toString() : "";
        String result = customerAiImageService.reversePrompt(image, prompt);
        Map<String, Object> data = new HashMap<>();
        data.put("prompt", result);
        return AjaxResult.success(data);
    }

    @GetMapping("/options/{optionKey}")
    public AjaxResult<Map<String, Object>> getImageOptions(@PathVariable String optionKey) {
        Map<String, Object> data = new HashMap<>();
        data.put("optionKey", optionKey);
        data.put("options", List.of(
            Map.of("value", "default", "label", "默认设置")
        ));
        return AjaxResult.success(data);
    }

    @PostMapping("/options")
    public AjaxResult<Map<String, Object>> getImageOptionsBatch(@RequestBody List<String> optionKeys) {
        Map<String, Object> data = new HashMap<>();
        data.put("options", optionKeys.stream().map(key -> Map.of(
            "optionKey", key,
            "options", List.of(Map.of("value", "default", "label", "默认设置"))
        )).toList());
        return AjaxResult.success(data);
    }

    @GetMapping("/pixel-configs")
    public AjaxResult<List<Map<String, Object>>> getPixelConfigs() {
        List<Map<String, Object>> configs = List.of(
            Map.of("qualityValue", "1k", "qualityLabel", "标清1K", "ratioValue", "1:1", "ratioLabel", "1:1 正方形", "width", 1328, "height", 1328),
            Map.of("qualityValue", "2k", "qualityLabel", "高清2K", "ratioValue", "16:9", "ratioLabel", "16:9 宽屏", "width", 2560, "height", 1440)
        );
        return AjaxResult.success(configs);
    }

    @GetMapping("/deduct-types")
    public AjaxResult<List<Map<String, Object>>> getDeductTypes() {
        List<Map<String, Object>> types = List.of(
            Map.of("code", "default", "label", "默认扣分", "description", "AI 生成默认扣分类型"),
            Map.of("code", "high_quality", "label", "高质量扣分", "description", "高质量生成扣分")
        );
        return AjaxResult.success(types);
    }

    @GetMapping("/session-types")
    public AjaxResult<List<Map<String, Object>>> getSessionTypes() {
        List<Map<String, Object>> sessionTypes = List.of(
            Map.of("sessionType", "white_bg", "label", "AI白底图", "description", "自动生成商品白底图"),
            Map.of("sessionType", "render", "label", "白底图生成背景", "description", "生成场景背景"),
            Map.of("sessionType", "main_image", "label", "主图设计", "description", "生成商品主图"),
            Map.of("sessionType", "detail", "label", "详情图", "description", "生成详情页素材")
        );
        return AjaxResult.success(sessionTypes);
    }

    @PostMapping("/session")
    public AjaxResult<String> createSession(@RequestParam String sessionType, @RequestParam String prompt) {
        return AjaxResult.success(UUID.randomUUID().toString());
    }

    @GetMapping("/sessions")
    public AjaxResult<Map<String, Object>> listSessions(@RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "10") int pageSize,
                                                      @RequestParam(required = false) String sessionType) {
        Map<String, Object> data = new HashMap<>();
        data.put("total", 0);
        data.put("rows", List.of());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return AjaxResult.success(data);
    }

    @GetMapping("/session/{sessionId}")
    public AjaxResult<Map<String, Object>> getSessionDetail(@PathVariable String sessionId) {
        Map<String, Object> data = new HashMap<>();
        data.put("sessionId", sessionId);
        data.put("status", "0");
        data.put("sessionType", "render");
        data.put("lastPrompt", "示例提示词");
        return AjaxResult.success(data);
    }

    @GetMapping("/session")
    public AjaxResult<Map<String, Object>> getSessionDetailAlt(@RequestParam(required = false) String sessionId,
                                                              @RequestParam(required = false) String recordType) {
        Map<String, Object> data = new HashMap<>();
        data.put("sessionId", sessionId);
        data.put("recordType", recordType);
        data.put("status", "0");
        data.put("sessionType", "render");
        return AjaxResult.success(data);
    }

    @DeleteMapping("/session/{sessionId}")
    public AjaxResult<Void> deleteSession(@PathVariable String sessionId) {
        return AjaxResult.success();
    }

    @PostMapping("/session/canvas")
    public AjaxResult<Void> saveCanvasParams(@RequestBody Map<String, Object> params) {
        return AjaxResult.success();
    }

    /**
     * 测试 AI 模型是否可用
     * 用于前端生图前检测 API key 是否有效
     */
    @GetMapping("/test")
    public AjaxResult<Map<String, Object>> testAiModel() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 调用服务层测试方法
            customerAiImageService.testAiModel();
            result.put("available", true);
            result.put("message", "AI 模型可用");
            return AjaxResult.success(result);
        } catch (Exception e) {
            result.put("available", false);
            result.put("message", e.getMessage());
            return AjaxResult.success(result);
        }
    }
}
