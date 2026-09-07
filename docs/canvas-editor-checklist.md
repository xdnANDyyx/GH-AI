# 画布编辑器集成检查清单

## 📋 集成前准备

- [ ] 阅读 `docs/canvas-editor-integration.md`
- [ ] 阅读 `docs/white-bg-integration-example.md`
- [ ] 确认 `CanvasEditor.vue` 组件已创建
- [ ] 确认 `useCanvasEditor.js` 已创建
- [ ] 确认 `image.js` store已创建
- [ ] 确认 `canvasEditor.js` API已创建

## 🎯 模块集成检查清单

### 1. 白底图 (WhiteBg.vue)

- [ ] 导入 CanvasEditor 组件
- [ ] 导入 useImageStore
- [ ] 导入 canvasEditor API
- [ ] 替换画布区域代码
- [ ] 实现 handleExtend 函数
- [ ] 实现 handleMultiAngle 函数
- [ ] 实现 handleEditText 函数
- [ ] 实现 handlePartialRedraw 函数
- [ ] 实现 handleExplodeLayers 函数
- [ ] 实现 handleDeleteImage 函数
- [ ] 实现 handleSendToRetouch 函数
- [ ] 实现 handleSendToWhiteBg 函数
- [ ] 实现 handleDownload 函数
- [ ] 实现 handleMoveUp/Down/Front/Back 函数
- [ ] 测试生成功能是否正常
- [ ] 测试图片显示是否正常
- [ ] 测试右键菜单是否正常

**Feature Name**: `white_bg`

---

### 2. 背景图 (Background.vue)

- [ ] 导入 CanvasEditor 组件
- [ ] 导入 useImageStore
- [ ] 导入 canvasEditor API
- [ ] 替换画布区域代码
- [ ] 实现所有事件处理函数
- [ ] 测试生成功能是否正常
- [ ] 测试图片显示是否正常
- [ ] 测试右键菜单是否正常

**Feature Name**: `change_bg`

---

### 3. 产品精修 (Retouch.vue)

- [ ] 导入 CanvasEditor 组件
- [ ] 导入 useImageStore
- [ ] 导入 canvasEditor API
- [ ] 替换画布区域代码
- [ ] 实现所有事件处理函数
- [ ] 测试生成功能是否正常
- [ ] 测试图片显示是否正常
- [ ] 测试右键菜单是否正常

**Feature Name**: `retouch`

---

### 4. AI模特 (AiModel.vue)

- [ ] 导入 CanvasEditor 组件
- [ ] 导入 useImageStore
- [ ] 导入 canvasEditor API
- [ ] 替换画布区域代码
- [ ] 实现所有事件处理函数
- [ ] 测试生成功能是否正常
- [ ] 测试图片显示是否正常
- [ ] 测试右键菜单是否正常

**Feature Name**: `ai_model`

---

### 5. 主图设计 (HeroImage.vue)

- [ ] 导入 CanvasEditor 组件
- [ ] 导入 useImageStore
- [ ] 导入 canvasEditor API
- [ ] 替换画布区域代码
- [ ] 实现所有事件处理函数
- [ ] 测试生成功能是否正常
- [ ] 测试图片显示是否正常
- [ ] 测试右键菜单是否正常

**Feature Name**: `main_image`

---

### 6. 详情图A+ (DetailImg.vue)

- [ ] 导入 CanvasEditor 组件
- [ ] 导入 useImageStore
- [ ] 导入 canvasEditor API
- [ ] 替换画布区域代码
- [ ] 实现所有事件处理函数
- [ ] 测试生成功能是否正常
- [ ] 测试图片显示是否正常
- [ ] 测试右键菜单是否正常

**Feature Name**: `detail_img`

---

### 7. Banner设计 (Banner.vue)

- [ ] 导入 CanvasEditor 组件
- [ ] 导入 useImageStore
- [ ] 导入 canvasEditor API
- [ ] 替换画布区域代码
- [ ] 实现所有事件处理函数
- [ ] 测试生成功能是否正常
- [ ] 测试图片显示是否正常
- [ ] 测试右键菜单是否正常

**Feature Name**: `banner`

---

### 8. 尺寸标记 (SizeMark.vue)

- [ ] 导入 CanvasEditor 组件
- [ ] 导入 useImageStore
- [ ] 导入 canvasEditor API
- [ ] 替换画布区域代码
- [ ] 实现所有事件处理函数
- [ ] 测试生成功能是否正常
- [ ] 测试图片显示是否正常
- [ ] 测试右键菜单是否正常

**Feature Name**: `dimension`

---

### 9. 批量处理 (BatchProcess.vue)

- [ ] 导入 CanvasEditor 组件
- [ ] 导入 useImageStore
- [ ] 导入 canvasEditor API
- [ ] 替换画布区域代码
- [ ] 实现所有事件处理函数
- [ ] 测试生成功能是否正常
- [ ] 测试图片显示是否正常
- [ ] 测试右键菜单是否正常

**Feature Name**: `batch`

---

## 🔧 后端API开发检查清单

- [ ] 开发图片扩图API (`/ai/image/extend`)
- [ ] 开发多角度生成API (`/ai/image/multi-angle`)
- [ ] 开发修改文字API (`/ai/image/edit-text`)
- [ ] 开发局部重绘API (`/ai/image/partial-redraw`)
- [ ] 开发图层检测API (`/ai/image/detect-layers`)
- [ ] 开发下载图层API (`/ai/image/download-layers`)
- [ ] 开发OCR识别API (`/ai/image/ocr`)
- [ ] 开发编辑历史API (`/ai/image/:id/history`)
- [ ] 开发撤销编辑API (`/ai/image/revert`)
- [ ] 测试所有API接口
- [ ] 编写API文档

## 🧪 测试检查清单

### 功能测试

- [ ] 测试所有9个模块的图片显示
- [ ] 测试点击图片显示工具栏
- [ ] 测试工具栏5个功能按钮
- [ ] 测试右键菜单8个功能
- [ ] 测试删除功能
- [ ] 测试下载功能
- [ ] 测试图层调整功能（上移/下移/置顶/置底）
- [ ] 测试跨模块跳转（产品精修、白底图）
- [ ] 测试原有生成功能是否正常
- [ ] 测试原有配置面板是否正常
- [ ] 测试原有AI助手是否正常

### UI测试

- [ ] 工具栏定位是否正确
- [ ] 右键菜单位置是否正确
- [ ] 对话框显示是否正常
- [ ] 动画效果是否流畅
- [ ] 响应式布局是否正常
- [ ] 移动端适配是否正常

### 兼容性测试

- [ ] Chrome浏览器
- [ ] Firefox浏览器
- [ ] Safari浏览器
- [ ] Edge浏览器

### 性能测试

- [ ] 大量图片（20+）显示性能
- [ ] 右键菜单响应速度
- [ ] 工具栏动画流畅度
- [ ] 内存占用情况

## 📝 文档检查清单

- [ ] 更新README.md
- [ ] 更新API文档
- [ ] 编写用户使用手册
- [ ] 编写开发者文档
- [ ] 更新CHANGELOG.md

## 🚀 上线前检查清单

- [ ] 所有模块集成完成
- [ ] 所有后端API开发完成
- [ ] 所有测试通过
- [ ] 代码审查完成
- [ ] 性能优化完成
- [ ] 文档编写完成
- [ ] 部署方案确认
- [ ] 回滚方案准备

## 📊 进度跟踪

**当前进度**: 20%

| 模块 | 集成状态 | API状态 | 测试状态 |
|------|---------|---------|---------|
| 白底图 | ⏳ | ⏳ | ⏳ |
| 背景图 | ⏳ | ⏳ | ⏳ |
| 产品精修 | ⏳ | ⏳ | ⏳ |
| AI模特 | ⏳ | ⏳ | ⏳ |
| 主图设计 | ⏳ | ⏳ | ⏳ |
| 详情图A+ | ⏳ | ⏳ | ⏳ |
| Banner设计 | ⏳ | ⏳ | ⏳ |
| 尺寸标记 | ⏳ | ⏳ | ⏳ |
| 批量处理 | ⏳ | ⏳ | ⏳ |

**图例**:
- ✅ 已完成
- ⏳ 进行中/待完成
- ❌ 阻塞/问题

---

最后更新: 2026-09-07
