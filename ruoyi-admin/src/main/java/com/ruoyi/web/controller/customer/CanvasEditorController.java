package com.ruoyi.web.controller.customer;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.service.CanvasEditorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Canvas Editor API 控制器
 * 提供画布编辑功能：扩图、多角度、改文字、局部重绘、图层检测等
 */
@Slf4j
@RestController
@RequestMapping("/ai/image")
@RequiredArgsConstructor
public class CanvasEditorController {

    private final CanvasEditorService canvasEditorService;

    // ============================================
    // 1. 图片扩图
    // ============================================
    @PostMapping("/extend")
    public AjaxResult<Map<String, Object>> extendImage(@RequestBody Map<String, Object> params) {
        try {
            log.info("图片扩图请求: {}", params);

            String imageUrl = (String) params.get("imageUrl");
            String ratio = (String) params.get("ratio");
            Integer width = params.get("width") instanceof Number ? ((Number) params.get("width")).intValue() : null;
            Integer height = params.get("height") instanceof Number ? ((Number) params.get("height")).intValue() : null;

            if (imageUrl == null || imageUrl.isEmpty()) {
                return AjaxResult.error("图片URL不能为空");
            }

            Map<String, Object> result = canvasEditorService.extendImage(imageUrl, ratio, width, height);
            return AjaxResult.success(result);

        } catch (Exception e) {
            log.error("图片扩图失败", e);
            return AjaxResult.error("扩图失败: " + e.getMessage());
        }
    }

    // ============================================
    // 2. 多角度生成
    // ============================================
    @PostMapping("/multi-angle")
    public AjaxResult<Map<String, Object>> generateMultiAngle(@RequestBody Map<String, Object> params) {
        try {
            log.info("多角度生成请求: {}", params);

            String imageUrl = (String) params.get("imageUrl");
            Integer count = params.get("count") instanceof Number ? ((Number) params.get("count")).intValue() : 4;
            String type = (String) params.get("type");

            if (imageUrl == null || imageUrl.isEmpty()) {
                return AjaxResult.error("图片URL不能为空");
            }

            if (count < 2 || count > 8) {
                return AjaxResult.error("生成数量必须在2-8之间");
            }

            List<Map<String, Object>> images = canvasEditorService.generateMultiAngle(imageUrl, count, type);
            Map<String, Object> result = new HashMap<>();
            result.put("images", images);
            return AjaxResult.success(result);

        } catch (Exception e) {
            log.error("多角度生成失败", e);
            return AjaxResult.error("生成失败: " + e.getMessage());
        }
    }

    // ============================================
    // 3. 修改文字
    // ============================================
    @PostMapping("/edit-text")
    public AjaxResult<Map<String, Object>> editImageText(@RequestBody Map<String, Object> params) {
        try {
            log.info("修改文字请求: {}", params);

            String imageUrl = (String) params.get("imageUrl");
            String originalText = (String) params.get("originalText");
            String newText = (String) params.get("newText");
            String font = (String) params.get("font");

            if (imageUrl == null || imageUrl.isEmpty()) {
                return AjaxResult.error("图片URL不能为空");
            }

            if (newText == null || newText.isEmpty()) {
                return AjaxResult.error("新文字不能为空");
            }

            Map<String, Object> result = canvasEditorService.editImageText(imageUrl, originalText, newText, font);
            return AjaxResult.success(result);

        } catch (Exception e) {
            log.error("修改文字失败", e);
            return AjaxResult.error("修改失败: " + e.getMessage());
        }
    }

    // ============================================
    // 4. 局部重绘
    // ============================================
    @PostMapping("/partial-redraw")
    public AjaxResult<Map<String, Object>> partialRedraw(@RequestBody Map<String, Object> params) {
        try {
            log.info("局部重绘请求: {}", params);

            String imageUrl = (String) params.get("imageUrl");
            String description = (String) params.get("description");
            Map<String, Object> mask = (Map<String, Object>) params.get("mask");

            if (imageUrl == null || imageUrl.isEmpty()) {
                return AjaxResult.error("图片URL不能为空");
            }

            if (description == null || description.isEmpty()) {
                return AjaxResult.error("重绘描述不能为空");
            }

            Map<String, Object> result = canvasEditorService.partialRedraw(imageUrl, description, mask);
            return AjaxResult.success(result);

        } catch (Exception e) {
            log.error("局部重绘失败", e);
            return AjaxResult.error("重绘失败: " + e.getMessage());
        }
    }

    // ============================================
    // 5. 图层检测
    // ============================================
    @PostMapping("/detect-layers")
    public AjaxResult<Map<String, Object>> detectLayers(@RequestBody Map<String, Object> params) {
        try {
            log.info("图层检测请求: {}", params);

            String imageUrl = (String) params.get("imageUrl");

            if (imageUrl == null || imageUrl.isEmpty()) {
                return AjaxResult.error("图片URL不能为空");
            }

            List<Map<String, Object>> layers = canvasEditorService.detectLayers(imageUrl);
            Map<String, Object> result = new HashMap<>();
            result.put("layers", layers);
            return AjaxResult.success(result);

        } catch (Exception e) {
            log.error("图层检测失败", e);
            return AjaxResult.error("检测失败: " + e.getMessage());
        }
    }

    // ============================================
    // 6. 下载图层
    // ============================================
    @PostMapping("/download-layers")
    public AjaxResult<Map<String, Object>> downloadLayers(@RequestBody Map<String, Object> params) {
        try {
            log.info("下载图层请求: {}", params);

            String imageUrl = (String) params.get("imageUrl");
            List<String> layers = (List<String>) params.get("layers");
            String format = (String) params.get("format");

            if (imageUrl == null || imageUrl.isEmpty()) {
                return AjaxResult.error("图片URL不能为空");
            }

            Map<String, Object> result = canvasEditorService.downloadLayers(imageUrl, layers, format);
            return AjaxResult.success(result);

        } catch (Exception e) {
            log.error("下载图层失败", e);
            return AjaxResult.error("下载失败: " + e.getMessage());
        }
    }

    // ============================================
    // 7. OCR文字识别
    // ============================================
    @PostMapping("/ocr")
    public AjaxResult<Map<String, Object>> recognizeText(@RequestBody Map<String, Object> params) {
        try {
            log.info("OCR识别请求: {}", params);

            String imageUrl = (String) params.get("imageUrl");

            if (imageUrl == null || imageUrl.isEmpty()) {
                return AjaxResult.error("图片URL不能为空");
            }

            List<Map<String, Object>> texts = canvasEditorService.recognizeText(imageUrl);
            Map<String, Object> result = new HashMap<>();
            result.put("texts", texts);
            return AjaxResult.success(result);

        } catch (Exception e) {
            log.error("OCR识别失败", e);
            return AjaxResult.error("识别失败: " + e.getMessage());
        }
    }

    // ============================================
    // 8. 编辑历史
    // ============================================
    @GetMapping("/{imageId}/history")
    public AjaxResult<Map<String, Object>> getEditHistory(@PathVariable String imageId) {
        try {
            log.info("查询编辑历史: {}", imageId);

            List<Map<String, Object>> history = canvasEditorService.getEditHistory(imageId);
            Map<String, Object> result = new HashMap<>();
            result.put("imageId", imageId);
            result.put("history", history);
            return AjaxResult.success(result);

        } catch (Exception e) {
            log.error("查询编辑历史失败", e);
            return AjaxResult.error("查询失败: " + e.getMessage());
        }
    }

    // ============================================
    // 9. 撤销编辑
    // ============================================
    @PostMapping("/revert")
    public AjaxResult<Map<String, Object>> revertImage(@RequestBody Map<String, Object> params) {
        try {
            log.info("撤销编辑请求: {}", params);

            String imageId = (String) params.get("imageId");
            Integer version = params.get("version") instanceof Number ? ((Number) params.get("version")).intValue() : null;

            if (imageId == null || imageId.isEmpty()) {
                return AjaxResult.error("图片ID不能为空");
            }

            if (version == null) {
                return AjaxResult.error("版本号不能为空");
            }

            Map<String, Object> result = canvasEditorService.revertImage(imageId, version);
            return AjaxResult.success(result);

        } catch (Exception e) {
            log.error("撤销编辑失败", e);
            return AjaxResult.error("撤销失败: " + e.getMessage());
        }
    }
}
