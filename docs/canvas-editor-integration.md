# 画布编辑器集成指南

## 概述

画布编辑器为前台工作台的所有图片生成模块提供统一的编辑功能，包括：
- **点击图片编辑**：扩图、多角度、改文字、局部重绘、图层炸开
- **右键菜单操作**：删除、跨模块跳转、下载、图层调整

## 已完成文件

### 1. CanvasEditor.vue
**路径**: `guanghe-studio/src/components/CanvasEditor.vue`

通用画布编辑器组件，提供完整的UI和交互。

### 2. useCanvasEditor.js
**路径**: `guanghe-studio/src/composables/useCanvasEditor.js`

画布编辑功能的Composition API混入，包含图片管理和编辑逻辑。

### 3. Image Store
**路径**: `guanghe-studio/src/store/modules/image.js`

全局图片状态管理，支持跨模块图片传递。

### 4. Canvas Editor API
**路径**: `guanghe-studio/src/api/canvasEditor.js`

画布编辑相关的API接口定义。

## 集成步骤

### 步骤1: 注册Image Store

在 `guanghe-studio/src/store/index.js` 中添加：

```javascript
import { createPinia } from 'pinia'
import { useImageStore } from './modules/image'

export const pinia = createPinia()

// 在应用启动时初始化
export const setupStores = () => {
  useImageStore()
}
```

### 步骤2: 集成到工作台模块

以 **WhiteBg.vue** 为例：

```vue
<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import CanvasEditor from '@/components/CanvasEditor.vue' // 导入组件
import { useImageStore } from '@/store/modules/image' // 导入图片store
import { extendImage, generateMultiAngle } from '@/api/canvasEditor' // 导入API

// 原有代码...

// 新增：图片store
const imageStore = useImageStore()

// 新增：图片管理（替代原有的resultImages）
const images = computed({
  get: () => resultImages.value, // 使用原有的resultImages
  set: (val) => { resultImages.value = val }
})

// 新增：编辑功能处理函数
const handleExtend = async (params) => {
  console.log('扩图:', params)
  // TODO: 调用扩图API
  // await extendImage(params)
  // imageStore.addPendingImage(newImage)
}

const handleMultiAngle = async (params) => {
  console.log('多角度:', params)
  // TODO: 调用多角度API
}

const handleEditText = async (params) => {
  console.log('改文字:', params)
}

const handlePartialRedraw = async (params) => {
  console.log('局部重绘:', params)
}

const handleExplodeLayers = async (params) => {
  console.log('图层炸开:', params)
}

// 新增：右键菜单处理
const handleDeleteImage = (index) => {
  removeImage(index) // 使用原有的removeImage函数
}

const handleSendToRetouch = (img) => {
  // 设置待传递图片
  imageStore.setPendingImages([img], 'white_bg')
  // TODO: 路由跳转到产品精修
  // router.push('/workspace/retouch')
}

const handleSendToWhiteBg = (img) => {
  ElMessage.success('已在白底图模块')
}

const handleDownload = (img) => {
  downloadImage(img) // 使用原有的downloadImage函数
}

const handleMoveUp = (index) => {
  moveImageUp(index) // 使用useCanvasEditor中的方法
}

const handleMoveDown = (index) => {
  moveImageDown(index)
}

const handleBringToFront = (index) => {
  bringToFront(index)
}

const handleSendToBack = (index) => {
  sendToBack(index)
}
</script>

<template>
  <!-- 原有模板结构保持不变，只替换画布区域 -->
  <div class="canvas-box">
    <!-- 删除原有的结果图显示代码 -->

    <!-- 新增：画布编辑器 -->
    <CanvasEditor
      :images="images"
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

  <!-- 其余模板保持不变 -->
</template>
```

### 步骤3: Feature Name 映射表

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

### 步骤4: 实现跨模块跳转

在 `router/index.js` 或路由配置中添加跳转逻辑：

```javascript
// 在发送到其他模块时
const handleSendToRetouch = (img) => {
  // 1. 保存图片到全局store
  imageStore.setPendingImages([img], 'white_bg')
  
  // 2. 跳转到目标模块
  router.push({
    path: '/workspace/retouch',
    query: { from: 'white_bg' }
  })
}
```

在目标模块（如Retouch.vue）的onMounted中：

```javascript
onMounted(() => {
  // 检查是否有从其他模块传递过来的图片
  const pendingImages = imageStore.consumePendingImages()
  if (pendingImages.length > 0) {
    // 加载这些图片
    pendingImages.forEach(img => {
      // TODO: 处理传递过来的图片
      console.log('接收到跨模块图片:', img)
    })
  }
})
```

## 功能说明

### 点击图片工具栏

1. **扩图** - 扩展图片画布，支持预设比例或自定义尺寸
2. **多角度** - 基于原图生成多个不同角度的产品图
3. **改文字** - 修改图片中的文字内容
4. **局部重绘** - 选择区域并描述需要重绘的内容
5. **图层炸开** - 检测并分离图片中的不同图层

### 右键菜单

1. **删除** - 删除当前图片
2. **放入产品精修** - 将图片发送到产品精修模块
3. **放入白底图** - 将图片发送到白底图模块
4. **下载图片** - 下载当前图片到本地
5. **向上移一层** - 将图片层级上移
6. **向下移一层** - 将图片层级下移
7. **置顶** - 将图片移动到最上层
8. **置底** - 将图片移动到底层

## 后端API开发

需要后端提供以下接口（已在 `canvasEditor.js` 中定义）：

### 1. 图片扩图
```javascript
POST /ai/image/extend
Body: {
  imageUrl: string,
  ratio?: string,  // 如 "16:9"
  width?: number,
  height?: number
}
```

### 2. 多角度生成
```javascript
POST /ai/image/multi-angle
Body: {
  imageUrl: string,
  count: number,   // 2-8
  type: string     // 'rotate' | 'multi-view' | '360'
}
```

### 3. 改文字
```javascript
POST /ai/image/edit-text
Body: {
  imageUrl: string,
  originalText: string,
  newText: string,
  font: string
}
```

### 4. 局部重绘
```javascript
POST /ai/image/partial-redraw
Body: {
  imageUrl: string,
  description: string,
  mask: object  // 重绘区域mask
}
```

### 5. 图层检测
```javascript
POST /ai/image/detect-layers
Body: {
  imageUrl: string
}
```

## 注意事项

1. **保持现有功能不变**：集成时不要破坏现有的生成、配置、AI助手等功能
2. **代码风格一致**：每个模块保持原有的代码风格（Options API或Composition API）
3. **渐进式实现**：可以先集成UI，功能逻辑后续再完善
4. **测试充分**：集成后务必测试原有功能是否正常

## 下一步

1. ✅ 集成CanvasEditor到9个工作台模块
2. ⏳ 实现后端API接口
3. ⏳ 实现扩图功能
4. ⏳ 实现多角度生成功能
5. ⏳ 实现改文字功能
6. ⏳ 实现局部重绘功能
7. ⏳ 实现图层炸开功能
8. ⏳ 完善跨模块跳转功能
9. ⏳ 测试所有功能
