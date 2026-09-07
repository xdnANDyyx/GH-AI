# 画布编辑器迁移指南

## 📋 概述

本指南帮助你将现有的画布实现迁移到新的CanvasEditor组件。

## 🎯 迁移范围

**涉及模块**（9个）:
- WhiteBg.vue (白底图)
- Background.vue (背景图)
- Retouch.vue (产品精修)
- AiModel.vue (AI模特)
- HeroImage.vue (主图设计)
- DetailImg.vue (详情图A+)
- Banner.vue (Banner设计)
- SizeMark.vue (尺寸标记)
- BatchProcess.vue (批量处理)

## ⚠️ 迁移前检查

### 1. 确认基础组件已创建

```bash
# 检查以下文件是否存在
ls guanghe-studio/src/components/CanvasEditor.vue
ls guanghe-studio/src/composables/useCanvasEditor.js
ls guanghe-studio/src/store/modules/image.js
ls guanghe-studio/src/api/canvasEditor.js
```

如果不存在，请先创建这些基础组件。

### 2. 备份当前代码

```bash
# 备份所有工作台模块
cp -r guanghe-studio/src/views/workspace guanghe-studio/src/views/workspace.backup
```

### 3. 确认现有功能正常

确保以下功能在迁移前正常工作：
- [ ] 图片生成
- [ ] 配置面板
- [ ] AI助手
- [ ] 历史记录

## 🚀 快速迁移（推荐）

### 方法一：使用模板（最快）

1. **阅读快速参考**
   ```bash
   cat docs/canvas-editor-quick-reference.md
   ```

2. **查看示例**
   ```bash
   cat docs/white-bg-integration-example.md
   ```

3. **按模板修改**
   ```bash
   cat docs/canvas-editor-integration-template.md
   ```

4. **逐个集成9个模块**

### 方法二：查看集成检查清单（最全面）

```bash
cat docs/canvas-editor-checklist.md
```

## 📝 详细迁移步骤

### 步骤 1: 集成第一个模块（学习阶段）

**推荐从 WhiteBg.vue 开始**

```bash
# 1. 打开WhiteBg.vue
code guanghe-studio/src/views/workspace/WhiteBg.vue

# 2. 按照模板修改
# - 导入CanvasEditor
# - 替换画布区域
# - 添加事件处理函数

# 3. 测试原有功能
# - 生成图片
# - 配置面板
# - AI助手
```

### 步骤 2: 理解核心改动

**旧代码**:
```vue
<template>
  <div class="canvas-box">
    <div v-if="hasResult" class="canvas-result">
      <el-image :src="resultImages[0].url" />
    </div>
  </div>
</template>
```

**新代码**:
```vue
<template>
  <div class="canvas-box">
    <CanvasEditor
      :images="resultImages"
      feature-name="white_bg"
      @extend="handleExtend"
      @multi-angle="handleMultiAngle"
      @edit-text="handleEditText"
      @partial-redraw="handlePartialRedraw"
      @explode-layers="handleExplodeLayers"
      @delete="handleDeleteImage"
      @send-to-retouch="handleSendToRetouch"
      @send-to-white-bg="handleSendToWhiteBg"
      @download="handleDownload"
      @move-up="handleMoveUp"
      @move-down="handleMoveDown"
      @bring-to-front="handleBringToFront"
      @send-to-back="handleSendToBack"
    />
  </div>
</template>
```

**关键区别**:
- 用CanvasEditor组件替换简单的el-image
- 原有的resultImages继续使用，无需改变
- 需要添加13个事件处理函数

### 步骤 3: 集成剩余8个模块

按照 **步骤1** 的模式，逐个集成：

1. Background.vue
2. Retouch.vue
3. AiModel.vue
4. HeroImage.vue
5. DetailImg.vue
6. Banner.vue
7. SizeMark.vue
8. BatchProcess.vue

**每个模块的改动**:
- 3处导入语句（组件、store、API）
- 1处画布区域替换
- 13个事件处理函数
- 1个Feature Name（每个模块不同）

### 步骤 4: 调整差异部分

每个模块可能需要微调：

#### 差异 1: Feature Name

| 模块 | Feature Name |
|------|-------------|
| WhiteBg.vue | `white_bg` |
| Background.vue | `change_bg` |
| Retouch.vue | `retouch` |
| AiModel.vue | `ai_model` |
| HeroImage.vue | `main_image` |
| DetailImg.vue | `detail_img` |
| Banner.vue | `banner` |
| SizeMark.vue | `dimension` |
| BatchProcess.vue | `batch` |

#### 差异 2: 跨模块跳转

**白底图 → 产品精修**:
```javascript
const handleSendToRetouch = (img) => {
  imageStore.setPendingImages([img], 'white_bg')
  router.push('/workspace/retouch')  // 需要添加路由跳转
}
```

**产品精修 → 白底图**:
```javascript
const handleSendToWhiteBg = (img) => {
  imageStore.setPendingImages([img], 'retouch')
  router.push('/workspace/white-bg')
}
```

#### 差异 3: 图片数据格式

如果某个模块的resultImages不是数组，需要修改：

```javascript
// 如果原来是字符串
const resultImages = ref('https://xxx.com/img.png')

// 改为数组
const resultImages = ref([
  { url: 'https://xxx.com/img.png', name: '生成的图片' }
])
```

## 🧪 测试策略

### 1. 单元测试

```bash
# 运行现有测试
npm run test:unit
```

### 2. 集成测试（每个模块）

**必测项**:
- [ ] 图片生成是否正常
- [ ] 配置面板是否正常
- [ ] AI助手是否正常
- [ ] 画布显示是否正常
- [ ] 点击图片是否显示工具栏
- [ ] 右键菜单是否正常
- [ ] 删除功能是否正常
- [ ] 下载功能是否正常
- [ ] 图层调整是否正常

### 3. 跨模块测试

- [ ] 从模块A发送图片到模块B
- [ ] 从模块B接收图片正常显示

## 🔄 回滚方案

如果迁移后出现问题，可以快速回滚：

```bash
# 1. 恢复到备份
cp -r guanghe-studio/src/views/workspace.backup/* guanghe-studio/src/views/workspace/

# 2. 或者使用git回滚
git checkout -- guanghe-studio/src/views/workspace/

# 3. 重新构建
npm run build
```

## 📊 迁移进度追踪

### 模块集成进度

| 模块 | 状态 | 测试状态 | 备注 |
|------|------|---------|------|
| WhiteBg.vue | ⏳ | ⏳ | 推荐第一个集成 |
| Background.vue | ⏳ | ⏳ | |
| Retouch.vue | ⏳ | ⏳ | |
| AiModel.vue | ⏳ | ⏳ | |
| HeroImage.vue | ⏳ | ⏳ | |
| DetailImg.vue | ⏳ | ⏳ | |
| Banner.vue | ⏳ | ⏳ | |
| SizeMark.vue | ⏳ | ⏳ | |
| BatchProcess.vue | ⏳ | ⏳ | |

**图例**:
- ✅ 已完成并测试通过
- ⏳ 进行中
- ❌ 阻塞/有问题

## ⚡ 快速迁移命令

```bash
# 1. 检查基础组件
ls guanghe-studio/src/components/CanvasEditor.vue

# 2. 备份现有代码
cp -r guanghe-studio/src/views/workspace guanghe-studio/src/views/workspace.backup

# 3. 逐个集成模块（以WhiteBg.vue为例）
code guanghe-studio/src/views/workspace/WhiteBg.vue

# 4. 测试
npm run dev

# 5. 集成下一个模块
# ... 重复步骤3-4
```

## 🐛 常见问题

### Q1: 工具栏显示不正常

**解决**: 检查CSS样式是否有冲突，确保.el-image-list样式正确

### Q2: 右键菜单位置偏移

**解决**: CanvasEditor已经处理了相对定位，如果仍有问题检查父元素的position属性

### Q3: 图片不显示

**解决**: 确认resultImages是数组格式且url正确

### Q4: 原有的生成功能失效

**解决**: 确保只替换了画布区域，其他代码未修改

### Q5: 事件没有触发

**解决**: 检查事件名称拼写（如@extend不是@extends）

## 📞 需要帮助？

1. 查看 [快速参考](canvas-editor-quick-reference.md)
2. 查看 [集成示例](white-bg-integration-example.md)
3. 查看 [检查清单](canvas-editor-checklist.md)
4. 检查 [API文档](canvas-editor-api-spec.md)

---

**最后更新**: 2026-09-07
