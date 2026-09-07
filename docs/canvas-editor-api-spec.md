# 画布编辑器 API 开发规范

## 📋 API列表

### 1. 图片扩图

**Endpoint**: `POST /ai/image/extend`

**功能**: 扩展图片画布，支持预设比例或自定义尺寸

**Request**:
```json
{
  "imageUrl": "string",     // 图片URL（必填）
  "ratio": "string",        // 预设比例，如 "16:9"（可选）
  "width": "number",        // 自定义宽度（可选）
  "height": "number"        // 自定义高度（可选）
}
```

**Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "url": "string",        // 扩展后的图片URL
    "width": "number",
    "height": "number",
    "ratio": "string"
  }
}
```

**预设比例列表**:
- `1:1` - 正方形
- `3:4` - 竖版（小红书）
- `9:16` - 竖版（短视频）
- `16:9` - 横版（YouTube）
- `4:3` - 横版（传统）
- `2:3` - 竖版（Instagram）
- `3:2` - 横版（摄影）

---

### 2. 多角度生成

**Endpoint**: `POST /ai/image/multi-angle`

**功能**: 基于原图生成多个不同角度的产品图

**Request**:
```json
{
  "imageUrl": "string",     // 原图URL（必填）
  "count": "number",        // 生成数量 2-8（必填）
  "type": "string"          // 角度类型（必填）
}
```

**Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "images": [
      {
        "url": "string",
        "angle": "string",  // 角度描述
        "type": "string"
      }
    ]
  }
}
```

**角度类型**:
- `rotate` - 旋转角度（0°, 45°, 90°, 135°, 180°, 225°, 270°, 315°）
- `multi-view` - 多视图（前视、侧视、俯视、后视）
- `360` - 360度全景

---

### 3. 修改文字

**Endpoint**: `POST /ai/image/edit-text`

**功能**: 修改图片中的文字内容

**Request**:
```json
{
  "imageUrl": "string",         // 图片URL（必填）
  "originalText": "string",     // 原文字（必填）
  "newText": "string",          // 新文字（必填）
  "font": "string"              // 字体（可选）
}
```

**Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "url": "string",            // 修改后的图片URL
    "originalText": "string",
    "newText": "string",
    "font": "string"
  }
}
```

**可用字体**（可选）:
- `PingFang SC` (苹方)
- `Microsoft YaHei` (微软雅黑)
- `SimSun` (宋体)
- `SimHei` (黑体)
- `KaiTi` (楷体)
- `Arial`
- `Times New Roman`

---

### 4. 局部重绘

**Endpoint**: `POST /ai/image/partial-redraw`

**功能**: 选择区域并描述需要重绘的内容

**Request**:
```json
{
  "imageUrl": "string",         // 图片URL（必填）
  "description": "string",      // 重绘描述（必填）
  "mask": "object"              // 重绘区域mask（必填）
}
```

**Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "url": "string",            // 重绘后的图片URL
    "description": "string"
  }
}
```

**Mask格式**:
```json
{
  "x": "number",        // 左上角X坐标（相对坐标 0-1）
  "y": "number",        // 左上角Y坐标（相对坐标 0-1）
  "width": "number",    // 宽度（相对坐标 0-1）
  "height": "number"    // 高度（相对坐标 0-1）
}
```

**实现建议**:
- 在前端使用Canvas绘制mask区域
- 将mask坐标转换为相对坐标（除以图片宽高）
- 后端将相对坐标转换为绝对坐标

---

### 5. 图层检测

**Endpoint**: `POST /ai/image/detect-layers`

**功能**: 检测图片中的不同图层

**Request**:
```json
{
  "imageUrl": "string"     // 图片URL（必填）
}
```

**Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "layers": [
      {
        "id": "string",
        "name": "string",
        "url": "string",
        "position": {
          "x": "number",
          "y": "number"
        },
        "size": {
          "width": "number",
          "height": "number"
        },
        "type": "string"      // layer | text | shape | background
      }
    ]
  }
}
```

**图层类型**:
- `layer` - 普通图层
- `text` - 文字图层
- `shape` - 形状图层
- `background` - 背景图层

---

### 6. 下载图层

**Endpoint**: `POST /ai/image/download-layers`

**功能**: 下载检测到的图层文件

**Request**:
```json
{
  "imageUrl": "string",     // 原图URL（必填）
  "layers": "array",        // 图层ID列表（必填）
  "format": "string"        // 导出格式（必填）
}
```

**Response**:
- Content-Type: `application/zip` 或 `application/octet-stream`
- 下载包含所有选中图层的ZIP文件

**导出格式**:
- `psd` - Photoshop文件
- `png` - PNG图片序列
- `svg` - SVG矢量文件

---

### 7. OCR文字识别

**Endpoint**: `POST /ai/image/ocr`

**功能**: 识别图片中的文字

**Request**:
```json
{
  "imageUrl": "string"     // 图片URL（必填）
}
```

**Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "texts": [
      {
        "text": "string",          // 识别到的文字
        "confidence": "number",    // 置信度 0-1
        "position": {
          "x": "number",
          "y": "number"
        },
        "size": {
          "width": "number",
          "height": "number"
        }
      }
    ]
  }
}
```

---

### 8. 编辑历史

**Endpoint**: `GET /ai/image/:imageId/history`

**功能**: 查询图片的编辑历史

**Request**:
- Path Parameter: `imageId` (string) - 图片ID

**Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "imageId": "string",
    "history": [
      {
        "version": "number",
        "operation": "string",     // extend | multi-angle | edit-text | partial-redraw
        "timestamp": "string",     // ISO 8601格式
        "params": "object",
        "resultUrl": "string"
      }
    ]
  }
}
```

---

### 9. 撤销编辑

**Endpoint**: `POST /ai/image/revert`

**功能**: 撤销图片的某次编辑

**Request**:
```json
{
  "imageId": "string",      // 图片ID（必填）
  "version": "number"       // 版本号（必填）
}
```

**Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "url": "string",        // 撤销后的图片URL
    "version": "number",
    "timestamp": "string"
  }
}
```

---

## 🔧 通用规范

### 响应格式

所有API必须返回统一格式：

```json
{
  "code": 200,              // HTTP状态码
  "message": "success",     // 状态消息
  "data": {} | [] | string  // 响应数据
}
```

### 错误处理

```json
{
  "code": 400,
  "message": "Invalid parameters",
  "error": "imageUrl is required"
}
```

**错误码**:
- `200` - 成功
- `400` - 参数错误
- `401` - 未授权
- `403` - 禁止访问
- `404` - 资源不存在
- `500` - 服务器错误

### 认证

所有API需要JWT认证：

```
Authorization: Bearer {token}
```

### 限流

建议实现限流：
- 每个用户每分钟最多60次请求
- 返回429状态码

---

## 🎨 AI功能实现建议

### 图片扩图

**推荐方案**:
1. 使用 `gemini-2.0-flash-exp-image-generation`
2. 系统提示词：根据ratio/width/height扩展画布
3. 支持外绘（outpainting）

**提示词模板**:
```
Extend this image to {ratio} format, maintaining the original content
and adding appropriate background/context on the sides.
```

### 多角度生成

**推荐方案**:
1. 使用 `gemini-2.0-flash-exp-image-generation`
2. 根据angle type生成不同角度
3. 保持产品特征一致

**提示词模板**:
```
Generate {count} different angles of this product from {type} perspective.
Maintain the product's key features and lighting consistency.
```

### 改文字

**推荐方案**:
1. 使用OCR识别文字位置
2. 使用 `gemini-2.0-flash-exp-image-generation` 替换文字
3. 保持字体风格一致

**实现步骤**:
1. 前端调用OCR API获取文字位置
2. 在选中位置生成新文字
3. 使用Inpainting替换原文字

### 局部重绘

**推荐方案**:
1. 使用 `gemini-2.0-flash-exp-image-generation`
2. 传入mask区域
3. 根据description生成内容

**提示词模板**:
```
In the highlighted area (marked), {description}.
Keep the rest of the image exactly the same.
```

### 图层检测

**推荐方案**:
1. 使用Computer Vision算法
2. 或调用第三方图层分离服务
3. 返回每个图层的边界和类型

---

## 🧪 测试用例

### 扩图测试

```bash
curl -X POST http://localhost:8080/ai/image/extend \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "https://example.com/product.jpg",
    "ratio": "16:9"
  }'
```

### 多角度测试

```bash
curl -X POST http://localhost:8080/ai/image/multi-angle \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "https://example.com/product.jpg",
    "count": 4,
    "type": "rotate"
  }'
```

---

## 📝 注意事项

1. **图片格式** - 支持 JPG、PNG、WebP
2. **图片大小** - 单张不超过10MB
3. **超时时间** - AI生成建议60-120秒超时
4. **异步处理** - 复杂任务建议使用异步队列
5. **图片缓存** - 建议缓存生成结果
6. **错误重试** - AI服务不稳定时建议重试3次

---

## 🔗 参考文档

- [CanvasEditor API接口定义](../../guanghe-studio/src/api/canvasEditor.js)
- [前端集成指南](./canvas-editor-integration.md)
- [Vertex AI文档](https://cloud.google.com/vertex-ai)

---

**最后更新**: 2026-09-07
