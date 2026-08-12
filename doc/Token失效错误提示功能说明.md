# Token失效错误提示功能说明

## 功能概述

当AI服务（OpenAI/LightCC等）的API Key失效时，系统会明确提示用户"API密钥已失效"，并引导用户联系管理员更换Key，而不是显示模糊的技术错误信息。

## 修改内容

### 1. 后端修改

#### 1.1 CustomerAiImageService.java（图片生成服务）

**修改位置**：
- `generateOpenAiImage()` 方法（第552-589行）
- `doGenerateLightCCImage()` 方法（第156-240行）

**修改内容**：
在调用AI服务返回错误时，检测HTTP状态码和错误信息：
- 如果状态码为 401
- 或者错误信息包含 "Invalid token"（不区分大小写）

则抛出明确的错误信息：
```java
"API密钥已失效，请更换有效的API Key后重试"
```

或

```java
"LightCC API密钥已失效，请更换有效的API Key后重试"
```

**代码示例**：
```java
if (response.statusCode() >= 300) {
    JSONObject err = safeParse(response.body());
    String message = err.getString("message");
    if (message == null) {
        message = err.getJSONObject("error").getString("message");
    }
    
    // 检测是否是token失效错误
    if (response.statusCode() == 401 || 
        message != null && message.toLowerCase().contains("invalid token")) {
        throw new IllegalStateException("API密钥已失效，请更换有效的API Key后重试");
    }
    
    throw new IllegalStateException("AI 图片生成失败: " + response.statusCode() + " " + message);
}
```

#### 1.2 CustomerAiDialogueService.java（AI对话服务）

**修改位置**：
- `callChatApi()` 方法（第184-230行）

**修改内容**：
在调用AI对话API返回错误时，同样检测401状态码和"Invalid token"错误信息，并返回明确的错误提示给用户：

```java
if (response.statusCode() == 401 || 
    (responseBody != null && responseBody.toLowerCase().contains("invalid token"))) {
    log.error("[{}] API密钥已失效，请更换有效的API Key", providerName);
    return "抱歉，" + providerName + "服务的API密钥已失效，请联系管理员更新系统配置。";
}
```

### 2. 前端修改

#### 2.1 useImageGeneration.js（图片生成Composable）

**修改位置**：
- `generate()` 函数（第135-175行）

**修改内容**：
在捕获生成错误时，检测错误信息是否包含token失效相关的关键词，如果是，则显示更友好的错误提示：

```javascript
catch (e) {
  // 检测是否是token失效错误
  const errorMsg = e.message || '生成失败'
  if (errorMsg.includes('API密钥已失效') || 
      errorMsg.includes('Invalid token') || 
      errorMsg.includes('401')) {
    error.value = 'API密钥已失效，请更换有效的API Key后重试。请联系管理员更新系统配置。'
  } else {
    error.value = errorMsg
  }
  statusText.value = '生成失败'
  throw e
}
```

**显示效果**：
- 错误信息会显示在生成按钮下方的红色错误提示区域
- 提示内容：`API密钥已失效，请更换有效的API Key后重试。请联系管理员更新系统配置。`

## 错误处理流程

### 图片生成流程

1. 用户点击"开始生成"按钮
2. 后端调用AI服务（OpenAI/LightCC）
3. AI服务返回401错误 + "Invalid token"
4. 后端捕获错误，识别为token失效
5. 后端抛出明确错误信息："API密钥已失效，请更换有效的API Key后重试"
6. 前端捕获错误，识别为token失效
7. 前端显示友好提示："API密钥已失效，请更换有效的API Key后重试。请联系管理员更新系统配置。"

### AI对话流程

1. 用户在AI助手对话框输入问题
2. 后端调用AI对话服务（DeepSeek/Doubao/Qwen-VL等）
3. AI服务返回401错误 + "Invalid token"
4. 后端捕获错误，识别为token失效
5. 后端返回明确提示："抱歉，[服务名称]服务的API密钥已失效，请联系管理员更新系统配置。"
6. 前端在对话气泡中显示该提示

## 支持的AI服务

以下AI服务都已添加token失效检测：

### 图片生成服务
- OpenAI（openAiEnvApiKey / openAi302ApiKey）
- LightCC（lightccImageApiKey）

### AI对话服务
- DeepSeek（deepseekApiKey）
- Doubao（doubaoApiKey）
- Qwen-VL（qwenVlApiKey）
- LightCC（lightccApiKey）
- OpenAI 302（openAi302ApiKey）

## 检测规则

系统通过以下两种方式检测token失效：

1. **HTTP状态码检测**：状态码为 401（Unauthorized）
2. **错误信息检测**：错误消息包含 "Invalid token"（不区分大小写）

只要满足任一条件，就会触发token失效提示。

## 用户提示信息

### 图片生成失败提示
```
API密钥已失效，请更换有效的API Key后重试。请联系管理员更新系统配置。
```

### AI对话失败提示
```
抱歉，[服务名称]服务的API密钥已失效，请联系管理员更新系统配置。
```

示例：
```
抱歉，DeepSeek服务的API密钥已失效，请联系管理员更新系统配置。
```

## 管理员操作建议

当用户反馈此错误时，管理员需要：

1. 登录后台管理系统
2. 进入系统配置页面
3. 找到对应的AI服务配置（如 OPENAI_API_KEY、spring.ai.lightcc.api-key 等）
4. 更新为有效的API Key
5. 重启服务（如需要）

## 配置文件位置

API Key配置在以下配置文件中：
- `application.yml`（主配置）
- 环境变量（如 OPENAI_API_KEY）

## 注意事项

1. **安全性**：错误提示中不会暴露具体的API Key值，只会提示需要更换
2. **日志记录**：后端会记录详细的错误日志，便于管理员排查问题
3. **降级机制**：图片生成服务支持多服务降级，如果LightCC失败会自动尝试OpenAI
4. **用户体验**：明确的中文提示，避免用户看到技术性的英文错误信息

## 测试建议

测试时可以使用以下方式模拟token失效：

1. 临时将API Key修改为无效值
2. 调用AI服务，验证是否显示正确的错误提示
3. 恢复正确的API Key，验证服务恢复正常

## 相关文件

- `ruoyi-admin/src/main/java/com/ruoyi/web/service/CustomerAiImageService.java`
- `ruoyi-admin/src/main/java/com/ruoyi/web/service/CustomerAiDialogueService.java`
- `guanghe-studio/src/composables/useImageGeneration.js`
- `guanghe-studio/src/components/AiAssistant.vue`

## 更新日期

2026-07-30