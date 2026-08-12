package com.ruoyi.web.controller.customer;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.service.CustomerAiDialogueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 对话接口（AI 助手问答）
 * 对应前端 /api/customer/ai/dialogue/chat
 */
@RestController
@RequestMapping("/customer/ai/dialogue")
@RequiredArgsConstructor
public class CustomerAiDialogueController {

    private final CustomerAiDialogueService customerAiDialogueService;

    /**
     * AI 对话聊天
     * @param params 请求参数，包含：
     *               - messages: List<{role: String, content: String}> 历史消息
     *               - content: String 当前用户输入
     *               - model: String (可选) 指定模型，默认 deepseek
     */
    @PostMapping("/chat")
    public AjaxResult<Map<String, Object>> chat(@RequestBody Map<String, Object> params) {
        // 提取消息列表
        @SuppressWarnings("unchecked")
        List<Map<String, String>> messages = (List<Map<String, String>>) params.get("messages");
        String content = params.get("content") != null ? params.get("content").toString() : "";
        String model = params.get("model") != null ? params.get("model").toString() : "deepseek";

        // 调用 AI 服务获取回复
        String reply = customerAiDialogueService.chat(messages, content, model);

        Map<String, Object> data = new java.util.HashMap<>();
        data.put("reply", reply);
        data.put("model", model);
        return AjaxResult.success(data);
    }
}