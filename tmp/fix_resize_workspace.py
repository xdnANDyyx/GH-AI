import re, os

FILES = {
    "AiModel.vue": "/home/beiming/guanghe-studio/src/views/workspace/AiModel.vue",
    "HeroImage.vue": "/home/beiming/guanghe-studio/src/views/workspace/HeroImage.vue",
    "DetailImg.vue": "/home/beiming/guanghe-studio/src/views/workspace/DetailImg.vue",
    "SizeMark.vue": "/home/beiming/guanghe-studio/src/views/workspace/SizeMark.vue",
    "Banner.vue": "/home/beiming/guanghe-studio/src/views/workspace/Banner.vue",
    "BatchProcess.vue": "/home/beiming/guanghe-studio/src/views/workspace/BatchProcess.vue",
}

def replace_in_file(path, old, new):
    with open(path, 'r') as f:
        content = f.read()
    if old not in content:
        print(f"WARNING: old text not found in {path}")
        return False
    content = content.replace(old, new)
    with open(path, 'w') as f:
        f.write(content)
    print(f"OK: {path}")
    return True

# ============================================================
# 1. AiModel.vue  - uses % based configFlex / aiFlex
# ============================================================
old = """const canvasFlex = ref('0 0 50%')
const configFlex = ref('0 0 25%')
const aiFlex = ref('0 0 25%')"""
new = """const canvasFlex = ref('0 0 50%')
const _configWidthPx = ref(280)
const _aiWidthPx = ref(360)
const configFlex = computed(() => `0 0 ${_configWidthPx.value}px`)
const aiFlex = computed(() => `0 0 ${_aiWidthPx.value}px`)"""

replace_in_file(FILES["AiModel.vue"], old, new)

# Fix resize logic: config resize should NOT change ai, ai resize should NOT change config
old = """    if (resizeTarget === 'config') {
      const clampedCanvas = Math.max(25, Math.min(70, pct))
      const clampedConfig = Math.max(15, Math.min(40, 100 - clampedCanvas - 10))
      canvasFlex.value = `0 0 ${clampedCanvas}%`
      configFlex.value = `0 0 ${clampedConfig}%`
    } else if (resizeTarget === 'ai') {
      const clampedAi = Math.max(10, Math.min(50, 100 - pct))
      const clampedConfig = Math.max(15, Math.min(40, 100 - canvasPct - clampedAi))
      configFlex.value = `0 0 ${clampedConfig}%`
      aiFlex.value = `0 0 ${(100 - canvasPct - clampedConfig).toFixed(1)}%`
    }"""
new = """    if (resizeTarget === 'config') {
      const clampedCanvas = Math.max(25, Math.min(70, pct))
      canvasFlex.value = `0 0 ${clampedCanvas}%`
    } else if (resizeTarget === 'ai') {
      const rightCol = document.querySelector('.right-col')
      if (!rightCol) return
      const rightRect = rightCol.getBoundingClientRect()
      const rightX = e.clientX - rightRect.left
      const aiWidth = rightRect.width - rightX - 6
      _aiWidthPx.value = Math.max(200, Math.min(rightRect.width * 0.8, aiWidth))
    }"""

replace_in_file(FILES["AiModel.vue"], old, new)

# Fix onMounted for init
old = """onMounted(() => {
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
})"""
new = """onMounted(() => {
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
  nextTick(() => {
    const rightCol = document.querySelector('.right-col')
    if (rightCol) {
      const w = rightCol.getBoundingClientRect().width
      _configWidthPx.value = Math.round(w * 0.35)
      _aiWidthPx.value = Math.round(w * 0.55)
    }
  })
})"""

replace_in_file(FILES["AiModel.vue"], old, new)

# The AiModel.vue uses ai-resize-handle, handle that via _aiWidthPx
old = """function startAiResize(e) {
  isAiResizing = true
  aiStartX = e.clientX
  const aiEl = aiPanel.value
  aiStartWidth = aiEl ? aiEl.getBoundingClientRect().width : 320"""
new = """function startAiResize(e) {
  isAiResizing = true
  aiStartX = e.clientX
  const aiEl = aiPanel.value
  aiStartWidth = aiEl ? aiEl.getBoundingClientRect().width : 360"""

replace_in_file(FILES["AiModel.vue"], old, new)

old = """    newWidth = Math.max(240, Math.min(600, newWidth))
      aiFlex.value = `0 0 ${newWidth}px`"""
new = """    newWidth = Math.max(240, Math.min(600, newWidth))
      _aiWidthPx.value = newWidth"""

replace_in_file(FILES["AiModel.vue"], old, new)

# ============================================================
# 2. HeroImage.vue - % based, similar to AiModel
# ============================================================
old2 = """const canvasFlex = ref('0 0 50%')
const configFlex = ref('0 0 25%')
const aiFlex = ref('1 1 0%')"""
new2 = """const canvasFlex = ref('0 0 50%')
const _configWidthPx = ref(280)
const _aiWidthPx = ref(360)
const configFlex = computed(() => {
  if (configCollapsed.value) return '0 0 40px'
  return `0 0 ${_configWidthPx.value}px`
})
const aiFlex = computed(() => {
  if (configCollapsed.value) return '1 1 0%'
  return `0 0 ${_aiWidthPx.value}px`
})"""

replace_in_file(FILES["HeroImage.vue"], old2, new2)

old2 = """    if (resizeTarget === 'config') {
      const clampedCanvas = Math.max(25, Math.min(70, pct))
      const clampedConfig = Math.max(15, Math.min(40, 100 - clampedCanvas - 10))
      canvasFlex.value = `0 0 ${clampedCanvas}%`
      configFlex.value = `0 0 ${clampedConfig}%`
    } else if (resizeTarget === 'ai') {
      const canvasPct = parseFloat(canvasFlex.value.replace('0 0 ', ''))
      const clampedAi = Math.max(10, Math.min(50, 100 - pct))
      const clampedConfig = Math.max(15, Math.min(40, 100 - canvasPct - clampedAi))
      configFlex.value = `0 0 ${(clamped * (1 - ratio)).toFixed(1)}%`
      aiFlex.value = `1 1 ${100 - clamped}%`
    }"""

new2 = """    if (resizeTarget === 'config') {
      const clampedCanvas = Math.max(25, Math.min(70, pct))
      canvasFlex.value = `0 0 ${clampedCanvas}%`
    } else if (resizeTarget === 'ai') {
      const rightCol = document.querySelector('.right-col')
      if (!rightCol) return
      const rightRect = rightCol.getBoundingClientRect()
      const rightX = e.clientX - rightRect.left
      const aiWidth = rightRect.width - rightX - 6
      _aiWidthPx.value = Math.max(200, Math.min(rightRect.width * 0.8, aiWidth))
    }"""

replace_in_file(FILES["HeroImage.vue"], old2, new2)

# Fix startAiResize default
old2 = """function startAiResize(e) {
      isAiResizing = true
      aiStartX = e.clientX
      const aiEl = aiPanel.value
      aiStartWidth = aiEl ? aiEl.getBoundingClientRect().width : 320"""
new2 = """function startAiResize(e) {
      isAiResizing = true
      aiStartX = e.clientX
      const aiEl = aiPanel.value
      aiStartWidth = aiEl ? aiEl.getBoundingClientRect().width : 360"""

replace_in_file(FILES["HeroImage.vue"], old2, new2)

old2 = """    newWidth = Math.max(240, Math.min(600, newWidth))
      aiFlex.value = `0 0 ${newWidth}px`"""
new2 = """    newWidth = Math.max(240, Math.min(600, newWidth))
      _aiWidthPx.value = newWidth"""

replace_in_file(FILES["HeroImage.vue"], old2, new2)

# Fix onMounted
old2 = """onMounted(() => {
  gen.loadPromptInfo()
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
})"""
new2 = """onMounted(() => {
  gen.loadPromptInfo()
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
  nextTick(() => {
    const rightCol = document.querySelector('.right-col')
    if (rightCol) {
      const w = rightCol.getBoundingClientRect().width
      _configWidthPx.value = Math.round(w * 0.35)
      _aiWidthPx.value = Math.round(w * 0.55)
    }
  })
})"""

replace_in_file(FILES["HeroImage.vue"], old2, new2)

# ============================================================
# 3. DetailImg.vue - rightFlex/configFlex/aiFlex pattern
# ============================================================
old3 = """const canvasFlex = ref('0 0 50%')
const rightFlex = ref('1 1 0%')
const configFlex = ref('0 0 50%')
const aiFlex = ref('1 1 0%')"""
new3 = """const canvasFlex = ref('0 0 50%')
const rightFlex = ref('1 1 0%')
const _configWidthPx = ref(280)
const _aiWidthPx = ref(360)
const configFlex = computed(() => {
  if (configCollapsed.value) return '0 0 40px'
  return `0 0 ${_configWidthPx.value}px`
})
const aiFlex = computed(() => {
  if (configCollapsed.value) return '1 1 0%'
  return `0 0 ${_aiWidthPx.value}px`
})"""

# DetailImg has let isResizing...etc with comma separated
# Find the pattern: const aiPanel = ref(null)\n.*let isResizing
# Instead, just search for the specific old block
content = open(FILES["DetailImg.vue"]).read()
# The old content for DetailImg uses:
# const configFlex = ref('0 0 50%')
# const aiFlex = ref('1 1 0%')
# const aiPanel = ref(null)
# let isResizing = false
if "const configFlex = ref('0 0 50%')" in content and "const aiFlex = ref('1 1 0%')" in content:
    content = content.replace("const configFlex = ref('0 0 50%')\nconst aiFlex = ref('1 1 0%')", f"""const _configWidthPx = ref(280)
const _aiWidthPx = ref(360)
const configFlex = computed(() => {{
  if (configCollapsed.value) return '0 0 40px'
  return `0 0 ${{_configWidthPx.value}}px`
}})
const aiFlex = computed(() => {{
  if (configCollapsed.value) return '1 1 0%'
  return `0 0 ${{_aiWidthPx.value}}px`
}})""")
    open(FILES["DetailImg.vue"], 'w').write(content)
    print("OK: DetailImg.vue (configFlex/aiFlex)")
else:
    print("WARNING: DetailImg.vue pattern not found")

# Fix resize logic in DetailImg.vue
old3 = """    if (resizeTarget === 'config') {
      const clampedConfig = Math.max(15, Math.min(70, rightPct))
      const clampedAi = Math.max(25, Math.min(80, 100 - clampedConfig))
      configFlex.value = `0 0 ${clampedConfig}%`
      aiFlex.value = `1 1 ${clampedAi}%`
    } else {
      const clampedMin = 25
      const clampedMax = 80
      const clamped = Math.max(clampedMin, Math.min(clampedMax, rightPct))
      configFlex.value = `0 0 ${clamped}%`
      aiFlex.value = `1 1 ${100 - clamped}%`
    }"""
new3 = """    if (resizeTarget === 'config') {
      const rightX = e.clientX - rightRect.left
      _configWidthPx.value = Math.max(150, Math.min(rightRect.width * 0.7, rightX))
    } else if (resizeTarget === 'ai') {
      const rightX = e.clientX - rightRect.left
      const aiWidth = rightRect.width - rightX - 6
      _aiWidthPx.value = Math.max(200, Math.min(rightRect.width * 0.8, aiWidth))
    }"""

replace_in_file(FILES["DetailImg.vue"], old3, new3)

# Fix onMounted for DetailImg
old3 = """onMounted(() => {
  gen.loadPromptInfo()
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
})"""
replace_in_file(FILES["DetailImg.vue"], old3, new2)

print("Done with DetailImg.vue")

# ============================================================
# 4. SizeMark.vue - % based with configFlex/aiFlex
# ============================================================
old4 = """const canvasFlex = ref('0 0 50%')
const configFlex = ref('0 0 25%')
const aiFlex = ref('1 1 0%')"""
new4 = """const canvasFlex = ref('0 0 50%')
const _configWidthPx = ref(280)
const _aiWidthPx = ref(360)
const configFlex = computed(() => `0 0 ${_configWidthPx.value}px`)
const aiFlex = computed(() => `0 0 ${_aiWidthPx.value}px`)"""

replace_in_file(FILES["SizeMark.vue"], old4, new4)

# Fix resize logic
old4 = """    if (resizeTarget === 'config') {
      const clampedCanvas = Math.max(25, Math.min(70, pct))
      const clampedConfig = Math.max(15, Math.min(40, 100 - clampedCanvas - 10))
      canvasFlex.value = `0 0 ${clampedCanvas}%`
      configFlex.value = `0 0 ${clampedConfig}%`
    } else if (resizeTarget === 'ai') {
      const clampedAi = Math.max(15, Math.min(50, 100 - pct))
      const clamped = Math.max(15, Math.min(50, clampedAi))
      const ratio = canvasWidth / sum
      canvasFlex.value = `0 0 ${(clamped * ratio).toFixed(1)}%`
      configFlex.value = `0 0 ${(clamped * (1 - ratio)).toFixed(1)}%`
      aiFlex.value = `1 1 ${100 - clamped}%`
    }"""

new4 = """    if (resizeTarget === 'config') {
      const clampedCanvas = Math.max(25, Math.min(70, pct))
      canvasFlex.value = `0 0 ${clampedCanvas}%`
    } else if (resizeTarget === 'ai') {
      const rightCol = document.querySelector('.right-col')
      if (!rightCol) return
      const rightRect = rightCol.getBoundingClientRect()
      const rightX = e.clientX - rightRect.left
      const aiWidth = rightRect.width - rightX - 6
      _aiWidthPx.value = Math.max(200, Math.min(rightRect.width * 0.8, aiWidth))
    }"""

replace_in_file(FILES["SizeMark.vue"], old4, new4)

# Fix startAiResize default
old4 = """function startAiResize(e) {
      isAiResizing = true
      aiStartX = e.clientX
      const aiEl = aiPanel.value
      aiStartWidth = aiEl ? aiEl.getBoundingClientRect().width : 320"""
new4 = """function startAiResize(e) {
      isAiResizing = true
      aiStartX = e.clientX
      const aiEl = aiPanel.value
      aiStartWidth = aiEl ? aiEl.getBoundingClientRect().width : 360"""

replace_in_file(FILES["SizeMark.vue"], old4, new4)

old4 = """    newWidth = Math.max(240, Math.min(600, newWidth))
      aiFlex.value = `0 0 ${newWidth}px`"""
new4 = """    newWidth = Math.max(240, Math.min(600, newWidth))
      _aiWidthPx.value = newWidth"""

replace_in_file(FILES["SizeMark.vue"], old4, new4)

# Fix onMounted
old4 = """onMounted(() => {
  gen.loadPromptInfo()
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
})"""
replace_in_file(FILES["SizeMark.vue"], old4, new2)

# ============================================================
# 5. Banner.vue - px based, resizeTarget: config|ai
# ============================================================
old5 = """const canvasFlex = ref('1 1 0%'); const configFlex = ref('0 0 340px'); const aiFlex = ref('0 0 260px')"""
new5 = """const canvasFlex = ref('1 1 0%')
const _configWidthPx = ref(340)
const _aiWidthPx = ref(260)
const configFlex = computed(() => `0 0 ${_configWidthPx.value}px`)
const aiFlex = computed(() => `0 0 ${_aiWidthPx.value}px`)"""

replace_in_file(FILES["Banner.vue"], old5, new5)

# Fix resize logic - config resize should NOT touch ai, ai resize should NOT touch config
old5 = """    if (resizeTarget === 'config') {
      const canvasPx = Math.max(totalW * 0.25, x - 3)
      const configPx = Math.min(Math.max(280, totalW - canvasPx - 280), 480)
      canvasFlex.value = `1 1 ${canvasPx}px`
      configFlex.value = `0 0 ${configPx}px`
    } else if (resizeTarget === 'ai') {
      const canvasPx = leftTotal * 0.7
      const configPx = Math.min(Math.max(280, leftTotal * 0.3), 480)
      canvasFlex.value = `1 1 ${canvasPx}px`
      configFlex.value = `0 0 ${configPx}px`
      aiFlex.value = `0 0 ${aiPx}px`
    }"""
new5 = """    if (resizeTarget === 'config') {
      const canvasPx = Math.max(totalW * 0.25, x - 3)
      const configPx = Math.min(Math.max(280, totalW - canvasPx - 260), 480)
      canvasFlex.value = `1 1 ${canvasPx}px`
      _configWidthPx.value = configPx
    } else if (resizeTarget === 'ai') {
      const rightCol = document.querySelector('.right-col')
      if (!rightCol) return
      const rightRect = rightCol.getBoundingClientRect()
      const rightX = e.clientX - rightRect.left
      const aiWidth = rightRect.width - rightX - 6
      _aiWidthPx.value = Math.max(200, Math.min(500, aiWidth))
    }"""

replace_in_file(FILES["Banner.vue"], old5, new5)

# Fix startAiResize default
old5 = """    function startAiResize(e) { isAiResizing = true; aiStartX = e.clientX; const aiEl = aiPanel.value; aiStartWidth = aiEl ? aiEl.getBoundingClientRect().width : 260;"""
new5 = """    function startAiResize(e) { isAiResizing = true; aiStartX = e.clientX; const aiEl = aiPanel.value; aiStartWidth = aiEl ? aiEl.getBoundingClientRect().width : 260;"""

# Fix aiFlex -> _aiWidthPx in AiResize
old5 = """aiFlex.value = `0 0 ${newWidth}px`"""
new5 = """_aiWidthPx.value = newWidth"""

replace_in_file(FILES["Banner.vue"], old5, new5)

# ============================================================
# 6. BatchProcess.vue
# ============================================================
old6 = """const canvasFlex = ref('1 1 0%'); const configFlex = ref('0 0 340px'); const aiFlex = ref('0 0 260px')"""
replace_in_file(FILES["BatchProcess.vue"], old6, new5)

old6b = """aiFlex.value = `0 0 ${newWidth}px`"""
replace_in_file(FILES["BatchProcess.vue"], old6b, new5)

# Fix resize logic for BatchProcess (same as Banner)
replace_in_file(FILES["BatchProcess.vue"], old5, new5)

print("All done!")
