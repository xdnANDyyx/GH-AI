# CanvasEditor 集成模板

> 使用此模板快速集成CanvasEditor到工作台模块

## 📋 需要修改的文件

```
YOUR_MODULE.vue  (例如: Background.vue, Retouch.vue 等)
```

## 🔧 完整代码

### 1. Script 部分

在 `<script setup>` 顶部添加导入：

```vue
<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CanvasEditor from '@/components/CanvasEditor.vue'  // 👈 新增
import { useImageStore } from '@/store/modules/image'      // 👈 新增
import {
  extendImage,           // 👈 新增
  generateMultiAngle,    // 👈 新增
  editImageText,         // 👈 新增
  partialRedraw,         // 👈 新增
  detectLayers           // 👈 新增
} from '@/api/canvasEditor'  // 👈 新增

// ========== 原有的代码保持不变 ==========
// ... 你的所有现有代码 ...

// ========== 新增：图片Store ==========
const imageStore = useImageStore()

// ========== 新增：事件处理函数 ==========

// 扩图
const handleExtend = async (params) => {
  console.log('扩图:', params)
  try {
    const res = await extendImage({
      imageUrl: params.image.url,
      ratio: params.ratio,
      width: params.width,
      height: params.height
    })
    ElMessage.success('扩图成功')
  } catch (error) {
    console.error('扩图失败:', error)
    ElMessage.error('扩图失败')
  }
}

// 多角度生成
const handleMultiAngle = async (params) => {
  console.log('多角度:', params)
  try {
    const res = await generateMultiAngle({
      imageUrl: params.image.url,
      count: params.count,
      type: params.type
    })
    // TODO: 将生成的图片添加到resultImages
    ElMessage.success(`已生成${params.count}个角度`)
  } catch (error) {
    console.error('多角度生成失败:', error)
    ElMessage.error('多角度生成失败')
  }
}

// 改文字
const handleEditText = async (params) => {
  console.log('改文字:', params)
  try {
    const res = await editImageText({
      imageUrl: params.image.url,
      originalText: params.originalText,
      newText: params.newText,
      font: params.font
    })
    // TODO: 替换原图片
    ElMessage.success('文字修改成功')
  } catch (error) {
    console.error('文字修改失败:', error)
    ElMessage.error('文字修改失败')
  }
}

// 局部重绘
const handlePartialRedraw = async (params) => {
  console.log('局部重绘:', params)
  try {
    const res = await partialRedraw({
      imageUrl: params.image.url,
      description: params.description,
      mask: params.mask
    })
    // TODO: 替换原图片
    ElMessage.success('局部重绘成功')
  } catch (error) {
    console.error('局部重绘失败:', error)
    ElMessage.error('局部重绘失败')
  }
}

// 图层炸开
const handleExplodeLayers = async (params) => {
  console.log('图层炸开:', params)
  try {
    const res = await detectLayers({
      imageUrl: params.image.url
    })
    // TODO: 处理图层分离结果
    ElMessage.success('图层炸开成功')
  } catch (error) {
    console.error('图层炸开失败:', error)
    ElMessage.error('图层炸开失败')
  }
}

// 删除图片
const handleDeleteImage = async (index) => {
  try {
    await ElMessageBox.confirm('确定删除这张图片吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // 删除图片
    resultImages.value.splice(index, 1)
    ElMessage.success('图片已删除')
  } catch {
    // 用户取消
  }
}

// 发送到产品精修
const handleSendToRetouch = (img) => {
  // 设置待传递图片
  imageStore.setPendingImages([img], 'white_bg')  // 👈 改为你的模块名称
  // TODO: 跳转到产品精修模块
  ElMessage.success('已添加到产品精修')
}

// 发送到白底图
const handleSendToWhiteBg = (img) => {
  ElMessage.success('已在白底图模块')
}

// 下载图片
const handleDownload = (img) => {
  if (!img) return
  const url = img.url || img
  const link = document.createElement('a')
  link.href = url
  link.download = `image_${Date.now()}.png`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  ElMessage.success('开始下载')
}

// 向上移一层
const handleMoveUp = (index) => {
  if (index > 0) {
    const temp = resultImages.value[index]
    resultImages.value[index] = resultImages.value[index - 1]
    resultImages.value[index - 1] = temp
  }
}

// 向下移一层
const handleMoveDown = (index) => {
  if (index < resultImages.value.length - 1) {
    const temp = resultImages.value[index]
    resultImages.value[index] = resultImages.value[index + 1]
    resultImages.value[index + 1] = temp
  }
}

// 置顶
const handleBringToFront = (index) => {
  if (index < resultImages.value.length - 1) {
    const img = resultImages.value.splice(index, 1)[0]
    resultImages.value.push(img)
  }
}

// 置底
const handleSendToBack = (index) => {
  if (index > 0) {
    const img = resultImages.value.splice(index, 1)[0]
    resultImages.value.unshift(img)
  }
}

// ========== 保持原有代码不变 ==========
// ... 你的其他现有代码 ...

</script>
```

### 2. Template 部分

找到原来的画布显示代码，替换为：

```vue
<template>
  <!-- ========== 原有的模板结构保持不变 ========== -->
  <!-- ... 你的其他模板代码 ... -->

  <!-- ========== 修改：画布区域 ========== -->
  <div class="canvas-box">
    <CanvasEditor
      :images="resultImages"
      feature-name="white_bg"  <!-- 👈 改为你的模块名称 -->
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

  <!-- ========== 保持原有代码不变 ========== -->
  <!-- ... 你的其他模板代码 ... -->
</template>
```

### 3. 确保 resultImages 格式正确

你的 `resultImages` 应该是数组格式：

```javascript
// ✅ 正确格式
const resultImages = ref([
  { url: 'https://xxx.com/img1.png', name: '图片1' },
  { url: 'https://xxx.com/img2.png', name: '图片2' }
])

// ❌ 错误格式（如果是非数组）
const resultImages = ref('https://xxx.com/img.png')
```

如果不是数组格式，需要修改为数组格式。

## 📝 修改清单

在集成完成后，请检查：

- [ ] Script顶部已导入所有依赖
- [ ] 所有事件处理函数已实现
- [ ] Template中的画布区域已替换
- [ ] feature-name已改为你的模块名称
- [ ] resultImages是数组格式
- [ ] 原有的生成功能测试通过
- [ ] 原有的配置面板测试通过
- [ ] 右键菜单功能正常
- [ ] 点击工具栏功能正常

## 🎯 模块名称映射

| 模块 | Feature Name |
|------|-------------|
| 白底图 | `white_bg` |
| 背景图 | `change_bg` |
| 产品精修 | `retouch` |
| AI模特 | `ai_model` |
| 主图设计 | `main_image` |
| 详情图A+ | `detail_img` |
| Banner设计 | `banner` |
| 尺寸标记 | `dimension` |
| 批量处理 | `batch` |

## ⚠️ 注意事项

1. **保持现有功能** - 不要删除或修改原有的生成、配置、AI助手功能
2. **代码风格** - 保持原有代码风格（Options API 或 Composition API）
3. **渐进式实现** - 可以先集成UI，功能逻辑后续再完善
4. **充分测试** - 集成后务必测试原有功能是否正常

## 🔗 相关文档

- [快速参考](../canvas-editor-quick-reference.md)
- [集成指南](../canvas-editor-integration.md)
- [WhiteBg.vue示例](../white-bg-integration-example.md)
- [检查清单](../canvas-editor-checklist.md)

---

**最后更新**: 2026-09-07
