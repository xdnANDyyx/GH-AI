# CanvasEditor 快速参考

## 🚀 快速集成（3步）

### 步骤 1: 导入依赖

```vue
<script setup>
import CanvasEditor from '@/components/CanvasEditor.vue'
import { useImageStore } from '@/store/modules/image'
import {
  extendImage,
  generateMultiAngle,
  editImageText,
  partialRedraw,
  detectLayers
} from '@/api/canvasEditor'
</script>
```

### 步骤 2: 替换画布区域

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

### 步骤 3: 添加事件处理函数

```vue
<script setup>
// 图片store
const imageStore = useImageStore()

// 编辑功能
const handleExtend = async (params) => {
  try {
    const res = await extendImage({
      imageUrl: params.image.url,
      ratio: params.ratio,
      width: params.width,
      height: params.height
    })
    ElMessage.success('扩图成功')
  } catch (error) {
    ElMessage.error('扩图失败')
  }
}

const handleMultiAngle = async (params) => {
  try {
    const res = await generateMultiAngle({
      imageUrl: params.image.url,
      count: params.count,
      type: params.type
    })
    ElMessage.success('多角度生成成功')
  } catch (error) {
    ElMessage.error('生成失败')
  }
}

const handleEditText = async (params) => {
  try {
    const res = await editImageText({
      imageUrl: params.image.url,
      originalText: params.originalText,
      newText: params.newText,
      font: params.font
    })
    ElMessage.success('文字修改成功')
  } catch (error) {
    ElMessage.error('修改失败')
  }
}

const handlePartialRedraw = async (params) => {
  try {
    const res = await partialRedraw({
      imageUrl: params.image.url,
      description: params.description,
      mask: params.mask
    })
    ElMessage.success('局部重绘成功')
  } catch (error) {
    ElMessage.error('重绘失败')
  }
}

const handleExplodeLayers = async (params) => {
  try {
    const res = await detectLayers({
      imageUrl: params.image.url
    })
    ElMessage.success('图层检测成功')
  } catch (error) {
    ElMessage.error('检测失败')
  }
}

// 右键菜单
const handleDeleteImage = async (index) => {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    resultImages.value.splice(index, 1)
    ElMessage.success('已删除')
  } catch {}
}

const handleSendToRetouch = (img) => {
  imageStore.setPendingImages([img], 'white_bg')
  ElMessage.success('已添加到产品精修')
}

const handleSendToWhiteBg = (img) => {
  ElMessage.success('已在白底图模块')
}

const handleDownload = (img) => {
  const url = img.url || img
  const link = document.createElement('a')
  link.href = url
  link.download = `image_${Date.now()}.png`
  link.click()
  ElMessage.success('开始下载')
}

const handleMoveUp = (index) => {
  if (index > 0) {
    const temp = resultImages.value[index]
    resultImages.value[index] = resultImages.value[index - 1]
    resultImages.value[index - 1] = temp
  }
}

const handleMoveDown = (index) => {
  if (index < resultImages.value.length - 1) {
    const temp = resultImages.value[index]
    resultImages.value[index] = resultImages.value[index + 1]
    resultImages.value[index + 1] = temp
  }
}

const handleBringToFront = (index) => {
  if (index < resultImages.value.length - 1) {
    const img = resultImages.value.splice(index, 1)[0]
    resultImages.value.push(img)
  }
}

const handleSendToBack = (index) => {
  if (index > 0) {
    const img = resultImages.value.splice(index, 1)[0]
    resultImages.value.unshift(img)
  }
}
</script>
```

## 📋 Feature Name 映射表

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

## 🎨 功能概览

### 点击图片工具栏（5个）

| 功能 | 说明 | API状态 |
|------|------|---------|
| 扩图 | 扩展图片画布 | ⏳ 待开发 |
| 多角度 | 生成多角度产品图 | ⏳ 待开发 |
| 改文字 | 修改图片文字 | ⏳ 待开发 |
| 局部重绘 | 选择区域重绘 | ⏳ 待开发 |
| 图层炸开 | 检测并分离图层 | ⏳ 待开发 |

### 右键菜单（8个）

| 功能 | 状态 | 说明 |
|------|------|------|
| 删除 | ✅ 已完成 | 删除当前图片 |
| 放入产品精修 | ⏳ 待完善 | 跨模块跳转 |
| 放入白底图 | ⏳ 待完善 | 跨模块跳转 |
| 下载图片 | ✅ 已完成 | 下载到本地 |
| 向上移一层 | ✅ 已完成 | 调整图层顺序 |
| 向下移一层 | ✅ 已完成 | 调整图层顺序 |
| 置顶 | ✅ 已完成 | 调整图层顺序 |
| 置底 | ✅ 已完成 | 调整图层顺序 |

## 🔧 CanvasEditor Props

| Prop | 类型 | 说明 | 必填 |
|------|------|------|------|
| images | Array | 图片数组 `[{url, name}]` | ✅ |
| featureName | String | 功能标识 | ✅ |

## 📡 CanvasEditor Events

### 点击工具栏事件

| Event | 参数 | 说明 |
|-------|------|------|
| extend | `{image, ratio, width, height}` | 扩图 |
| multi-angle | `{image, count, type}` | 多角度 |
| edit-text | `{image, originalText, newText, font}` | 改文字 |
| partial-redraw | `{image, description, mask}` | 局部重绘 |
| explode-layers | `{image}` | 图层炸开 |

### 右键菜单事件

| Event | 参数 | 说明 |
|-------|------|------|
| delete | index | 删除图片 |
| send-to-retouch | image | 发送到产品精修 |
| send-to-white-bg | image | 发送到白底图 |
| download | image | 下载图片 |
| move-up | index | 向上移一层 |
| move-down | index | 向下移一层 |
| bring-to-front | index | 置顶 |
| send-to-back | index | 置底 |

## 🎯 关键代码片段

### 图片数据结构

```javascript
// resultImages 应该是这种格式
resultImages: [
  { url: 'https://xxx.com/img1.png', name: '图片1' },
  { url: 'https://xxx.com/img2.png', name: '图片2' }
]
```

### 键盘快捷键

- `Delete` - 删除选中的图片
- `Escape` - 关闭对话框/取消选择

### 图片Store方法

```javascript
const imageStore = useImageStore()

// 设置待传递图片
imageStore.setPendingImages([img], 'white_bg')

// 消费待传递图片
const pendingImages = imageStore.consumePendingImages()
```

## 📂 文件结构

```
guanghe-studio/src/
├── components/
│   └── CanvasEditor.vue          # 画布编辑器组件
├── composables/
│   └── useCanvasEditor.js        # 画布编辑逻辑
├── store/modules/
│   └── image.js                  # 图片状态管理
└── api/
    └── canvasEditor.js           # API接口定义
```

## ⚠️ 注意事项

1. **保持现有功能** - 集成时不要破坏原有的生成、配置、AI助手功能
2. **resultImages格式** - 确保是数组格式 `[{url, name}]`
3. **API开发** - 后端需要开发对应的API接口才能实现编辑功能
4. **渐进式实现** - 可以先集成UI，功能逻辑后续再完善
5. **充分测试** - 集成后务必测试原有功能是否正常

## 🔗 相关文档

- [集成指南](../canvas-editor-integration.md)
- [集成示例](../white-bg-integration-example.md)
- [功能总结](../canvas-editor-summary.md)
- [检查清单](./canvas-editor-checklist.md)

---

**最后更新**: 2026-09-07
