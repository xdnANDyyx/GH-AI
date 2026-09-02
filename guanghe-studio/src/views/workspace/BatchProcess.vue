<template>
  <div class="workspace-page">

    <!-- ========== Three Column Layout ========== -->
    <div class="three-col">
      <!-- ===== Canvas Column (left) ===== -->
   
      <div class="canvas-col" :style="{ flex: canvasFlex }">
        <!-- ========== Steps Bar ========== -->
        <!-- <div class="steps-bar">
          <div class="step-item active" @click="currentStep = 1">
            <div class="step-num">1</div>
            上传素材
          </div>
          <div class="step-line"></div>
          <div class="step-item" :class="{ active: currentStep >= 2 }" @click="currentStep = 2">
            <div class="step-num">2</div>
            设计类型与风格
          </div>
          <div class="step-line"></div>
          <div class="step-item" :class="{ active: currentStep >= 3 }" @click="currentStep = 3">
            <div class="step-num">3</div>
            生成设置
          </div>
          <div class="step-line"></div>
          <div class="step-item" :class="{ active: currentStep >= 4 }" @click="currentStep = 4">
            <div class="step-num">4</div>
            批量生成
          </div>
        </div> -->

        <!-- Canvas Toolbar -->
         <!--
        <div class="canvas-toolbar">
          <div class="toolbar-left">
            <button class="toolbar-btn" @click="fitToScreen">
              <svg viewBox="0 0 14 14" fill="none"><path d="M2 7a5 5 0 119.5 1.5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/><path d="M2 3v4h4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              适应屏幕
            </button>
          </div>
          <div class="toolbar-right">
            <div class="zoom-group">
              <button class="zoom-btn" @click="zoomOut">−</button>
              <span class="zoom-val">{{ zoomLevel }}%</span>
              <button class="zoom-btn" @click="zoomIn">+</button>
            </div>
            <button class="toolbar-btn" @click="toggleFullscreen">
              <svg viewBox="0 0 14 14" fill="none"><rect x="2" y="2" width="10" height="10" rx="1" stroke="currentColor" stroke-width="1.2"/><path d="M5 2v10M9 2v10M2 5h10M2 9h10" stroke="currentColor" stroke-width="0.8" opacity="0.4"/></svg>
              全屏
            </button>
          </div>
        </div>
          -->
        <!-- Canvas Area -->
        <div class="canvas-box">
          <!-- <CanvasOverlay :overlay="canvasUI" @export="handleCanvasExport" /> -->
          <!-- 有结果图时显示在画布中 -->
          <div v-if="resultImages.length > 0" class="canvas-result-grid" :class="{ generating: isGenerating }">
            <div v-for="(img, i) in resultImages" :key="i" class="canvas-result-item" @click="previewImage(img.url)">
              <img :src="img.url" class="canvas-result-img" />
            </div>
          </div>
          <!-- 空状态占位符 -->
          <div v-else-if="!isGenerating" class="canvas-placeholder">
            <svg viewBox="0 0 64 64" fill="none">
              <rect x="8" y="12" width="48" height="40" rx="4" stroke="#D1D5DB" stroke-width="2"/>
              <circle cx="24" cy="26" r="5" stroke="#D1D5DB" stroke-width="1.5"/>
              <path d="M8 44l16-14 10 10 10-14 12 12" stroke="#D1D5DB" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <h3>上传产品图并配置参数后生成</h3>
            <p>生成结果将同时显示在此画布和右侧 AI 助手中</p>
          </div>

          <!-- 生图阶段状态绝对定位浮层 -->
          <div v-if="isGenerating" class="canvas-loading">
            <el-icon class="is-loading" :size="24" color="#2563FF"><Loading /></el-icon>
            <p>{{ genStatus || '正在生成...' }}</p>
          </div>
        </div>

        <!-- Task List Card -->
        <div class="task-card">
          <div class="task-header">
            <span class="task-title">生成任务列表（{{ tasks.length }}）</span>
            <div class="task-actions">
              <button class="task-action-btn" @click="clearTasks">
                <svg viewBox="0 0 14 14" fill="none"><path d="M3 4h8l-.7 7.5a1 1 0 01-1 .9H4.7a1 1 0 01-1-.9L3 4z" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/><path d="M5.5 4V2.5h3V4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                清空记录
              </button>
              <span class="task-fold" @click="taskListExpanded = !taskListExpanded">{{ taskListExpanded ? '∧' : '∨' }}</span>
            </div>
          </div>

          <!-- Tabs -->
          <div class="task-tabs">
            <div
              v-for="tab in taskTabs"
              :key="tab.key"
              class="task-tab"
              :class="{ active: taskFilter === tab.key }"
              @click="taskFilter = tab.key"
            >{{ tab.label }}</div>
          </div>

          <!-- Table -->
          <div class="task-table-wrap" v-show="taskListExpanded">
            <table class="task-table">
              <thead>
                <tr>
                  <th style="width:20%">任务名称</th>
                  <th style="width:10%">设计类型</th>
                  <th style="width:14%">素材数量</th>
                  <th style="width:8%">生成数量</th>
                  <th style="width:8%">状态</th>
                  <th style="width:14%">进度</th>
                  <th style="width:14%">创建时间</th>
                  <th style="width:12%">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="task in filteredTasks" :key="task.id">
                  <td>
                    <div class="task-name">
                      <div class="task-thumbs">
                        <div class="task-thumb" v-for="(thumb, i) in task.thumbs" :key="i">
                          <img v-if="thumb.url" :src="thumb.url" class="thumb-img" />
                          <div v-else class="img-placeholder"></div>
                        </div>
                      </div>
                      <span class="task-name-text">{{ task.name }}</span>
                    </div>
                  </td>
                  <td><span class="cell-meta">{{ task.designType }}</span></td>
                  <td><span class="cell-meta">{{ task.materialCount }}</span></td>
                  <td><span class="cell-val">{{ task.genCount }} 套方案</span></td>
                  <td>
                    <span class="status-dot" :class="task.statusClass">{{ task.statusText }}</span>
                    <div v-if="task.errorMsg" class="task-error-msg">{{ task.errorMsg }}</div>
                  </td>
                  <td>
                    <div class="progress-cell" v-if="task.progress !== null">
                      <div class="progress-bar"><div class="progress-fill" :class="task.statusClass" :style="{ width: task.progress + '%' }"></div></div>
                      <span class="progress-text">{{ task.progress }}%</span>
                    </div>
                    <span class="cell-meta" v-else>--</span>
                  </td>
                  <td><span class="cell-meta">{{ task.createdAt }}</span></td>
                  <td>
                    <div class="table-actions">
                      <div class="table-action-icon" title="查看" v-if="task.status === 'done' || task.status === 'processing'" @click="viewTask(task)">
                        <svg viewBox="0 0 15 15" fill="none"><path d="M1.5 7.5s2.5-4.5 6-4.5 6 4.5 6 4.5-2.5 4.5-6 4.5-6-4.5-6-4.5z" stroke="currentColor" stroke-width="1.2"/><circle cx="7.5" cy="7.5" r="2" stroke="currentColor" stroke-width="1.2"/></svg>
                      </div>
                      <div class="table-action-icon" title="下载" v-if="task.status === 'done'" @click="downloadTask(task)">
                        <svg viewBox="0 0 15 15" fill="none"><path d="M7.5 2v8M4.5 7l3 3 3-3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/><path d="M2.5 11v1.5h10V11" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                      </div>
                      <div class="table-action-icon" title="删除" @click="deleteTask(task)">
                        <svg viewBox="0 0 15 15" fill="none"><path d="M3.5 4.5h8l-.6 7.5a1 1 0 01-1 .9H5.1a1 1 0 01-1-.9L3.5 4.5z" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/><path d="M5.5 4.5V3h4v1.5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                      </div>
                    </div>
                  </td>
                </tr>
                <tr v-if="filteredTasks.length === 0">
                  <td colspan="8" class="empty-row">暂无任务记录</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Tips Card -->
        <div class="tips-card">
          <svg class="tips-icon" viewBox="0 0 20 20" fill="none">
            <circle cx="10" cy="10" r="8.5" stroke="#2563FF" stroke-width="1.5"/>
            <path d="M7.5 7.5a2.5 2.5 0 114 2c-.7.5-1.5 1-1.5 2.2" stroke="#2563FF" stroke-width="1.3" stroke-linecap="round"/>
            <circle cx="10" cy="14.5" r="0.7" fill="#2563FF"/>
          </svg>
          <div class="tips-content">
            <div class="tips-title">使用提示</div>
            <div class="tips-text">
              1. 建议上传清晰的产品图与参考图，参考图越多，生成效果越丰富<br>
              2. 生成时间会根据图片数量和复杂度有所不同，请耐心等待
            </div>
          </div>
        </div>
      </div>

      <!-- ===== Divider + Toggle: canvas ⇔ right panel ===== -->
      <div class="col-divider-wrapper">
        <div class="col-divider" @mousedown="startColResize($event, 'right')"></div>
        <div class="config-toggle-btn" @click="configCollapsed = !configCollapsed" :title="configCollapsed ? '展开创作配置' : '折叠创作配置'">
          <svg viewBox="0 0 16 16" fill="none" :style="{ transform: configCollapsed ? 'rotate(180deg)' : 'rotate(0deg)' }">
            <path d="M6 4l4 4-4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
      </div>

      <!-- ===== Right Column: Config + AI ===== -->
      <div class="right-col" :style="{ flex: rightFlex }">
        <div class="right-panel-divider" @mousedown="startRightPanelResize($event, 'config')"></div>

        <!-- ===== Config Panel (创作配置) ===== -->
        <div class="config-col" :class="{ collapsed: configCollapsed }" :style="{ flex: configFlex }">
          <div class="config-scroll" v-show="!configCollapsed">
            <div class="config-inner">
              <div class="panel-header" @click="toggleAllSections">
                <span>创作配置</span>
                <span class="panel-toggle-all">{{ allExpanded ? '全部折叠 ▲' : '全部展开 ▼' }}</span>
              </div>

              <!-- 反推提示词入口 -->
              <div class="reverse-prompt-entry">
                <el-button type="primary" plain class="reverse-prompt-btn" @click="openReversePromptDialog">
                  <el-icon><MagicStick /></el-icon>
                  <span>反推提示词</span>
                </el-button>
                <p class="entry-helper">上传参考图，AI 帮你描述想要的画面效果</p>
              </div>

              <!-- Section: 上传素材 -->
              <div class="config-section collapsible">
                <div class="section-header collapsible" @click="toggleSection('upload')">
                  <span class="section-label"><span class="required-mark">*</span>上传素材<span class="required-mark">（必填）</span></span>
                  <span class="expand-text">
                    {{ sections.upload ? '收起' : '展开' }}
                    <svg :size="12" class="expand-arrow" :class="{ expanded: sections.upload }" viewBox="0 0 12 12" fill="none"><path d="M3 4.5l3 3 3-3" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"/></svg>
                  </span>
                </div>
                <div class="section-body" v-show="sections.upload">
                  <!-- 产品图 -->
                  <div class="upload-section">
                    <div class="upload-label">产品图 <span class="required">（必传）</span></div>
                    <div class="upload-zone" @click="triggerProductUpload">
                      <svg viewBox="0 0 28 28" fill="none"><path d="M14 7v10M10 11l4-4 4 4" stroke="#9CA3AF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/><path d="M6 18v4a2 2 0 002 2h12a2 2 0 002-2v-4" stroke="#9CA3AF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
                      <div class="upload-text">点击上传产品图</div>
                      <div class="upload-hint">支持 JPG / PNG / WebP，最多 10 张</div>
                    </div>
                    <div class="upload-count">
                      <span class="upload-count-text">已上传 <strong>{{ productImages.length }}</strong> / 50</span>
                      <span class="upload-clear" v-if="productImages.length > 0" @click="productImages = []">清空</span>
                    </div>
                    <input ref="productInput" type="file" multiple accept="image/jpeg,image/png,image/webp" style="display:none" @change="handleProductUpload" />
                  </div>

                  <!-- 参考图 -->
                  <div class="upload-section">
                    <div class="upload-label">参考图 <span class="optional">（选传）</span></div>
                    <div class="upload-zone" @click="triggerRefUpload">
                      <svg viewBox="0 0 28 28" fill="none"><path d="M14 7v10M10 11l4-4 4 4" stroke="#9CA3AF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/><path d="M6 18v4a2 2 0 002 2h12a2 2 0 002-2v-4" stroke="#9CA3AF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
                      <div class="upload-text">点击上传参考图</div>
                      <div class="upload-hint">支持 JPG / PNG / WebP，最多 10 张</div>
                    </div>
                    <div class="upload-count">
                      <span class="upload-count-text">已上传 <strong>{{ refImages.length }}</strong> / 20</span>
                      <span class="upload-clear" v-if="refImages.length > 0" @click="refImages = []">清空</span>
                    </div>
                    <input ref="refInput" type="file" multiple accept="image/jpeg,image/png,image/webp" style="display:none" @change="handleRefUpload" />
                  </div>
                </div>
              </div>

              <!-- Section: 核心卖点 -->
              <div class="config-section collapsible">
                <div class="section-header collapsible" @click="toggleSection('selling')">
                  <span class="section-label">核心卖点</span>
                  <span class="expand-text">
                    {{ sections.selling ? '收起' : '展开' }}
                    <svg :size="12" class="expand-arrow" :class="{ expanded: sections.selling }" viewBox="0 0 12 12" fill="none"><path d="M3 4.5l3 3 3-3" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"/></svg>
                  </span>
                </div>
                <div class="section-body" v-show="sections.selling">
                  <div class="checkbox-grid">
                    <div
                      v-for="(point, i) in sellingPoints"
                      :key="i"
                      class="checkbox-item"
                      @click="toggleSellingPoint(i)"
                    >
                      <div class="checkbox-box" :class="{ checked: point.checked }">
                        <svg v-if="point.checked" viewBox="0 0 10 10" fill="none"><path d="M2 5l2.2 2.2L8 3" stroke="#fff" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
                      </div>
                      {{ point.label }}
                    </div>
                    <div class="checkbox-add" @click="addCustomSellingPoint">
                      <svg width="12" height="12" viewBox="0 0 12 12" fill="none"><path d="M6 2.5v7M2.5 6h7" stroke="#2563FF" stroke-width="1.3" stroke-linecap="round"/></svg>
                      自定义卖点
                    </div>
                  </div>
                </div>
              </div>

              <!-- Section: 生成数量 -->
              <div class="config-section collapsible">
                <div class="section-header collapsible" @click="toggleSection('count')">
                  <span class="section-label">生成数量</span>
                  <span class="expand-text">
                    {{ sections.count ? '收起' : '展开' }}
                    <svg :size="12" class="expand-arrow" :class="{ expanded: sections.count }" viewBox="0 0 12 12" fill="none"><path d="M3 4.5l3 3 3-3" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"/></svg>
                  </span>
                </div>
                <div class="section-body" v-show="sections.count">
                  <div class="gen-count-row">
                    <el-input-number v-model="genCount" :min="1" :max="maxGenerateCount" size="small" controls-position="right" style="width: 120px" />
                  </div>
                </div>
              </div>

              <!-- Section: 输出设置 -->
              <div class="config-section collapsible">
                <div class="section-header collapsible" @click="toggleSection('output')">
                  <span class="section-label">输出设置</span>
                  <span class="expand-text">
                    {{ sections.output ? '收起' : '展开' }}
                    <svg :size="12" class="expand-arrow" :class="{ expanded: sections.output }" viewBox="0 0 12 12" fill="none"><path d="M3 4.5l3 3 3-3" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"/></svg>
                  </span>
                </div>
                <div class="section-body" v-show="sections.output">
                  <div class="output-group">
                    <div class="output-label">图片格式</div>
                    <div class="radio-group">
                      <button
                        v-for="fmt in formatOptions"
                        :key="fmt"
                        class="radio-btn"
                        :class="{ active: outputFormat === fmt }"
                        @click="outputFormat = outputFormat === fmt ? '' : fmt"
                      >{{ fmt }}</button>
                    </div>
                  </div>

                  <div class="output-group">
                    <div class="output-label">画质</div>
                    <div class="radio-group">
                      <button
                        v-for="q in qualityOptions"
                        :key="q.value"
                        class="radio-btn"
                        :class="{ active: outputQuality === q.value }"
                        @click="outputQuality = outputQuality === q.value ? '' : q.value"
                      >{{ q.label }}</button>
                    </div>
                  </div>

                  <div class="output-group">
                    <div class="output-label">输出尺寸</div>
                    <select class="size-select" v-model="outputSize">
                      <option v-for="s in sizeOptions" :key="s.value" :value="s.value">{{ s.label }}</option>
                    </select>
                  </div>

                  <!-- 自定义尺寸输入 -->
                  <div class="output-group" v-if="outputSize === 'custom'">
                    <div class="output-label">自定义尺寸（像素）</div>
                    <div class="custom-size-row">
                      <input type="number" v-model.number="customWidth" placeholder="宽度" class="size-input" min="100" />
                      <span class="size-x">×</span>
                      <input type="number" v-model.number="customHeight" placeholder="高度" class="size-input" min="100" />
                    </div>
                  </div>
                </div>
              </div>

              <!-- Section: 语言 -->
              <div class="config-section collapsible">
                <div class="section-header collapsible" @click="toggleSection('language')">
                  <span class="section-label">语言</span>
                  <span class="expand-text">
                    {{ sections.language ? '收起' : '展开' }}
                    <svg :size="12" class="expand-arrow" :class="{ expanded: sections.language }" viewBox="0 0 12 12" fill="none"><path d="M3 4.5l3 3 3-3" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"/></svg>
                  </span>
                </div>
                <div class="section-body" v-show="sections.language">
                  <el-select v-model="language" style="width: 100%">
                    <el-option v-for="l in languages" :key="l.value" :label="l.label" :value="l.value" />
                  </el-select>
                  <p class="section-helper">选择输出图片上文字的语言，适配跨境电商场景</p>
                </div>
              </div>

              <!-- Generate Button -->
              <!-- 生成按钮已移除，请通过AI助手发送 -->
            </div>
          </div>
        </div>

        <!-- ===== Divider inside right panel: config ⇔ AI ===== -->
        <div class="right-panel-divider" @mousedown="startRightPanelResize($event, 'ai')"></div>

        <!-- ===== AI Assistant Column ===== -->
        <div class="ai-col" :style="{ flex: aiFlex }">
          
          <AiAssistant ref="aiAssistantRef" :generate-fn="handleGenerate" :is-generating="isGenerating" :gen-status="genStatus" :gen-progress="genProgress" :gen-error="genError" :has-image="productImages.length > 0" :on-clear-images="clearWorkspaceImages" />
        </div>
      </div>
    </div>

    <!-- 任务结果查看弹窗 -->
    <el-dialog
      v-model="taskDetailVisible"
      :title="currentTaskDetail?.name || '任务详情'"
      width="720px"
      :close-on-click-modal="true"
      append-to-body
      class="task-detail-dialog"
    >
      <div class="task-detail-body" v-if="currentTaskDetail">
        <!-- 任务信息 -->
        <div class="td-info-row">
          <div class="td-info-item"><span class="td-label">设计类型</span><span class="td-value">{{ currentTaskDetail.designType }}</span></div>
          <div class="td-info-item"><span class="td-label">素材数量</span><span class="td-value">{{ currentTaskDetail.materialCount }}</span></div>
          <div class="td-info-item"><span class="td-label">生成数量</span><span class="td-value">{{ currentTaskDetail.genCount }} 套</span></div>
          <div class="td-info-item"><span class="td-label">创建时间</span><span class="td-value">{{ currentTaskDetail.createdAt }}</span></div>
        </div>
        <!-- 状态 -->
        <div class="td-status-row">
          <span class="td-label">状态：</span>
          <span class="status-dot" :class="currentTaskDetail.statusClass">{{ currentTaskDetail.statusText }}</span>
          <div class="progress-cell" v-if="currentTaskDetail.progress !== null" style="margin-left: 16px">
            <div class="progress-bar"><div class="progress-fill" :class="currentTaskDetail.statusClass" :style="{ width: currentTaskDetail.progress + '%' }"></div></div>
            <span class="progress-text">{{ currentTaskDetail.progress }}%</span>
          </div>
        </div>
        <!-- 结果图片 -->
        <div class="td-section-title" v-if="currentTaskDetail.resultImages && currentTaskDetail.resultImages.length > 0">生成结果</div>
        <div class="td-result-grid" v-if="currentTaskDetail.resultImages && currentTaskDetail.resultImages.length > 0">
          <div class="td-result-item" v-for="(img, i) in currentTaskDetail.resultImages" :key="i">
            <img :src="img.url || img" class="td-result-img" @click="previewImage(img.url || img)" />
          </div>
        </div>
        <div class="td-empty" v-else>
          <p>暂无生成结果图片</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="taskDetailVisible = false">关闭</el-button>
        <el-button type="primary" v-if="currentTaskDetail?.status === 'done'" @click="downloadTask(currentTaskDetail)">
          下载全部
        </el-button>
      </template>
    </el-dialog>

    <!-- 图片预览 -->
    <el-image-viewer
      v-if="previewVisible"
      :url-list="[previewUrl]"
      @close="previewVisible = false"
      hide-on-click-modal
    />

    <!-- 反推提示词模态框 -->
    <el-dialog
      v-model="reverseDialogVisible"
      title="反推提示词"
      width="560px"
      :close-on-click-modal="false"
      append-to-body
      class="reverse-prompt-dialog"
      draggable
    >
      <div class="reverse-prompt-body">
        <!-- 图片上传区 -->
        <div class="rp-upload-zone" @click="triggerReverseUpload" @dragover.prevent @drop.prevent="handleReverseDrop">
          <img v-if="reverseImagePreview" :src="reverseImagePreview" class="rp-preview-img" alt="" />
          <template v-else>
            <el-icon :size="36" color="#9CA3AF"><UploadFilled /></el-icon>
            <p class="rp-upload-text">点击或拖拽图片到此处</p>
            <p class="rp-upload-hint">支持 JPG/PNG/WebP，单张最大 7MB</p>
          </template>
          <button v-if="reverseImagePreview" class="rp-clear-btn" @click.stop="clearReverseImage">✕</button>
        </div>

        <!-- 提示词输入框 -->
        <div class="rp-prompt-row">
          <label class="rp-label">补充提示词</label>
          <el-input
            v-model="reversePromptInput"
            type="textarea"
            :rows="6"
            maxlength="1000"
            show-word-limit
          />
        </div>

        <!-- 结果区 -->
        <div v-if="reverseResult" class="rp-result-area">
          <div class="rp-result-header">
            <span class="rp-label">AI 推理结果</span>
            <el-button link type="primary" size="small" @click="copyResult(reverseResult)">
              <el-icon><DocumentCopy /></el-icon> 复制
            </el-button>
          </div>
          <div class="rp-result-box">{{ reverseResult }}</div>
        </div>
      </div>

      <template #footer>
        <el-button @click="reverseDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="reverseLoading" :disabled="!reverseImageFile" @click="submitReversePrompt">
          {{ reverseLoading ? '推理中…' : '发送推理' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'BatchProcessView' })
import { ref, computed, onMounted, onBeforeUnmount, onActivated, nextTick, watch } from 'vue'
import { compressImage } from '@/utils/compress'
import AiAssistant from '@/components/AiAssistant.vue'
// import { useCanvasInteractions } from '@/composables/useCanvasInteractions'
// import CanvasOverlay from '@/components/CanvasOverlay.vue'
import { useImageGeneration } from '@/composables/useImageGeneration'
import { useBatchTasks } from '@/composables/useBatchTasks'
import { ElMessage, ElImageViewer } from 'element-plus'
import { getPublicCreationConfigByGroup, reversePrompt } from '@/api/customer'
import { MagicStick, DocumentCopy, UploadFilled } from '@element-plus/icons-vue'

// const { canvasUI, handleCanvasExport } = useCanvasInteractions({
//   canvasSelector: '.canvas-box',
//   defaultName: 'batch-process',
// })
const gen = useImageGeneration('render')
const isGenerating = computed(() => gen.generating.value)
const genProgress = computed(() => gen.progress.value)
const genStatus = computed(() => gen.statusText.value)
const genError = computed(() => gen.error.value)
const resultImages = computed(() => gen.resultImages.value.map(img => {
  const url = img.url || img
  return { url }
}))

// ==================== AI Assistant ====================
const aiAssistantRef = ref(null)

// ===== 反推提示词 =====
const reverseDialogVisible = ref(false)
const reverseImageFile = ref(null)
const reverseImagePreview = ref('')
const reverseResult = ref('')
const reverseLoading = ref(false)
const REVERSE_DEFAULT_PROMPT = `请对原图进行逆向视觉解构，推测其生成逻辑与核心构成元素。请以结构化、专业的中文提示词格式输出，需涵盖：结构布局与质感；关键细节；技术参数与视角。 输出结果应具有高度可复用性，能直接用于引导图像生成。`
const reversePromptInput = ref(REVERSE_DEFAULT_PROMPT)

function openReversePromptDialog() {
  reverseDialogVisible.value = true
}

function triggerReverseUpload() {
  if (reverseImagePreview.value) return
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp'
  input.onchange = (e) => {
    const file = e.target.files?.[0]
    if (file) handleReverseFile(file)
    e.target.value = ''
  }
  input.click()
}

function handleReverseDrop(e) {
  const file = e.dataTransfer?.files?.[0]
  if (file) handleReverseFile(file)
}

const REVERSE_ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const REVERSE_MAX_SIZE = 1.5 * 1024 * 1024

async function handleReverseFile(file) {
  if (!REVERSE_ALLOWED_TYPES.includes(file.type)) {
    ElMessage.error('仅支持 JPG / PNG / WebP 格式的图片')
    return
  }
  let targetFile = file
  if (targetFile.size > REVERSE_MAX_SIZE) {
    targetFile = await compressImage(targetFile, 1.5)
  }
  reverseImageFile.value = targetFile
  reverseResult.value = ''
  const reader = new FileReader()
  reader.onload = (ev) => { reverseImagePreview.value = ev.target.result }
  reader.readAsDataURL(targetFile)
}

function clearReverseImage() {
  reverseImageFile.value = null
  reverseImagePreview.value = ''
  reverseResult.value = ''
}

async function submitReversePrompt() {
  if (!reverseImageFile.value) {
    ElMessage.warning('请先上传一张图片')
    return
  }
  reverseLoading.value = true
  reverseResult.value = ''
  try {
    const imageDataUri = reverseImagePreview.value
    const prompt = reversePromptInput.value?.trim()
      ? reversePromptInput.value.trim()
      : REVERSE_DEFAULT_PROMPT
    const res = await reversePrompt({ image: imageDataUri, prompt })
    const data = res?.data || res
    const result = typeof data === 'string' ? data : (data?.prompt || data?.result || '')
    reverseResult.value = result || 'AI 未返回文本结果'
    ElMessage.success('推理完成')
  } catch (e) {
    console.error('反推提示词失败:', e)
    ElMessage.error(e?.message || '反推提示词失败，请重试')
  } finally {
    reverseLoading.value = false
  }
}

async function copyResult(text) {
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

// ==================== Steps ====================
const currentStep = ref(1)

// ==================== Canvas ====================
const zoomLevel = ref(100)


// function zoomIn() {
//   zoomLevel.value = Math.min(200, zoomLevel.value + 25)
// }
// function zoomOut() {
//   zoomLevel.value = Math.max(25, zoomLevel.value - 25)
// }
function fitToScreen() {
  zoomLevel.value = 100
}
function toggleFullscreen() {
  // fullscreen toggle placeholder
}

// ==================== Task List ====================
const { tasks, createTask, updateTask, updateProgress, deleteTask: removeTask, clearAll, reloadTasks, fixStaleTasks } = useBatchTasks()
const taskListExpanded = ref(true)
const taskFilter = ref('all')
const taskTabs = [
  { key: 'all', label: '全部' },
  { key: 'processing', label: '处理中' },
  { key: 'queued', label: '排队中' },
  { key: 'done', label: '已完成' },
  { key: 'failed', label: '生成失败' }
]

const filteredTasks = computed(() => {
  if (taskFilter.value === 'all') return tasks.value
  return tasks.value.filter(t => t.status === taskFilter.value)
})

// 任务详情弹窗
const taskDetailVisible = ref(false)
const currentTaskDetail = ref(null)
// 图片预览
const previewVisible = ref(false)
const previewUrl = ref('')

function clearTasks() {
  clearAll()
}

function viewTask(task) {
  currentTaskDetail.value = task
  taskDetailVisible.value = true
}

function previewImage(url) {
  previewUrl.value = url
  previewVisible.value = true
}

function downloadTask(task) {
  if (!task.resultImages || task.resultImages.length === 0) {
    ElMessage.warning('该任务没有可下载的图片')
    return
  }
  task.resultImages.forEach((img, i) => {
    const url = img.url || img
    const link = document.createElement('a')
    link.href = url
    link.download = `${task.name}_第${i + 1}张.jpg`
    link.target = '_blank'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  })
  ElMessage.success(`开始下载 ${task.resultImages.length} 张图片`)
}

function deleteTask(task) {
  removeTask(task.id)
}

// ==================== Upload ====================
const productImages = ref([])
const refImages = ref([])
const productInput = ref(null)
const refInput = ref(null)

function triggerProductUpload() {
  productInput.value?.click()
}
function triggerRefUpload() {
  refInput.value?.click()
}

async function handleProductUpload(e) {
  const files = Array.from(e.target.files || [])
  const processedFiles = []
  for (const file of files) {
    let targetFile = file
    if (targetFile.size > 7 * 1024 * 1024) {
      targetFile = await compressImage(targetFile, 7)
    }
    processedFiles.push(targetFile)
  }
  productImages.value = [...productImages.value, ...processedFiles].slice(0, 10)
}
async function handleRefUpload(e) {
  const files = Array.from(e.target.files || [])
  const processedFiles = []
  for (const file of files) {
    let targetFile = file
    if (targetFile.size > 7 * 1024 * 1024) {
      targetFile = await compressImage(targetFile, 7)
    }
    processedFiles.push(targetFile)
  }
  refImages.value = [...refImages.value, ...processedFiles].slice(0, 10)
}

// ==================== Selling Points ====================
const sellingPoints = ref([
  { label: '高品质材料', checked: true },
  { label: '耐用结实', checked: true },
  { label: '多功能使用', checked: true },
  { label: '易于安装', checked: false },
  { label: '防刮耐磨', checked: false },
  { label: '安全环保', checked: false },
  { label: '时尚设计', checked: false },
  { label: '性价比高', checked: false },
  { label: '智能设计', checked: false },
  { label: '抗菌防霉', checked: false },
  { label: '防潮防水', checked: false },
  { label: '静音减震', checked: false }
])

// ==================== Output Settings (defaults, overridden by loadCreationConfig) ====================
// qualityOptions 和 languages 已在上方声明，此处仅做注释说明它们会被后台配置覆盖

function toggleSellingPoint(index) {
  sellingPoints.value[index].checked = !sellingPoints.value[index].checked
}

function addCustomSellingPoint() {
  const name = prompt('请输入自定义卖点名称')
  if (name && name.trim()) {
    sellingPoints.value.push({ label: name.trim(), checked: true })
  }
}

// ==================== Generate Count ====================
const maxGenerateCount = ref(5)
const genCount = ref(1)

// ==================== Output Settings ====================
const formatOptions = ref(['JPG', 'PNG', 'WebP'])
const outputFormat = ref('JPG')
const qualityOptions = ref([
  { label: '标准', value: 'standard' },
  { label: '高清', value: 'hd' },
  { label: '超清', value: 'ultra' }
])
const outputQuality = ref('hd')
const outputSize = ref('')
const customWidth = ref(1600)
const customHeight = ref(1600)

// ==================== Size Options (defaults) ====================
const sizeOptions = ref([
  { label: '不指定尺寸', value: '' },
  { label: '1600 × 1600（1:1 主图）', value: '1600x1600' },
  { label: '2000 × 2000（高清 1:1）', value: '2000x2000' },
  { label: '1200 × 1800（3:2）', value: '1200x1800' },
  { label: '1800 × 1200（3:2 横版）', value: '1800x1200' },
  { label: '1200 × 1200（1:1 标准）', value: '1200x1200' },
  { label: '800 × 800（小尺寸）', value: '800x800' },
  { label: '自定义尺寸', value: 'custom' }
])

// ==================== Language ====================
const language = ref('zh-CN')
const languages = ref([
  { label: '中文（简体）', value: 'zh-CN' },
  { label: '英语（美国）', value: 'en-US' },
  { label: '英语（英国）', value: 'en-GB' },
  { label: '日语', value: 'ja-JP' },
  { label: '韩语', value: 'ko-KR' },
  { label: '德语', value: 'de-DE' },
  { label: '法语', value: 'fr-FR' },
  { label: '西班牙语', value: 'es-ES' },
])

// ==================== Prompt Boost ====================
// (提示词增强已移除)

// ==================== Generate ====================
const generating = ref(false)
const currentTaskId = ref(null)

// 监听结果图片，一旦画布有了返回的图片，立刻将当前生成任务的状态改为已完成，进度改为100%
watch(() => gen.resultImages.value, (newImages) => {
  if (newImages && newImages.length > 0 && currentTaskId.value) {
    stopProgressSimulation()
    const resultImages = newImages.map(img => {
      const url = img.url || img
      return { url }
    })
    updateTask(currentTaskId.value, {
      status: 'done',
      progress: 100,
      resultImages,
    })
  }
}, { deep: true })

// 进度模拟定时器
let progressTimer = null

function startProgressSimulation(taskId) {
  stopProgressSimulation()
  let p = 5
  progressTimer = setInterval(() => {
    p = Math.min(90, p + Math.random() * 8 + 2)
    updateProgress(taskId, Math.round(p))
  }, 1500)
}

function stopProgressSimulation() {
  if (progressTimer) {
    clearInterval(progressTimer)
    progressTimer = null
  }
}

async function handleGenerate(opts = {}) {
  if (productImages.value.length === 0) { ElMessage.warning('请先上传产品图片'); return }
  if (!(await gen.checkPoints(2))) { ElMessage.warning('积分不足，请先充值'); return }
  generating.value = true
  localStorage.setItem('gh_batch_cleared', 'false')

  // 创建任务记录
  const task = createTask({
    name: `批量生成 ${productImages.value.length} 张`,
    designType: '批量生成',
    materialCount: `产品图 ${productImages.value.length} 张${refImages.value.length > 0 ? ' / 参考图 ' + refImages.value.length + ' 张' : ''}`,
    genCount: genCount.value,
    thumbs: productImages.value.slice(0, 4).map(f => {
      if (f instanceof File) return { url: URL.createObjectURL(f) }
      if (typeof f === 'string') return { url: f }
      return {}
    }),
    prompt: aiAssistantRef.value?.inputText?.trim() || '',
    productImages: [], // Don't save File objects to localStorage
  })
  
  currentTaskId.value = task.id

  // 监听 recordId 并保存到任务中
  const unwatchRecordId = watch(() => gen.currentRecordId.value, (newId) => {
    if (newId) {
      updateTask(task.id, { recordId: newId })
      unwatchRecordId()
    }
  })

  // 更新为处理中
  updateTask(task.id, { status: 'processing', progress: 0 })
  startProgressSimulation(task.id)

  // 总超时保护：10分钟后如果还没完成，强制标记为超时失败
  const timeoutId = setTimeout(() => {
    stopProgressSimulation()
    unwatchRecordId()
    if (generating.value) {
      generating.value = false
      updateTask(task.id, { status: 'failed', errorMsg: '生成超时（超过10分钟），请减少图片数量或稍后重试' })
      ElMessage.error('生成超时，请减少图片数量或稍后重试')
    }
  }, 10 * 60 * 1000)

  try {
    const text = aiAssistantRef.value?.inputText?.trim() || ''
    const languageTextMap = {
      'zh-CN': '中文',
      'en-US': '英文',
      'en-GB': '英文',
      'ja-JP': '日文',
      'ko-KR': '韩文',
      'de-DE': '德文',
      'fr-FR': '法文',
      'es-ES': '西班牙文'
    }
    const langText = languageTextMap[language.value] || '中文'
    const basePrompt = `批量生成电商图片，共${productImages.value.length}张，数量${genCount.value}。图片上的文字使用${langText}。${text ? ' ' + text : ''}`
    await gen.fullGenerate(productImages.value, basePrompt, { consumePoints: 2, featureName: 'ai_assistant', title: '批量处理生成', n: genCount.value, model: opts.model, language: language.value })

    // 生成成功
    stopProgressSimulation()
    clearTimeout(timeoutId)
    unwatchRecordId()
    const resultImages = gen.resultImages.value.map(img => {
      const url = img.url || img
      return { url }
    })
    updateTask(task.id, {
      status: 'done',
      progress: 100,
      resultImages,
    })
    // 将结果图推入 AI 助手对话框
    aiAssistantRef.value?.addResultImages(resultImages)
} catch (e) {
clearTimeout(timeoutId)
unwatchRecordId()
if (e?.message?.includes('已取消')) {
stopProgressSimulation()
updateTask(task.id, { status: 'cancelled' })
return
}
console.error('批量生成失败:', e)
    stopProgressSimulation()
    // 即使 fullGenerate 抛出了异常，但如果已经拿到了结果图片，说明生成本身是成功的
    // （常见于 fullGenerate 内部重试逻辑中，图片已返回但后续重试抛错的场景）
    if (gen.resultImages.value && gen.resultImages.value.length > 0) {
      const resultImages = gen.resultImages.value.map(img => {
        const url = img.url || img
        return { url }
      })
      updateTask(task.id, {
        status: 'done',
        progress: 100,
        resultImages,
      })
    } else {
      const isTimeout = e?.code === 'ECONNABORTED'
        || /timeout|超时|人数过多|繁忙|busy|timed out/i.test(e?.message || '')
      const errorMsg = e?.message || ''
      let taskErrorMsg = '生成失败，请稍后重试'
      if (isTimeout) {
        taskErrorMsg = '生成超时，请减少图片数量或稍后重试'
        ElMessage.error('生成超时，当前模型处理时间较长，请稍后重试或减少图片数量')
      } else if (/network|网络|ECONNREFUSED|ERR_NETWORK/i.test(errorMsg)) {
        taskErrorMsg = '网络连接异常，请检查网络后重试'
        ElMessage.error('网络连接异常，请检查网络后重试')
      } else if (errorMsg) {
        taskErrorMsg = '生成失败：' + errorMsg
        ElMessage.error('生成失败：' + errorMsg)
      } else {
        ElMessage.error('生成失败，请稍后重试')
      }
      updateTask(task.id, { status: 'failed', errorMsg: taskErrorMsg })
    }
  } finally {
    generating.value = false
    stopProgressSimulation()
    currentTaskId.value = null
  }
}

// ==================== Section Collapse (like WhiteBg) ====================
const sections = ref({
  upload: true,
  selling: true,
  count: true,
  output: true,
  language: true
})

const allExpanded = computed(() => {
  return Object.values(sections.value).every(v => v)
})

function toggleSection(key) {
  sections.value[key] = !sections.value[key]
}

function toggleAllSections() {
  const target = !allExpanded.value
  Object.keys(sections.value).forEach(k => {
    sections.value[k] = target
  })
}

// ==================== Panel Layout ====================
const configCollapsed = ref(false)
const _configWidthPx = ref(320)
const _aiWidthPx = ref(360)

const canvasFlex = computed(() => '1 1 0%')
const rightFlex = computed(() => {
  const configW = configCollapsed.value ? 40 : _configWidthPx.value
  return `0 0 ${configW + _aiWidthPx.value + 12}px`
})
const configFlex = computed(() => {
  if (configCollapsed.value) return '0 0 40px'
  return `0 0 ${_configWidthPx.value}px`
})
const aiFlex = computed(() => `0 0 ${_aiWidthPx.value}px`)

// ---------- Column resize logic (like WhiteBg) ----------
let isResizing = false
let resizeTarget = ''

function startColResize(e, target) {
  isResizing = true
  resizeTarget = target
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
  e.preventDefault()
}

function onMouseMove(e) {
  if (!isResizing) return
  const threeCol = document.querySelector('.three-col')
  if (!threeCol) return
  const rect = threeCol.getBoundingClientRect()

  if (resizeTarget === 'right' || resizeTarget === 'config') {
    const rightWidth = rect.right - e.clientX - 24
    const totalCurrent = _configWidthPx.value + _aiWidthPx.value + 12
    if (totalCurrent > 0 && rightWidth > 200) {
      const ratio = rightWidth / totalCurrent
      _configWidthPx.value = Math.max(150, Math.min(600, Math.round(_configWidthPx.value * ratio)))
      _aiWidthPx.value = Math.max(200, Math.min(800, Math.round(_aiWidthPx.value * ratio)))
    }
  } else if (resizeTarget === 'ai') {
    const rightCol = document.querySelector('.right-col')
    if (!rightCol) return
    const rightRect = rightCol.getBoundingClientRect()
    const rightX = e.clientX - rightRect.left
    const aiWidth = rightRect.width - rightX - 6
    _aiWidthPx.value = Math.max(200, Math.min(800, Math.round(aiWidth)))
  }
}

function onMouseUp() {
  if (isResizing) {
    isResizing = false
    resizeTarget = ''
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
  }
}

function startRightPanelResize(e, target) {
  isResizing = true
  resizeTarget = target
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
  e.preventDefault()
}


// ==================== Lifecycle ====================
// ===== 从后台创作配置读取批量生成配置 =====
async function loadCreationConfig() {
  try {
    const res = await getPublicCreationConfigByGroup('batch_process')
    const list = res.data || res.rows || []
    const map = {}
    list.forEach(c => { map[c.configKey] = c })

    // ---- 核心卖点 ----
    const sellingCfg = map.selling_points
    if (sellingCfg && sellingCfg.configValue) {
      const arr = JSON.parse(sellingCfg.configValue)
      if (Array.isArray(arr) && arr.length) {
        // 保留之前的 checked 状态，仅更新 label 列表
        const oldMap = {}
        sellingPoints.value.forEach(s => { oldMap[s.label] = s.checked })
        sellingPoints.value = arr.map(s => {
          const label = s.label || s.value
          return { label, checked: oldMap[label] ?? false }
        })
      }
    }

    // ---- 图片格式 ----
    const formatCfg = map.format_options
    if (formatCfg && formatCfg.configValue) {
      const arr = JSON.parse(formatCfg.configValue)
      if (Array.isArray(arr) && arr.length) {
        const fmts = arr.map(s => s.value || s.label || s)
        formatOptions.value = fmts
        // 如果当前选中的格式不在新列表里，选第一个
        if (!fmts.includes(outputFormat.value)) outputFormat.value = fmts[0]
      }
    }

    // ---- 画质选项 ----
    const qualityCfg = map.quality_options
    if (qualityCfg && qualityCfg.configValue) {
      const arr = JSON.parse(qualityCfg.configValue)
      if (Array.isArray(arr) && arr.length) {
        qualityOptions.value = arr.map(s => ({ label: s.label || s.value, value: s.value }))
        // 确保当前选中的画质仍在新列表中
        if (!qualityOptions.value.some(q => q.value === outputQuality.value)) {
          outputQuality.value = qualityOptions.value[0]?.value || 'hd'
        }
      }
    }

    // ---- 尺寸设置 ----
    const sizeCfg = map.size_options
    if (sizeCfg && sizeCfg.configValue) {
      const arr = JSON.parse(sizeCfg.configValue)
      if (Array.isArray(arr) && arr.length) {
        const loaded = arr.map(s => {
          if (typeof s === 'string') return { label: s, value: s }
          // 兼容带 w/h 的格式
          if (s.w && s.h) return { label: s.label || s.value, value: s.value, w: s.w, h: s.h }
          return { label: s.label || s.value, value: s.value }
        })
        const hasCustom = loaded.some(s => s.value === 'custom')
        if (!hasCustom) loaded.push({ label: '自定义尺寸', value: 'custom' })
        sizeOptions.value = [{ label: '不指定尺寸', value: '' }, ...loaded]
      }
    }

    // ---- 最大产品图数量 ----
    const maxProductCfg = map.max_product_images
    if (maxProductCfg && maxProductCfg.configValue) {
      const n = Number(JSON.parse(maxProductCfg.configValue))
      if (n > 0) {
        // 更新上传上限提示文案中的数字（可选，此处仅保留配置值备用）
      }
    }

    // ---- 最大生成数量 ----
    const maxGenCfg = map.max_generate_count
    if (maxGenCfg && maxGenCfg.configValue) {
      const n = Number(JSON.parse(maxGenCfg.configValue))
      if (n > 0) maxGenerateCount.value = n
    }

    // ---- 语言列表 ----
    const langCfg = map.language_options
    if (langCfg && langCfg.configValue) {
      const arr = JSON.parse(langCfg.configValue)
      if (Array.isArray(arr) && arr.length) {
        languages.value = arr.filter(l => l.value)
        // 确保当前选中的语言仍在新列表中
        if (!languages.value.some(l => l.value === language.value)) {
          language.value = languages.value[0]?.value || 'zh-CN'
        }
      }
    }
  } catch { /* use defaults */ }
}

function restoreLatestWorkspace() {
  const cleared = localStorage.getItem('gh_batch_cleared')
  if (cleared === 'true') return
  
  const latestTask = tasks.value[0]
  if (!latestTask) return
  
  // 恢复 AI 助手的文本
  if (latestTask.prompt) {
    nextTick(() => {
      if (aiAssistantRef.value) {
        aiAssistantRef.value.inputText = latestTask.prompt
      }
    })
  }
  
  // 恢复生成结果到 gen.resultImages (如果是已完成的任务，并且当前 gen.resultImages 为空)
  if (latestTask.status === 'done' && latestTask.resultImages && latestTask.resultImages.length > 0) {
    if (gen.resultImages.value.length === 0) {
      gen.resultImages.value = latestTask.resultImages.map(img => img.url || img)
    }
  }
}

let isResuming = false
async function resumeStaleTasks() {
  if (isResuming) return
  isResuming = true
  try {
    const staleTasks = tasks.value.filter(t => t.status === 'processing' || t.status === 'queued')
    for (const t of staleTasks) {
      if (!t.recordId) {
        updateTask(t.id, { status: 'failed', errorMsg: '任务信息不完整' })
        continue
      }
      try {
        gen.generating.value = true
        generating.value = true
        startProgressSimulation(t.id)
        const data = await gen.pollResult(t.recordId)
        stopProgressSimulation()
        if (data && data.status === '2') {
          const resultImages = data.images.map(img => ({ url: img.url || img }))
          updateTask(t.id, { status: 'done', progress: 100, resultImages })
          if (tasks.value[0]?.id === t.id) {
            aiAssistantRef.value?.addResultImages(resultImages)
          }
        }
      } catch (e) {
        stopProgressSimulation()
        updateTask(t.id, { status: 'failed', errorMsg: e.message || '生成失败' })
      } finally {
        gen.generating.value = false
        generating.value = false
      }
    }
  } finally {
    isResuming = false
  }
}

onMounted(() => {
  loadCreationConfig()
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
  restoreLatestWorkspace()
  resumeStaleTasks()
})

// keep-alive 重新激活时刷新任务列表
onActivated(() => {
  reloadTasks()
  restoreLatestWorkspace()
  if (!generating.value) {
    resumeStaleTasks()
  }
})

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
  stopProgressSimulation()
})

function clearWorkspaceImages() {
  productImages.value = []
  refImages.value = []
  outputSize.value = ''
  language.value = 'zh-CN'
  gen.reset()
  localStorage.setItem('gh_batch_cleared', 'true')
}
</script>

<style scoped>
/* ============================================================
   Layout
   ============================================================ */
.workspace-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

/* ---- Steps Bar ---- */
.steps-bar {
  display: flex;
  align-items: center;
  padding: 0 0 12px;
  background: transparent;
  flex-shrink: 0;
  overflow-x: auto;
  gap: 0;
}
.step-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6B7280;
  white-space: nowrap;
  cursor: pointer;
}
.step-item.active { color: #2563FF; font-weight: 600; }
.step-num {
  width: 22px; height: 22px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 600;
  border: 2px solid #E8EDF5;
  flex-shrink: 0;
}
.step-item.active .step-num {
  background: #2563FF; color: #fff; border-color: #2563FF;
}
.step-item.done { color: #22C55E; }
.step-item.done .step-num {
  background: #22C55E; color: #fff; border-color: #22C55E;
}
.step-line {
  flex: 1; height: 2px; background: #E8EDF5; min-width: 12px; margin: 0 6px;
}
.step-line.done { background: #22C55E; }

/* ---- Three Column ---- */
.three-col {
  display: flex;
  flex: 1;
  overflow: hidden;
  min-height: 0;
}

/* ---- Column Divider + Toggle Wrapper ---- */
.col-divider-wrapper {
  position: relative;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  width: 24px;
}
.col-divider {
  width: 6px;
  height: 100%;
  background: transparent;
  cursor: col-resize;
  flex-shrink: 0;
  position: relative;
  z-index: 5;
  transition: background 0.2s;
}
.col-divider:hover,
.col-divider:active { background: #2563FF; }
.config-toggle-btn {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
  width: 20px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 1px solid #E8EDF5;
  border-radius: 8px 0 0 8px;
  cursor: pointer;
  color: #6B7280;
  box-shadow: -2px 0 8px rgba(0,0,0,0.06);
  transition: all 0.2s ease;
}
.config-toggle-btn:hover {
  background: #F0F4FF;
  color: #2563FF;
  border-color: #2563FF;
}
.config-toggle-btn svg {
  width: 14px;
  height: 14px;
  transition: transform 0.2s ease;
}

/* ---- Right Panel Divider ---- */
.right-panel-divider {
  width: 6px;
  background: transparent;
  cursor: col-resize;
  flex-shrink: 0;
  position: relative;
  z-index: 5;
  transition: background 0.2s;
}
.right-panel-divider:hover,
.right-panel-divider:active { background: #2563FF; }

/* ============================================================
   Canvas Column
   ============================================================ */
.canvas-col {
  display: flex;
  flex-direction: column;
  padding: 16px;
  overflow: hidden;
  background: #F7F9FC;
  min-width: 0;
  gap: 12px;
}

/* Canvas Toolbar */
.canvas-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 12px;
  background: #fff;
  border: 1px solid #E8EDF5;
  border-radius: 10px;
  flex-shrink: 0;
}
.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.toolbar-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 5px 10px;
  border: 1px solid #E8EDF5;
  background: #fff;
  font-size: 12px;
  color: #6B7280;
  cursor: pointer;
  border-radius: 6px;
  transition: all .15s;
  font-family: inherit;
}
.toolbar-btn:hover { border-color: #2563FF; color: #2563FF; }
.toolbar-btn svg { width: 14px; height: 14px; }

.zoom-group {
  display: flex;
  align-items: center;
  gap: 0;
  border: 1px solid #E8EDF5;
  border-radius: 6px;
  overflow: hidden;
}
.zoom-btn {
  padding: 4px 8px;
  border: none;
  background: #fff;
  font-size: 13px;
  color: #6B7280;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: inherit;
}
.zoom-btn:hover { background: #F7F9FC; color: #2563FF; }
.zoom-val {
  font-size: 12px;
  color: #1F2937;
  padding: 0 8px;
  border-left: 1px solid #E8EDF5;
  border-right: 1px solid #E8EDF5;
  line-height: 24px;
  font-weight: 500;
}

/* Canvas Box */
.canvas-box {
flex: 1;
border: 2px dashed #E8EDF5;
border-radius: 12px;
background: #fff;
display: flex;
align-items: center;
justify-content: center;
overflow: hidden;
min-height: 200px;
position: relative;
}
.canvas-placeholder {
display: flex;
flex-direction: column;
align-items: center;
justify-content: center;
gap: 8px;
color: #9CA3AF;
padding: 24px;
text-align: center;
}
.canvas-placeholder svg {
width: 48px;
height: 48px;
margin-bottom: 4px;
opacity: .4;
}
.canvas-placeholder h3 {
font-size: 14px;
color: #6B7280;
margin-bottom: 0;
font-weight: 500;
}
.canvas-placeholder p {
font-size: 12px;
color: #9CA3AF;
}

/* Canvas Result Grid */
.canvas-result-grid {
display: grid;
grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
gap: 10px;
padding: 12px;
width: 100%;
height: 100%;
overflow-y: auto;
align-content: start;
}
.canvas-result-item {
border-radius: 8px;
overflow: hidden;
border: 1px solid #E8EDF5;
aspect-ratio: 1;
cursor: pointer;
transition: border-color 0.2s;
}
.canvas-result-item:hover {
border-color: #2563FF;
}
.canvas-result-img {
width: 100%;
height: 100%;
object-fit: cover;
display: block;
transition: transform 0.25s ease;
}
.canvas-result-item:hover .canvas-result-img {
transform: scale(1.05);
}

/* Canvas Loading Overlay */
.canvas-loading {
position: absolute;
top: 0;
left: 0;
right: 0;
bottom: 0;
display: flex;
flex-direction: column;
align-items: center;
justify-content: center;
gap: 12px;
background: rgba(255, 255, 255, 0.85);
z-index: 10;
}
.loading-spinner {
width: 36px;
height: 36px;
border: 3px solid #E8EDF5;
border-top-color: #2563FF;
border-radius: 50%;
animation: spin 0.8s linear infinite;
}
@keyframes spin {
to { transform: rotate(360deg); }
}
.canvas-loading p {
font-size: 13px;
color: #6B7280;
}

/* Task Card */
.task-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #E8EDF5;
  flex-shrink: 0;
  overflow: hidden;
}
.task-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px 0;
}
.task-title {
  font-size: 15px;
  font-weight: 600;
  color: #1F2937;
}
.task-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.task-action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border: none;
  background: transparent;
  font-size: 12px;
  color: #6B7280;
  cursor: pointer;
  border-radius: 6px;
  transition: all .15s;
  font-family: inherit;
}
.task-action-btn:hover { background: #F7F9FC; color: #2563FF; }
.task-action-btn svg { width: 14px; height: 14px; }
.task-fold {
  cursor: pointer;
  color: #9CA3AF;
  font-size: 14px;
  padding: 4px;
}
.task-fold:hover { color: #6B7280; }

/* Tabs */
.task-tabs {
  display: flex;
  gap: 0;
  padding: 12px 16px 0;
  border-bottom: 1px solid #E8EDF5;
}
.task-tab {
  padding: 8px 16px;
  font-size: 13px;
  color: #6B7280;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all .15s;
  font-weight: 500;
}
.task-tab:hover { color: #2563FF; }
.task-tab.active { color: #2563FF; border-bottom-color: #2563FF; }

/* Table - 紧凑样式确保3行能放下 */
.task-table-wrap {
  max-height: 180px;
  overflow-y: auto;
}
.task-table {
  width: 100%;
  border-collapse: collapse;
}
.task-table th {
  padding: 8px 10px;
  text-align: left;
  font-size: 11px;
  color: #9CA3AF;
  font-weight: 500;
  background: #FAFBFC;
  border-bottom: 1px solid #E8EDF5;
  position: sticky;
  top: 0;
}
.task-table td {
  padding: 8px 10px;
  font-size: 12px;
  color: #1F2937;
  border-bottom: 1px solid #F7F9FC;
  vertical-align: middle;
}
.task-table tr:last-child td { border-bottom: none; }
.task-name {
  display: flex;
  align-items: center;
  gap: 6px;
}
.task-thumbs { display: flex; gap: 3px; }
.task-thumb { width: 24px; height: 24px; border-radius: 4px; overflow: hidden; flex-shrink: 0; }
.task-thumb .img-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #e8edf5, #f0f4fa);
}
.task-thumb .thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.task-name-text {
  font-size: 12px;
  font-weight: 500;
  color: #1F2937;
  line-height: 1.3;
}
.cell-meta { font-size: 11px; color: #6B7280; }
.cell-val { font-size: 11px; }

.status-dot {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 500;
}
.status-dot::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}
.status-dot.blue { color: #2563FF; }
.status-dot.orange { color: #F59E0B; }
.status-dot.green { color: #22C55E; }
.status-dot.red { color: #EF4444; }
.status-dot.gray { color: #9CA3AF; }

.task-error-msg {
  font-size: 11px;
  color: #EF4444;
  margin-top: 4px;
  line-height: 1.3;
  word-break: break-all;
}

.progress-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 80px;
}
.progress-bar {
  flex: 1;
  height: 4px;
  background: #E8EDF5;
  border-radius: 2px;
  overflow: hidden;
}
.progress-fill { height: 100%; border-radius: 2px; }
.progress-fill.blue { background: #2563FF; }
.progress-fill.green { background: #22C55E; }
.progress-fill.orange { background: #F59E0B; }
.progress-fill.gray { background: #9CA3AF; }
.progress-text {
  font-size: 12px;
  color: #6B7280;
  white-space: nowrap;
}

.table-actions { display: flex; align-items: center; gap: 4px; }
.table-action-icon {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  color: #9CA3AF;
  transition: all .15s;
}
.table-action-icon:hover { background: #F7F9FC; color: #2563FF; }
.table-action-icon svg { width: 15px; height: 15px; }

.empty-row {
  text-align: center;
  color: #9CA3AF;
  font-size: 13px;
  padding: 24px 12px;
}

/* Tips Card - 紧凑样式 */
.tips-card {
  background: linear-gradient(135deg, #EEF2FF, #EFF6FF);
  border: 1px solid #DBEAFE;
  border-radius: 10px;
  padding: 10px 14px;
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
.tips-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  color: #2563FF;
  margin-top: 1px;
}
.tips-content { flex: 1; }
.tips-title { font-size: 12px; font-weight: 600; color: #1F2937; margin-bottom: 3px; }
.tips-text { font-size: 11px; color: #6B7280; line-height: 1.6; }

/* ============================================================
   Right Column (Config + AI)
   ============================================================ */
.right-col {
  display: flex;
  background: #fff;
  min-width: 0;
  overflow: hidden;
}

/* ============================================================
   Config Column (创作配置)
   ============================================================ */
.config-col {
  display: flex;
  flex-direction: column;
  background: #fff;
  min-width: 0;
  overflow: hidden;
  transition: flex 0.2s ease;
}
.config-col.collapsed {
  flex: 0 0 0 !important;
  min-width: 0;
  overflow: hidden;
}
.config-scroll {
  flex: 1;
  overflow-y: auto;
  min-width: 0;
}
.config-inner {
  padding: 0 16px 16px;
}

/* Panel Header */
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  cursor: pointer;
  user-select: none;
}
.panel-header span:first-child {
  font-size: 16px;
  font-weight: 700;
  color: #1F2937;
}
.panel-toggle-all {
  font-size: 12px;
  color: #2563FF;
  cursor: pointer;
}
.panel-toggle-all:hover { text-decoration: underline; }

/* Config Section */
.config-section {
  border-bottom: 1px solid #E8EDF5;
  margin-bottom: 0;
}
.config-section:last-of-type {
  border-bottom: none;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  cursor: pointer;
  user-select: none;
}
.section-header:hover .section-label { color: #2563FF; }
.section-label {
  font-size: 13px;
  font-weight: 600;
  color: #1F2937;
  transition: color 0.15s;
}
.required-mark { color: #EF4444; margin-right: 2px; font-weight: 500; }
.expand-text {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #9CA3AF;
}
.expand-arrow {
  width: 12px;
  height: 12px;
  transition: transform 0.2s ease;
}
.expand-arrow.expanded {
  transform: rotate(180deg);
}
.section-body {
  padding: 0 0 16px;
}

/* Upload Section */
.upload-section { margin-bottom: 16px; }
.upload-section:last-child { margin-bottom: 0; }
.upload-label {
  font-size: 13px;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.upload-label .required { color: #EF4444; font-size: 12px; }
.upload-label .optional { color: #9CA3AF; font-size: 12px; font-weight: 400; }
.upload-zone {
  border: 2px dashed #E8EDF5;
  border-radius: 10px;
  padding: 20px 16px;
  text-align: center;
  cursor: pointer;
  transition: all .2s;
}
.upload-zone:hover { border-color: #2563FF; background: #FAFBFF; }
.upload-zone svg { width: 28px; height: 28px; color: #9CA3AF; margin-bottom: 8px; }
.upload-text { font-size: 13px; color: #2563FF; font-weight: 500; margin-bottom: 4px; }
.upload-hint { font-size: 11px; color: #9CA3AF; }
.upload-count {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}
.upload-count-text { font-size: 12px; color: #6B7280; }
.upload-count-text strong { color: #1F2937; font-weight: 600; }
.upload-clear { font-size: 12px; color: #2563FF; cursor: pointer; }
.upload-clear:hover { text-decoration: underline; }

/* Checkbox Grid */
.checkbox-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.checkbox-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #1F2937;
  cursor: pointer;
  padding: 6px 8px;
  border-radius: 6px;
  transition: background .15s;
}
.checkbox-item:hover { background: #F7F9FC; }
.checkbox-box {
  width: 14px;
  height: 14px;
  border-radius: 3px;
  border: 1.5px solid #D1D5DB;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.15s;
}
.checkbox-box.checked { background: #2563FF; border-color: #2563FF; }
.checkbox-box svg { width: 10px; height: 10px; display: none; }
.checkbox-box.checked svg { display: block; }
.checkbox-add {
  grid-column: 1 / -1;
  font-size: 12px;
  color: #2563FF;
  cursor: pointer;
  padding: 6px 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.checkbox-add:hover { text-decoration: underline; }

/* Count Section */
.count-sublabel { font-size: 11px; color: #9CA3AF; margin-bottom: 10px; }
.gen-count-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.count-row { display: flex; align-items: center; gap: 8px; }
.count-group {
  display: flex;
  gap: 0;
  border: 1px solid #E8EDF5;
  border-radius: 8px;
  overflow: hidden;
  align-items: center;
  flex: 1;
}
.count-btn {
  flex: 1;
  padding: 7px 6px;
  font-size: 12px;
  cursor: pointer;
  background: #fff;
  border: none;
  color: #6B7280;
  border-right: 1px solid #E8EDF5;
  transition: all .15s;
  font-weight: 500;
  font-family: inherit;
  white-space: nowrap;
}
.count-btn:last-of-type { border-right: none; }
.count-btn.active { background: #2563FF; color: #fff; }
.count-btn:hover:not(.active) { background: #F7F9FC; }
.count-input {
  display: flex;
  align-items: center;
  border: 1px solid #E8EDF5;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}
.count-input input {
  width: 42px;
  padding: 6px 4px;
  border: none;
  text-align: center;
  font-size: 13px;
  font-weight: 600;
  color: #1F2937;
  outline: none;
  font-family: inherit;
}
.count-input-arrows { display: flex; flex-direction: column; border-left: 1px solid #E8EDF5; }
.count-input-arrow {
  padding: 1px 5px;
  font-size: 9px;
  color: #9CA3AF;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #E8EDF5;
  line-height: 1;
}
.count-input-arrow:last-child { border-bottom: none; }
.count-input-arrow:hover { background: #F7F9FC; color: #6B7280; }

/* Output Section */
.output-group { margin-bottom: 14px; }
.output-group:last-child { margin-bottom: 0; }
.output-label { font-size: 12px; color: #6B7280; margin-bottom: 8px; }
.radio-group {
  display: flex;
  gap: 0;
  border: 1px solid #E8EDF5;
  border-radius: 8px;
  overflow: hidden;
}
.radio-btn {
  flex: 1;
  padding: 7px 10px;
  font-size: 12px;
  cursor: pointer;
  background: #fff;
  border: none;
  color: #6B7280;
  text-align: center;
  border-right: 1px solid #E8EDF5;
  transition: all .15s;
  font-weight: 500;
  font-family: inherit;
}
.radio-btn:last-child { border-right: none; }
.radio-btn.active { background: #2563FF; color: #fff; }
.radio-btn:hover:not(.active) { background: #F7F9FC; }

/* Size Select */
.size-select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #E8EDF5;
  border-radius: 8px;
  font-size: 12px;
  color: #1F2937;
  outline: none;
  background: #fff;
  font-family: inherit;
  cursor: pointer;
}
.size-select:focus { border-color: #2563FF; }

/* Custom Size */
.custom-size-row { display: flex; align-items: center; gap: 8px; }
.size-input {
  flex: 1;
  padding: 7px 10px;
  border: 1px solid #E8EDF5;
  border-radius: 6px;
  font-size: 12px;
  color: #1F2937;
  outline: none;
  font-family: inherit;
  text-align: center;
}
.size-input:focus { border-color: #2563FF; }
.size-x { font-size: 13px; color: #9CA3AF; font-weight: 500; }

/* Section helper */
.section-helper {
  font-size: 11px;
  color: #9CA3AF;
  margin-top: 6px;
  line-height: 1.5;
}

/* Generate Button 已移除 */

/* ============================================================
   AI Column
   ============================================================ */
.ai-col {
  display: flex;
  flex-direction: column;
  background: #fff;
  border-left: 1px solid #E8EDF5;
  min-width: 0;
  overflow: hidden;
}

/* Scrollbar */
::-webkit-scrollbar { width: 4px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #E8EDF5; border-radius: 2px; }
::-webkit-scrollbar-thumb:hover { background: #D1D5DB; }

/* ===== 反推提示词入口按钮 ===== */
.reverse-prompt-entry {
  margin: 0 0 12px 0;
}
.reverse-prompt-btn {
  width: 100%;
  justify-content: center;
}
.entry-helper {
  font-size: 12px;
  color: #9CA3AF;
  margin: 6px 0 0 0;
  text-align: center;
}

/* ===== 反推提示词模态框 ===== */
.reverse-prompt-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.rp-upload-zone {
  position: relative;
  border: 1px dashed #D1D5DB;
  border-radius: 8px;
  min-height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
  background: #FAFBFC;
  transition: border-color 0.2s;
}
.rp-upload-zone:hover {
  border-color: #2563FF;
}
.rp-upload-text {
  font-size: 14px;
  color: #4B5563;
  margin: 8px 0 0 0;
}
.rp-upload-hint {
  font-size: 12px;
  color: #9CA3AF;
  margin: 4px 0 0 0;
}
.rp-preview-img {
  width: 100%;
  max-height: 320px;
  object-fit: contain;
  display: block;
}
.rp-clear-btn {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  border: none;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.rp-clear-btn:hover {
  background: #EF4444;
}
.rp-label {
  font-size: 13px;
  font-weight: 600;
  color: #1F2937;
  display: block;
}
.rp-prompt-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.rp-result-area {
  border-top: 1px solid #E5E7EB;
  padding-top: 12px;
}
.rp-result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}
.rp-result-box {
  background: #F3F4F6;
  border-radius: 6px;
  padding: 10px 12px;
  font-size: 13px;
  line-height: 1.6;
  color: #1F2937;
  white-space: pre-wrap;
  max-height: 180px;
  overflow-y: auto;
}

/* ===== 任务详情弹窗 ===== */
.task-detail-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.td-info-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}
.td-info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.td-label {
  font-size: 12px;
  color: #9CA3AF;
  font-weight: 400;
}
.td-value {
  font-size: 13px;
  color: #1F2937;
  font-weight: 500;
}
.td-status-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}
.td-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: -4px;
}
.td-result-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.td-result-item {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #E8EDF5;
  aspect-ratio: 1;
  cursor: pointer;
  transition: border-color 0.2s;
}
.td-result-item:hover {
  border-color: #2563FF;
}
.td-result-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.td-empty {
  text-align: center;
  padding: 40px 0;
  color: #9CA3AF;
  font-size: 13px;
}
</style>