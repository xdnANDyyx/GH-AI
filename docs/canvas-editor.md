# 画布编辑器 (Canvas Editor)

为前台工作台9个图片生成模块提供统一的画布编辑功能，参考即梦AI的交互设计。

## 📚 文档导航

### 🚀 快速开始

- **[快速参考](canvas-editor-quick-reference.md)** ⭐ **从这里开始！**
  - 3步集成流程
  - Feature Name映射表
  - 完整代码示例
  - API参考

### 📖 详细文档

- **[功能总结](canvas-editor-summary.md)**
  - 完整功能清单
  - 已完成/待完成工作
  - 技术栈说明
  - 交付文件列表

- **[集成指南](canvas-editor-integration.md)**
  - 详细集成步骤
  - 跨模块跳转实现
  - 后端API开发规范
  - 注意事项

- **[集成示例](white-bg-integration-example.md)**
  - WhiteBg.vue完整示例
  - 代码前后对比
  - 事件处理函数详解

- **[集成检查清单](canvas-editor-checklist.md)**
  - 9个模块的集成进度跟踪
  - 后端API开发清单
  - 测试检查清单
  - 上线前检查清单

## ✨ 核心功能

### 点击图片工具栏（5个功能）

1. **扩图** - 扩展图片画布，支持预设比例或自定义尺寸
2. **多角度** - 基于原图生成多个不同角度的产品图
3. **改文字** - 修改图片中的文字内容
4. **局部重绘** - 选择区域并描述需要重绘的内容
5. **图层炸开** - 检测并分离图片中的不同图层

### 右键菜单（8个功能）

1. **删除** - 删除当前图片
2. **放入产品精修** - 将图片发送到产品精修模块
3. **放入白底图** - 将图片发送到白底图模块
4. **下载图片** - 下载当前图片到本地
5. **向上移一层** - 将图片层级上移
6. **向下移一层** - 将图片层级下移
7. **置顶** - 将图片移动到最上层
8. **置底** - 将图片移动到底层

## 🎯 支持的模块

| # | 模块 | 文件 | Feature Name | 集成状态 |
|---|------|------|-------------|---------|
| 1 | 白底图 | WhiteBg.vue | `white_bg` | ⏳ 待集成 |
| 2 | 背景图 | Background.vue | `change_bg` | ⏳ 待集成 |
| 3 | 产品精修 | Retouch.vue | `retouch` | ⏳ 待集成 |
| 4 | AI模特 | AiModel.vue | `ai_model` | ⏳ 待集成 |
| 5 | 主图设计 | HeroImage.vue | `main_image` | ⏳ 待集成 |
| 6 | 详情图A+ | DetailImg.vue | `detail_img` | ⏳ 待集成 |
| 7 | Banner设计 | Banner.vue | `banner` | ⏳ 待集成 |
| 8 | 尺寸标记 | SizeMark.vue | `dimension` | ⏳ 待集成 |
| 9 | 批量处理 | BatchProcess.vue | `batch` | ⏳ 待集成 |

## 🏗️ 项目结构

```
guanghe-studio/src/
├── components/
│   └── CanvasEditor.vue          # 画布编辑器组件 (800+ 行)
├── composables/
│   └── useCanvasEditor.js        # 画布编辑逻辑封装
├── store/modules/
│   └── image.js                  # 图片状态管理 (Pinia)
├── api/
│   └── canvasEditor.js           # API接口定义 (9个端点)

docs/
├── canvas-editor-quick-reference.md   # 快速参考 ⭐
├── canvas-editor-summary.md          # 功能总结
├── canvas-editor-integration.md      # 集成指南
├── white-bg-integration-example.md   # 集成示例
└── canvas-editor-checklist.md        # 检查清单
```

## 🛠️ 技术栈

- **前端框架**: Vue 3 (Composition API)
- **UI组件**: Element Plus
- **状态管理**: Pinia
- **API请求**: Axios
- **样式**: SCSS

## 📦 已完成的文件

### 前端组件

1. ✅ `CanvasEditor.vue` - 通用画布编辑器组件
2. ✅ `useCanvasEditor.js` - 画布编辑Composable
3. ✅ `image.js` - Pinia图片状态管理
4. ✅ `canvasEditor.js` - API接口定义

### 文档

1. ✅ `canvas-editor-quick-reference.md` - 快速参考
2. ✅ `canvas-editor-summary.md` - 功能总结
3. ✅ `canvas-editor-integration.md` - 集成指南
4. ✅ `white-bg-integration-example.md` - 集成示例
5. ✅ `canvas-editor-checklist.md` - 检查清单

## 🔄 工作流程

### 前端集成流程

```
1. 阅读快速参考
   ↓
2. 查看WhiteBg.vue集成示例
   ↓
3. 按照Feature Name映射表集成到对应模块
   ↓
4. 实现事件处理函数
   ↓
5. 测试原有功能是否正常
```

### 后端开发流程

```
1. 阅读集成指南中的API规范
   ↓
2. 开发9个API端点
   ↓
3. 测试API接口
   ↓
4. 前后端联调
```

## 🎨 交互设计

### 图片选择

- 点击图片 → 显示编辑工具栏
- 点击工具栏按钮 → 打开对应功能对话框
- 再次点击图片 → 关闭工具栏

### 右键菜单

- 右键点击图片 → 在鼠标位置显示操作菜单
- 点击菜单项 → 执行对应操作
- 点击其他位置 → 关闭菜单

### 键盘快捷键

- `Delete` - 删除选中的图片
- `Escape` - 关闭对话框/取消选择

## ⚡ 状态说明

| 图标 | 状态 | 说明 |
|------|------|------|
| ✅ | 已完成 | 功能已完成并测试通过 |
| ⏳ | 待完成 | 正在开发或待开发 |
| ❌ | 阻塞 | 遇到问题需要解决 |

## 📊 当前进度

**总体进度**: 20%

- **前端组件**: ✅ 100% 完成
- **文档**: ✅ 100% 完成
- **模块集成**: ⏳ 0% 待完成 (9个模块)
- **后端API**: ⏳ 0% 待开发 (9个端点)

## 🚀 下一步

### 优先级 1：模块集成

1. 集成CanvasEditor到9个工作台模块
2. 测试每个模块的原有功能
3. 确保画布显示正常

### 优先级 2：后端开发

1. 开发图片扩图API
2. 开发多角度生成API
3. 开发其他编辑功能API

### 优先级 3：功能完善

1. 实现扩图功能完整逻辑
2. 实现多角度生成功能
3. 集成OCR文字识别
4. 实现局部重绘功能
5. 实现图层炸开功能

## 💡 使用提示

### 首次使用？

👉 **请先阅读 [快速参考](canvas-editor-quick-reference.md)**，里面包含完整的代码示例和3步集成流程。

### 需要详细说明？

👉 查看 [集成指南](canvas-editor-integration.md)，了解完整的集成步骤和注意事项。

### 需要看实际例子？

👉 查看 [WhiteBg.vue集成示例](white-bg-integration-example.md)，看一个完整的模块是如何改写的。

### 需要追踪进度？

👉 查看 [检查清单](canvas-editor-checklist.md)，了解当前进度和待办事项。

## 📞 支持

如有问题，请按顺序查看：
1. 快速参考
2. 集成指南
3. 集成示例
4. 检查清单

---

**创建日期**: 2026-09-07
**版本**: v1.0.0
**状态**: 🟡 基础组件已完成，待集成到各模块
