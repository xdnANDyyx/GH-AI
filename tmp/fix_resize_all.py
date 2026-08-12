#!/usr/bin/env python3
import os

workspace_dir = 'guanghe-studio/src/views/workspace'

def fix_file(path, old, new, name):
    if not os.path.exists(path):
        print(f"{name}: FILE NOT FOUND")
        return False
    with open(path, 'r') as f:
        content = f.read()
    if old in content:
        content = content.replace(old, new)
        with open(path, 'w') as f:
            f.write(content)
        print(f"{name}: FIXED")
        return True
    else:
        lines = content.split('\n')
        for i, line in enumerate(lines):
            if "resizeTarget === 'ai'" in line:
                ctx_start = max(0, i-2)
                ctx_end = min(len(lines), i+12)
                snippet = '\n'.join(f"  {j+1}: {lines[j]}" for j in range(ctx_start, ctx_end))
                print(f"{name}: NOT MATCHED at line {i+1}:\n{snippet}")
                return False
        # check for resizeTarget === 'inner'
        for i, line in enumerate(lines):
            if "resizeTarget === 'inner'" in line or "resizeTarget === 'right'" in line:
                ctx_start = max(0, i-2)
                ctx_end = min(len(lines), i+12)
                snippet = '\n'.join(f"  {j+1}: {lines[j]}" for j in range(ctx_start, ctx_end))
                print(f"{name}: found right/inner at line {i+1}:\n{snippet}")
                return False
        print(f"{name}: no resizeTarget found")
        return False

# ===================================================================
# Type B: Tri-column layout with percentage-based flex (HeroImage, SizeMark)
# aiFlex is 1 1, not 0 0
# ===================================================================
hero_old = (
    "      } else if (resizeTarget === 'ai') {\n"
    "        const clampedMin = 50\n"
    "        const clampedMax = 85\n"
    "        const clamped = Math.max(clampedMin, Math.min(clampedMax, pct))\n"
    "        const canvasWidth = parseFloat(canvasFlex.value.replace('0 0 ', ''))\n"
    "        const configWidth = parseFloat(configFlex.value.replace('0 0 ', ''))\n"
    "        const sum = canvasWidth + configWidth\n"
    "        const ratio = sum > 0 ? canvasWidth / sum : 0.66\n"
    "        canvasFlex.value = `0 0 ${(clamped * ratio).toFixed(1)}%`\n"
    "        configFlex.value = `0 0 ${(clamped * (1 - ratio)).toFixed(1)}%`\n"
    "        aiFlex.value = `1 1 ${100 - clamped}%`\n"
    "      }"
)
hero_new = (
    "      } else if (resizeTarget === 'ai') {\n"
    "        // 拖拽创作配置和AI对话之间的分隔线：画布栏固定不变\n"
    "        const canvasPct = parseFloat(canvasFlex.value.replace('0 0 ', ''))\n"
    "        const clampedAi = Math.max(10, Math.min(50, 100 - pct))\n"
    "        const clampedConfig = Math.max(15, Math.min(40, 100 - canvasPct - clampedAi))\n"
    "        configFlex.value = `0 0 ${clampedConfig}%`\n"
    "        aiFlex.value = `1 1 ${(100 - canvasPct - clampedConfig).toFixed(1)}%`\n"
    "      }"
)

fix_file(os.path.join(workspace_dir, 'HeroImage.vue'), hero_old, hero_new, 'HeroImage.vue')
fix_file(os.path.join(workspace_dir, 'SizeMark.vue'), hero_old, hero_new, 'SizeMark.vue')

# ===================================================================
# Type B: Tri-column with pixel-based flex (Banner, BatchProcess)
# ===================================================================
banner_old = (
    "      } else if (resizeTarget === 'ai') {\n"
    "        const clampedMain = Math.max(canvasMin, Math.min(canvasMax, x))\n"
    "        canvasFlex.value = clampedMain + 'px'\n"
    "        const remaining = total - clampedMain - configWidth - 12\n"
    "        if (remaining >= 200) {\n"
    "          aiFlex.value = `1 1 ${remaining}px`\n"
    "        }\n"
    "      }"
)
banner_new = (
    "      } else if (resizeTarget === 'ai') {\n"
    "        // 拖拽创作配置和AI对话之间的分隔线：画布栏固定不变，只调整配置和AI\n"
    "        const remaining = total - canvasFlex.value.replace('px', '') - 12\n"
    "        const clampedAi = Math.max(200, Math.min(remaining - 200, remaining - x + 200))\n"
    "        const newConfigWidth = Math.max(200, Math.min(remaining - 200, remaining - clampedAi))\n"
    "        configFlex.value = `0 0 ${newConfigWidth}px`\n"
    "        aiFlex.value = `1 1 ${remaining - newConfigWidth}px`\n"
    "      }"
)

fix_file(os.path.join(workspace_dir, 'Banner.vue'), banner_old, banner_new, 'Banner.vue')
fix_file(os.path.join(workspace_dir, 'BatchProcess.vue'), banner_old, banner_new, 'BatchProcess.vue')

# ===================================================================
# Type A: Two-column (canvas + rightCol) (WhiteBg, Background, DetailImg)
# rightCol contains config + ai panels
# ===================================================================
whitebg_old = (
    "      if (resizeTarget === 'right') {\n"
    "        // 画布固定，只改变右侧宽度\n"
    "        const newRightWidth = Math.max(200, Math.min(600, total - x))\n"
    "        rightFlex.value = `0 0 ${newRightWidth}px`\n"
    "      }"
)
whitebg_new = (
    "      if (resizeTarget === 'right') {\n"
    "        // 画布固定，只改变右侧宽度\n"
    "        const newRightWidth = Math.max(200, Math.min(600, total - x))\n"
    "        rightFlex.value = `0 0 ${newRightWidth}px`\n"
    "      } else if (resizeTarget === 'inner') {\n"
    "        // 拖拽创作配置和AI对话之间的分隔线：画布固定不变，只调整配置和AI\n"
    "        const rightWidth = parseFloat(rightFlex.value.replace('0 0 ', '').replace('px', ''))\n"
    "        const innerX = x - (total - rightWidth)\n"
    "        const panelWidth = Math.max(120, Math.min(rightWidth - 120, innerX))\n"
    "        configFlex.value = `0 0 ${panelWidth}px`\n"
    "        aiFlex.value = `1 1 ${rightWidth - panelWidth}px`\n"
    "      }"
)

fix_file(os.path.join(workspace_dir, 'WhiteBg.vue'), whitebg_old, whitebg_new, 'WhiteBg.vue')
fix_file(os.path.join(workspace_dir, 'Background.vue'), whitebg_old, whitebg_new, 'Background.vue')
fix_file(os.path.join(workspace_dir, 'DetailImg.vue'), whitebg_old, whitebg_new, 'DetailImg.vue')

print("\nDone!")