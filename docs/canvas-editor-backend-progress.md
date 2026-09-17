# Canvas Editor 后端开发进度看板

**更新时间**: 2026-09-07
**整体进度**: 10% (API框架完成，业务逻辑待实现)

---

## 📊 进度总览

```
前端集成:  ████████████████████ 100% ✅
API框架:   ████████████████████ 100% ✅
业务逻辑:  ██░░░░░░░░░░░░░░░░░░  10% ⏳
测试:      ░░░░░░░░░░░░░░░░░░░░   0% ⏳
```

---

## ✅ P0 功能 (必须实现)

### 1. 删除、下载、图层调整
**状态**: ✅ 前端已完成，无需后端
**说明**: 这些功能完全在前端实现，不需要后端API

---

### 2. 图片扩图 (extendImage)
**状态**: ⏳ 待实现
**优先级**: 🔴 最高
**难度**: ⭐⭐⭐
**预计时间**: 4-6小时

**API端点**: `POST /ai/image/extend`

**Request**:
```json
{
  "imageUrl": "https://...",
  "ratio": "16:9",
  "width": 1920,
  "height": 1080
}
```

**Response**:
```json
{
  "url": "https://...",
  "width": 1920,
  "height": 1080,
  "ratio": "16:9"
}
```

**实现步骤**:
- [ ] 1. 下载原图
- [ ] 2. 计算扩展画布尺寸
- [ ] 3. 调用Vertex AI进行外绘
- [ ] 4. 上传结果图片
- [ ] 5. 返回新图片URL

**技术方案**:
- 使用gemini-3-pro-image模型
- System prompt: "Extend the canvas of this image to {ratio} ratio, keeping the original content intact and generating appropriate background on the extended areas."
- 参考: CustomerAiImageService.generateImages() 方法

---

### 3. 多角度生成 (generateMultiAngle)
**状态**: ⏳ 待实现
**优先级**: 🔴 最高
**难度**: ⭐⭐⭐⭐
**预计时间**: 6-8小时

**API端点**: `POST /ai/image/multi-angle`

**Request**:
```json
{
  "imageUrl": "https://...",
  "count": 4,
  "type": "rotate"
}
```

**Response**:
```json
{
  "images": [
    {"url": "https://...", "angle": "0°", "type": "rotate"},
    {"url": "https://...", "angle": "45°", "type": "rotate"},
    ...
  ]
}
```

**实现步骤**:
- [ ] 1. 解析角度类型和数量
- [ ] 2. 生成对应角度的提示词
- [ ] 3. 并行调用Vertex AI生成图片
- [ ] 4. 上传所有结果
- [ ] 5. 返回图片URL列表

**角度类型**:
- `rotate`: 0°, 45°, 90°, 135°, 180°, 225°, 270°, 315°
- `multi-view`: front, side, top, back
- `360`: 全景视角

---

## ⏳ P1 功能 (重要)

### 4. OCR识别 (recognizeText)
**状态**: ⏳ 待实现
**优先级**: 🟡 中
**难度**: ⭐⭐⭐
**预计时间**: 3-4小时

**依赖**: 支持"修改文字"功能

**API端点**: `POST /ai/image/ocr`

---

### 5. 局部重绘 (partialRedraw)
**状态**: ⏳ 待实现
**优先级**: 🟡 中
**难度**: ⭐⭐⭐⭐
**预计时间**: 6-8小时

**API端点**: `POST /ai/image/partial-redraw`

---

### 6. 编辑历史 (getEditHistory)
**状态**: ⏳ 待实现
**优先级**: 🟡 中
**难度**: ⭐⭐
**预计时间**: 2-3小时

**需要**: 创建数据库表 `canvas_edit_history`

---

### 7. 撤销编辑 (revertImage)
**状态**: ⏳ 待实现
**优先级**: 🟡 中
**难度**: ⭐⭐⭐
**预计时间**: 3-4小时

---

## 📋 P2 功能 (增强)

### 8. 修改文字 (editImageText)
**状态**: ⏳ 待实现
**优先级**: 🟢 低
**难度**: ⭐⭐⭐⭐⭐
**预计时间**: 8-12小时

**依赖**: OCR识别

---

### 9. 图层检测 (detectLayers)
**状态**: ⏳ 待实现
**优先级**: 🟢 低
**难度**: ⭐⭐⭐⭐⭐
**预计时间**: 8-12小时

---

### 10. 下载图层 (downloadLayers)
**状态**: ⏳ 待实现
**优先级**: 🟢 低
**难度**: ⭐⭐⭐
**预计时间**: 4-6小时

---

## 📝 实现计划

### 本周目标 (Day 1-2)
- [x] ✅ API框架搭建
- [ ] 实现图片扩图功能
- [ ] 实现多角度生成功能
- [ ] 单元测试

### 下周目标 (Day 3-5)
- [ ] 实现OCR识别功能
- [ ] 实现局部重绘功能
- [ ] 实现编辑历史和撤销
- [ ] 集成测试

### 后续优化 (Day 6-10)
- [ ] 实现修改文字功能
- [ ] 实现图层检测功能
- [ ] 性能优化
- [ ] 文档完善

---

## 🔧 技术债务

### 待优化项
- [ ] 图片缓存机制
- [ ] 异步任务队列
- [ ] 错误重试机制
- [ ] 限流和降级
- [ ] 日志和监控

### 待实现辅助功能
- [ ] getAccessToken() - 获取Vertex AI令牌
- [ ] callVertexAi() - 调用Vertex AI
- [ ] downloadImage() - 下载图片
- [ ] uploadImage() - 上传图片

---

## 📊 统计数据

| 功能 | 状态 | 难度 | 预计时间 |
|------|------|------|---------|
| API框架 | ✅ 完成 | - | ✅ |
| 删除/下载/图层调整 | ✅ 前端完成 | ⭐ | ✅ |
| 图片扩图 | ⏳ 待实现 | ⭐⭐⭐ | 4-6h |
| 多角度生成 | ⏳ 待实现 | ⭐⭐⭐⭐ | 6-8h |
| OCR识别 | ⏳ 待实现 | ⭐⭐⭐ | 3-4h |
| 局部重绘 | ⏳ 待实现 | ⭐⭐⭐⭐ | 6-8h |
| 编辑历史 | ⏳ 待实现 | ⭐⭐ | 2-3h |
| 撤销编辑 | ⏳ 待实现 | ⭐⭐⭐ | 3-4h |
| 修改文字 | ⏳ 待实现 | ⭐⭐⭐⭐⭐ | 8-12h |
| 图层检测 | ⏳ 待实现 | ⭐⭐⭐⭐⭐ | 8-12h |
| 下载图层 | ⏳ 待实现 | ⭐⭐⭐ | 4-6h |

**已完成**: 2/11 (18%)
**进行中**: 0/11 (0%)
**待开始**: 9/11 (82%)

---

## 📚 相关文档

- [后端开发规范](./canvas-editor-api-spec.md)
- [API接口定义](../../guanghe-studio/src/api/canvasEditor.js)
- [Controller代码](../../ruoyi-admin/src/main/java/com/ruoyi/web/controller/customer/CanvasEditorController.java)
- [Service代码](../../ruoyi-admin/src/main/java/com/ruoyi/web/service/CanvasEditorService.java)
- [参考实现](../../ruoyi-admin/src/main/java/com/ruoyi/web/service/CustomerAiImageService.java)

---

**最后更新**: 2026-09-07
**负责人**: Claude Code
**下次更新**: 实现图片扩图功能后
