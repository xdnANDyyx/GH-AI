# WhiteBg.vue 画布编辑器集成示例

## 修改步骤

### 1. 导入依赖

在 `<script setup>` 或 `setup()` 函数顶部添加：

```javascript
import CanvasEditor from '@/components/CanvasEditor.vue'
import { useImageStore } from '@/store/modules/image'
import {
  extendImage,
  generateMultiAngle,
  editImageText,
  partialRedraw,
  detectLayers
} from '@/api/canvasEditor'
```

### 2. 替换画布区域

**原始代码：**
```vue
<div class="canvas-box">
  <!-- 画布浮层：缩放 / 全屏 / 导出 / 右键菜单 -->
  <!--<CanvasOverlay :overlay="canvasUI" @export="handleCanvasExport" />-->

  <!-- 有结果图时显示在画布中 -->
  <div v-if="hasResult" class="canvas-result" :class="{ generating: isGenerating }">
    <el-image
      :src="resultImages[0].url || resultImages[0]"
      :preview-src-list="resultImages.map(r => r.url || r)"
      fit="contain"
      class="result-img"
    />
  </div>
  <!-- 空状态占位符 -->
  <div v-else-if="!isGenerating" class="canvas-placeholder">
    <!-- ... -->
  </div>

  <!-- 生图阶段状态绝对定位浮层 -->
  <div v-if="isGenerating" class="canvas-loading">
    <!-- ... -->
  </div>
</div>
```

**修改后代码：**
```vue
<div class="canvas-box">
  <!-- 画布编辑器 -->
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
```

### 3. 添加事件处理函数

在 `<script>` 部分添加：

```javascript
// 图片store
const imageStore = useImageStore()

// 编辑功能处理
const handleExtend = async (params) => {
  console.log('扩图:', params)
  try {
    // 调用API
    const res = await extendImage({
      imageUrl: params.image.url,
      ratio: params.ratio,
      width: params.width,
      height: params.height
    })
    // 处理返回结果
    ElMessage.success('扩图成功')
  } catch (error) {
    console.error('扩图失败:', error)
    ElMessage.error('扩图失败')
  }
}

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

// 右键菜单处理
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

const handleSendToRetouch = (img) => {
  // 设置待传递图片
  imageStore.setPendingImages([img], 'white_bg')
  // TODO: 跳转到产品精修模块
  ElMessage.success('已添加到产品精修')
}

const handleSendToWhiteBg = (img) => {
  ElMessage.success('已在白底图模块')
}

const handleDownload = (img) => {
  if (!img) return
  const url = img.url || img
  const link = document.createElement('a')
  link.href = url
  link.download = `white_bg_${Date.now()}.png`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
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
```

### 4. 保持原有功能

- 生成函数（handleGenerate）保持不变
- 配置面板保持不变
- AI助手保持不变
- 只替换了画布区域的显示方式

### 5. 完整对比

**改动前：**
- 简单的el-image显示
- 点击只能预览
- 右键无功能

**改动后：**
- 网格化图片展示
- 点击显示编辑工具栏
- 右键显示操作菜单
- 支持图片编辑功能
- 支持图层调整
- 支持跨模块跳转
- 支持下载

## 其他模块集成

其他8个模块（Background、Retouch、AiModel、HeroImage、DetailImg、Banner、SizeMark、BatchProcess）的集成方式完全相同，只需：

1. 将 `feature-name="white_bg"` 改为对应的feature name
2. 实现对应的事件处理函数
3. 根据需要调整事件处理逻辑

## 注意事项

1. **resultImages结构**：确保resultImages是数组格式
   ```javascript
   resultImages: [
     { url: 'xxx', name: 'xxx' },
     { url: 'yyy', name: 'yyy' }
   ]
   ```

2. **API开发**：后端需要开发对应的API接口

3. **测试**：集成后务必测试原有生成功能是否正常

4. **样式**：如需要调整网格显示样式，可修改CanvasEditor.vue中的CSS
