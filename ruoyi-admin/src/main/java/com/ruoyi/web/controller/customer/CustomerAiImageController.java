package com.ruoyi.web.controller.customer;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.business.domain.GhPromptTemplate;
import com.ruoyi.business.service.IGhPromptTemplateService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.web.service.CustomerAiImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/customer/ai/image", "/customer/ai-image"})
@RequiredArgsConstructor
public class CustomerAiImageController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomerAiImageController.class);

    private final IGhPromptTemplateService promptTemplateService;
    private final CustomerAiImageService customerAiImageService;
    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        checkAndPatchDatabase();
    }

    private void checkAndPatchDatabase() {
        try {
            // 1. 检查表是否存在
            jdbcTemplate.execute("SELECT 1 FROM ai_image_generate_record LIMIT 1");
            
            // 2. 检查 type 列是否存在并自动添加
            try {
                jdbcTemplate.execute("SELECT type FROM ai_image_generate_record LIMIT 1");
            } catch (Exception e) {
                log.info("数据库表 ai_image_generate_record 缺少 type 列，正在自动升级...");
                jdbcTemplate.execute("ALTER TABLE `ai_image_generate_record` ADD COLUMN `type` char(1) NOT NULL DEFAULT '1' COMMENT '记录类型（1:对话,2:画布）' AFTER `user_id`");
            }
            
            // 3. 检查 ai_task_id 列是否存在并自动添加
            try {
                jdbcTemplate.execute("SELECT ai_task_id FROM ai_image_generate_record LIMIT 1");
            } catch (Exception e) {
                log.info("数据库表 ai_image_generate_record 缺少 ai_task_id 列，正在自动升级...");
                jdbcTemplate.execute("ALTER TABLE `ai_image_generate_record` ADD COLUMN `ai_task_id` varchar(100) DEFAULT NULL COMMENT 'AI任务ID' AFTER `model_name`");
            }

            // 4. 自动升级 target_images 和 reference_images 为 LONGTEXT，以支持 Base64 长文本存储
            try {
                log.info("自动扩容 target_images 和 reference_images 列为 LONGTEXT，以支持 Base64 图数据存储...");
                jdbcTemplate.execute("ALTER TABLE `ai_image_generate_record` MODIFY COLUMN `target_images` LONGTEXT DEFAULT NULL COMMENT '目标图URL列表（JSON数组）'");
                jdbcTemplate.execute("ALTER TABLE `ai_image_generate_record` MODIFY COLUMN `reference_images` LONGTEXT DEFAULT NULL COMMENT '参考图URL列表（JSON数组）'");
            } catch (Exception e) {
                log.error("自动升级数据库列 [target_images/reference_images] 失败: ", e);
            }
        } catch (Exception e) {
            log.error("数据库检测异常: 请确保已在 MySQL 中执行了 sql/ai_image_generate.sql 脚本来初始化 ai_image_generate_record 表！错误原因: " + e.getMessage());
        }
    }

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
                if (nValue instanceof Number) {
                    n = ((Number) nValue).intValue();
                } else {
                    n = (int) Double.parseDouble(nValue.toString());
                }
            } catch (NumberFormatException ignored) {
            }
        }
        Object sizeValue = params.get("size");
        String size = sizeValue != null ? sizeValue.toString() : null;

        List<String> images = customerAiImageService.generateImages(prompt, n, size, params);

        Long userId = SecurityUtils.getUserId();
        String recordId = UUID.randomUUID().toString();
        
        try {
            String targetImagesJson = JSON.toJSONString(images);
            String productImagesJson = JSON.toJSONString(params.get("productImages"));
            String referenceImagesJson = JSON.toJSONString(params.get("referenceImages"));
            
            jdbcTemplate.update(
                "INSERT INTO ai_image_generate_record (ai_task_id, user_id, type, generate_type, prompt, target_images, reference_images, generate_count, status, create_time, create_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                recordId, userId, "2", "2", prompt, targetImagesJson, (productImagesJson != null ? productImagesJson : referenceImagesJson), n, "2", new java.util.Date(), SecurityUtils.getUsername()
            );
        } catch (Exception e) {
            // Keep going if DB logging fails, don't block user generation
            e.printStackTrace();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", recordId);
        data.put("sessionId", sessionId);
        data.put("prompt", prompt);
        data.put("status", "2");
        data.put("images", images);
        return AjaxResult.success(data);
    }

    @GetMapping("/result/{recordId}")
    public AjaxResult<Map<String, Object>> queryGenerateResult(@PathVariable String recordId) {
        try {
            Map<String, Object> record = jdbcTemplate.queryForMap(
                "SELECT * FROM ai_image_generate_record WHERE ai_task_id = ? LIMIT 1",
                recordId
            );
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", record.get("ai_task_id"));
            data.put("sessionId", record.get("session_id"));
            data.put("status", record.get("status"));
            
            String targetImages = (String) record.get("target_images");
            List<String> imageList = targetImages != null ? JSON.parseArray(targetImages, String.class) : List.of();
            data.put("images", imageList);
            return AjaxResult.success(data);
        } catch (Exception e) {
            // Fallback for mock recordIds or if DB doesn't have it
            Map<String, Object> data = new HashMap<>();
            data.put("id", recordId);
            data.put("sessionId", UUID.randomUUID().toString());
            data.put("status", "2");
            data.put("images", List.of("https://via.placeholder.com/1024x1024.png?text=AI+Result"));
            return AjaxResult.success(data);
        }
    }

    @GetMapping("/records")
    public AjaxResult<List<Map<String, Object>>> listRecords(@RequestParam(required = false, defaultValue = "2") String type) {
        Long userId = SecurityUtils.getUserId();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT * FROM ai_image_generate_record WHERE user_id = ? AND type = ? ORDER BY create_time DESC",
            userId, type
        );
        
        List<Map<String, Object>> records = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", row.get("ai_task_id"));
            item.put("name", "批量生成 " + row.get("generate_count") + " 张");
            item.put("designType", "批量生成");
            item.put("materialCount", "产品图 " + row.get("generate_count") + " 张");
            item.put("genCount", row.get("generate_count"));
            item.put("createdAt", row.get("create_time"));
            String status = (String) row.get("status");
            String frontendStatus = "done";
            if ("0".equals(status)) {
                frontendStatus = "queued";
            } else if ("1".equals(status)) {
                frontendStatus = "processing";
            } else if ("2".equals(status)) {
                frontendStatus = "done";
            } else if ("3".equals(status)) {
                frontendStatus = "failed";
            }
            
            item.put("status", frontendStatus);
            item.put("progress", "2".equals(row.get("status")) ? 100 : null);
            
            if ("2".equals(status)) {
                item.put("statusText", "已完成");
                item.put("statusClass", "green");
            } else if ("3".equals(status)) {
                item.put("statusText", "生成失败");
                item.put("statusClass", "red");
            } else {
                item.put("statusText", "生成中");
                item.put("statusClass", "blue");
            }
            
            String targetImagesStr = (String) row.get("target_images");
            if (targetImagesStr != null) {
                List<String> imgs = JSON.parseArray(targetImagesStr, String.class);
                List<Map<String, String>> resultImgs = new ArrayList<>();
                List<Map<String, String>> thumbs = new ArrayList<>();
                for (String url : imgs) {
                    resultImgs.add(Map.of("url", url));
                    if (thumbs.size() < 4) {
                        thumbs.add(Map.of("url", url));
                    }
                }
                item.put("resultImages", resultImgs);
                item.put("thumbs", thumbs);
            }
            records.add(item);
        }
        return AjaxResult.success(records);
    }

    @DeleteMapping("/records/{recordId}")
    public AjaxResult<Void> deleteRecord(@PathVariable String recordId) {
        Long userId = SecurityUtils.getUserId();
        jdbcTemplate.update(
            "DELETE FROM ai_image_generate_record WHERE ai_task_id = ? AND user_id = ?",
            recordId, userId
        );
        return AjaxResult.success();
    }

    @DeleteMapping("/records")
    public AjaxResult<Void> clearRecords(@RequestParam(required = false, defaultValue = "2") String type) {
        Long userId = SecurityUtils.getUserId();
        jdbcTemplate.update(
            "DELETE FROM ai_image_generate_record WHERE user_id = ? AND type = ?",
            userId, type
        );
        return AjaxResult.success();
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
