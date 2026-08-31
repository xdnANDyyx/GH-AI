<template>
  <div class="admin-system">
    <el-tabs v-model="activeTab" type="border-card" class="system-tabs">
      <el-tab-pane label="系统配置" name="basic">
        <div class="section-header">
          <div>
            <h3 class="form-section-title">系统参数</h3>
            <p class="section-desc">维护平台公共参数与站点级配置。</p>
          </div>
          <el-button type="primary" @click="openConfigDialog()">
            <el-icon><Plus /></el-icon>新增参数
          </el-button>
        </div>

        <el-table v-loading="configLoading" :data="configList" :header-cell-style="headerStyle">
          <el-table-column prop="configName" label="参数名称" min-width="180" />
          <el-table-column prop="configValue" label="参数值" min-width="220" show-overflow-tooltip />
          <el-table-column prop="configType" label="内置" width="100">
            <template #default="{ row }">
              <el-tag :type="row.configType === 'Y' ? 'warning' : 'info'" size="small">{{ row.configType === 'Y' ? '是' : '否' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openConfigDialog(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDeleteConfig(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
        <el-tab-pane label="提示词配置" name="prompt">
        <!-- 提示词模板（原提示词选项库） -->
          <div class="section-header">
            <div>
              <h3 class="form-section-title">提示词模板</h3>
              <p class="section-desc">维护生图时可选择的提示词选项（产品类别、材质、场景、风格、卖点、镜头等）。配置后前台 AI 配置面板即时联动生效。</p>
            </div>
            <div class="filter-inline">
              <el-select v-model="libraryFilters.scope" placeholder="工作台" clearable filterable style="width: 160px" @change="onLibraryScopeChange">
                <el-option v-for="s in promptPickerScopeOptions.filter(o => o.value)" :key="s.value" :label="s.label" :value="s.value" />
              </el-select>
              <el-select v-model="libraryFilters.category" placeholder="分类" clearable filterable style="width: 160px" @change="fetchPromptLibrary">
                <el-option v-for="c in libraryFilterCategoryOptions" :key="c.value" :label="c.label" :value="c.value" />
              </el-select>
              <el-input v-model="libraryFilters.label" placeholder="显示名" clearable style="width: 160px" @keyup.enter="fetchPromptLibrary" />
              <!-- <el-input v-model="libraryFilters.promptKey" placeholder="Key" clearable style="width: 180px" /> -->
              
              <el-select v-model="libraryFilters.status" placeholder="状态" clearable style="width: 120px">
                <el-option label="全部" value="" />
                <el-option label="启用" value="0" />
                <el-option label="停用" value="1" />
              </el-select>
              <el-select v-model="libraryFilters.referenced" placeholder="引用状态" clearable style="width: 130px" @change="() => { libraryPageNum = 1; fetchPromptLibrary() }">
                <el-option label="全部" value="" />
                <el-option label="已引用" value="1" />
                <el-option label="未引用" value="0" />
              </el-select>
              <el-button @click="fetchPromptLibrary">查询</el-button>
              <el-button type="primary" @click="openLibraryDialog()">新增选项</el-button>
            </div>
          </div>

          <el-table v-loading="libraryLoading" :data="promptLibraryList" :header-cell-style="headerStyle">
            <el-table-column prop="scope" label="适用功能" width="150" show-overflow-tooltip>
              <template #default="{ row }">{{ getScopeLabel(row.scope) }}</template>
            </el-table-column>
            <el-table-column prop="category" label="分类" width="120">
              <template #default="{ row }">{{ getCategoryLabel(row.category) }}</template>
            </el-table-column>
            <el-table-column prop="label" label="显示名" min-width="140" show-overflow-tooltip />
            <el-table-column prop="promptText" label="提示词内容" min-width="260" show-overflow-tooltip />
            
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-switch :model-value="row.status === '0'" @change="(value) => toggleLibraryStatus(row, value)" />
              </template>
            </el-table-column>
            <el-table-column prop="referenced" label="引用状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="referencedPromptKeys.has(row.promptKey)" type="warning" size="small">已引用</el-tag>
                <el-tag v-else type="info" size="small">未引用</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="openLibraryDialog(row)">编辑</el-button>
                <el-tooltip v-if="referencedPromptKeys.has(row.promptKey)" content="该选项已被创作配置引用" placement="top">
                  <el-button link type="danger" size="small" disabled>删除</el-button>
                </el-tooltip>
                <el-button v-else link type="danger" size="small" @click="handleDeleteLibrary(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-if="libraryTotal > 0"
            class="table-pagination"
            background
            layout="total, sizes, prev, pager, next, jumper"
            :current-page="libraryPageNum"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="libraryPageSize"
            :total="libraryTotal"
            @size-change="(val) => { libraryPageSize = val; libraryPageNum = 1; fetchPromptLibrary() }"
            @current-change="(val) => { libraryPageNum = val; fetchPromptLibrary() }"
          />

      </el-tab-pane>
      <el-tab-pane label="创作配置" name="creation">
        <div class="section-header">
          <div>
            <h3 class="form-section-title">创作配置</h3>
            <p class="section-desc">维护前台各工作台的配置项（尺寸预设、场景选项、风格选项等）。配置后前台工作台自动读取生效。</p>
          </div>
          <div class="filter-inline">
            <el-select v-model="creationFilters.configGroup" placeholder="选择工作台" clearable filterable style="width: 200px" @change="fetchCreationConfigs">
              <el-option v-for="g in creationGroupOptions" :key="g.value" :label="g.label" :value="g.value" />
            </el-select>
            <el-select v-model="creationFilters.status" placeholder="状态" clearable style="width: 120px" @change="fetchCreationConfigs">
              <el-option label="全部" value="" />
              <el-option label="启用" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
            <el-button @click="fetchCreationConfigs">查询</el-button>
            <el-button v-if="canAddCreationConfig" type="primary" @click="openCreationDialog()">新增配置</el-button>
          </div>
        </div>

        <div v-if="!creationFilters.configGroup" class="creation-group-cards">
          <div v-for="g in creationGroupOptions" :key="g.value" class="creation-group-card" @click="creationFilters.configGroup = g.value; fetchCreationConfigs()">
            <div class="creation-group-icon">{{ g.icon }}</div>
            <div class="creation-group-info">
              <div class="creation-group-name">{{ g.label }}</div>
              <div class="creation-group-desc">{{ g.desc }}</div>
            </div>
            <div class="creation-group-count">{{ getGroupCount(g.value) }}项</div>
          </div>
        </div>

        <el-table v-loading="creationLoading" :data="creationConfigList" :header-cell-style="headerStyle">
          <el-table-column prop="configGroup" label="工作台" width="140">
            <template #default="{ row }">{{ getGroupLabel(row.configGroup) }}</template>
          </el-table-column>
          <el-table-column prop="configName" label="配置名称" min-width="180" />
          <!-- <el-table-column prop="configValue" label="配置值" min-width="280" show-overflow-tooltip>
            <template #default="{ row }">{{ formatConfigValue(row.configValue) }}</template>
          </el-table-column> -->
          <el-table-column prop="sort" label="排序" width="90" />
          <el-table-column prop="status" label="状态" width="110">
            <template #default="{ row }">
              <el-switch :model-value="row.status === '0'" @change="(value) => toggleCreationStatus(row, value)" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openCreationDialog(row)">编辑</el-button>
              <!-- <el-button link type="danger" size="small" @click="handleDeleteCreation(row)">删除</el-button> -->
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-if="creationTotal > 0"
          class="table-pagination"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :current-page="creationPageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="creationPageSize"
          :total="creationTotal"
          @size-change="(val) => { creationPageSize = val; creationPageNum = 1; fetchCreationConfigs() }"
          @current-change="(val) => { creationPageNum = val; fetchCreationConfigs() }"
        />
      </el-tab-pane>



      <el-tab-pane label="标签配置" name="tag">
        <div class="section-header">
          <div>
            <h3 class="form-section-title">标签体系</h3>
            <p class="section-desc">维护素材标签、功能标签等分类标签。标签可在素材管理、工作台配置中关联使用。</p>
          </div>
          <div class="filter-inline">
            <el-input v-model="tagFilters.tagName" placeholder="标签名称" clearable style="width: 180px" />
            <el-select v-model="tagFilters.tagType" placeholder="标签类型" clearable filterable style="width: 180px" @change="fetchTags">
              <el-option v-for="t in tagTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
            <el-select v-model="tagFilters.status" placeholder="状态" clearable style="width: 120px" @change="fetchTags">
              <el-option label="全部" value="" />
              <el-option label="启用" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
            <el-button @click="fetchTags">查询</el-button>
            <el-button type="primary" @click="openTagDialog()">新增标签</el-button>
          </div>
        </div>

        <el-table v-loading="tagLoading" :data="tagList" :header-cell-style="headerStyle">
          <el-table-column prop="tagName" label="标签名称" min-width="180" />
          <el-table-column prop="tagType" label="标签类型" width="160">
            <template #default="{ row }">{{ getTagTypeLabel(row.tagType) }}</template>
          </el-table-column>
          <el-table-column prop="sort" label="排序" width="90" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === '0' ? 'success' : 'info'" size="small">{{ row.status === '0' ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openTagDialog(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDeleteTag(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="configDialogVisible" :title="configForm.configId ? '编辑参数' : '新增参数'" width="560px">
      <el-form :model="configForm" label-width="100px">
        <el-form-item label="参数名称"><el-input v-model="configForm.configName" /></el-form-item>
        <el-form-item label="参数键名"><el-input v-model="configForm.configKey" /></el-form-item>
        <el-form-item label="参数值"><el-input v-model="configForm.configValue" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="内置参数">
          <el-select v-model="configForm.configType" style="width: 160px">
            <el-option label="否" value="N" />
            <el-option label="是" value="Y" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="configForm.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingConfig" @click="submitConfig">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="promptDialogVisible" :title="promptForm.id ? '编辑提示词模板' : '新增提示词模板'" width="620px">
      <el-form :model="promptForm" label-width="100px">
        <el-form-item label="所属模块"><el-input v-model="promptForm.module" /></el-form-item>
        <el-form-item label="模板名称"><el-input v-model="promptForm.name" /></el-form-item>
        <el-form-item label="模板内容"><el-input v-model="promptForm.content" type="textarea" :rows="5" /></el-form-item>
        <el-form-item label="默认模板">
          <el-select v-model="promptForm.isDefault" style="width: 160px">
            <el-option label="否" value="0" />
            <el-option label="是" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="promptForm.sort" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="promptForm.status" style="width: 160px">
            <el-option label="启用" value="0" />
            <el-option label="停用" value="1" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="promptDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingPrompt" @click="submitPrompt">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="libraryDialogVisible" :title="libraryForm.id ? '编辑提示词选项' : '新增提示词选项'" width="680px">
      <el-form :model="libraryForm" label-width="100px">
        <el-form-item label="分类" required>
          <el-select v-model="libraryForm.category" placeholder="请选择分类" filterable style="width: 100%">
            <el-option v-for="c in libraryFilterCategoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="显示名" required>
          <el-input v-model="libraryForm.label" placeholder="前端显示名称" />
        </el-form-item>
        <el-form-item label="提示词内容">
          <el-input v-model="libraryForm.promptText" type="textarea" :rows="5" placeholder="提示词内容（正向约束/负向约束/参数描述）" />
        </el-form-item>
        <el-form-item label="适用功能" required>
          <el-select
            v-model="libraryScopeArray"
            multiple
            collapse-tags
            collapse-tags-tooltip
            clearable
            filterable
            placeholder="请选择适用功能"
            style="width: 100%"
          >
            <el-option v-for="s in promptPickerScopeOptions.filter(o => o.value)" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <!-- <el-form-item label="优先级">
          <el-input-number v-model="libraryForm.priority" :min="0" />
        </el-form-item> -->
        <!-- <el-form-item label="默认">
          <el-select v-model="libraryForm.isDefault" style="width: 160px">
            <el-option label="否" value="0" />
            <el-option label="是" value="1" />
          </el-select>
        </el-form-item> -->
        <!-- <el-form-item label="排序">
          <el-input-number v-model="libraryForm.sort" :min="0" />
        </el-form-item> -->
        <el-form-item label="状态">
          <el-select v-model="libraryForm.status" style="width: 160px">
            <el-option label="启用" value="0" />
            <el-option label="停用" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="libraryForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="libraryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingLibrary" @click="submitLibrary">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="tagDialogVisible" :title="tagForm.id ? '编辑标签' : '新增标签'" width="520px">
      <el-form :model="tagForm" label-width="90px">
        <el-form-item label="标签名称"><el-input v-model="tagForm.tagName" /></el-form-item>
        <el-form-item label="标签类型">
          <el-select v-model="tagForm.tagType" placeholder="请选择标签类型" filterable style="width: 100%">
            <el-option v-for="t in tagTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="tagForm.sort" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="tagForm.status" style="width: 160px">
            <el-option label="启用" value="0" />
            <el-option label="停用" value="1" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tagDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingTag" @click="submitTag">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="creationDialogVisible" :title="creationForm.id ? '编辑创作配置' : '新增创作配置'" width="620px">
      <el-form :model="creationForm" label-width="100px">
        <el-form-item label="工作台" required>
          <el-select v-model="creationForm.configGroup" placeholder="请选择工作台" filterable style="width: 100%" @change="onConfigGroupInput">
            <el-option v-for="g in creationGroupOptions" :key="g.value" :label="g.label" :value="g.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置名称" required>
          <el-input v-model="creationForm.configName" placeholder="请输入配置名称，如：输出尺寸预设" />
        </el-form-item>
        <el-form-item label="配置值类型">
          <el-select :model-value="creationEditorType" style="width: 200px" @change="onEditorTypeChange">
            <el-option label="选项列表" value="options" />
            <el-option label="文本选项" value="string_list" />
            <el-option label="数字" value="number" />
            <el-option label="文本" value="string" />
            <el-option label="高级JSON" value="json" />
          </el-select>
          <span class="form-item-hint">根据内容自动识别，可手动切换</span>
        </el-form-item>

        <el-form-item v-if="creationEditorType === 'options'" label="选项列表">
          <div class="option-editor">
            <!-- 提示词库绑定工具条：先在「提示词配置」建好显示名，这里下拉选择即自动绑定其提示词Key -->
            <div class="option-picker-bar">
              <span class="option-picker-bar-label">🔗 从提示词库绑定</span>
              <el-select
                v-model="promptPickerCategory"
                placeholder="选择分类"
                filterable
                size="small"
                style="width: 150px"
                @change="onPickerCategoryManualChange"
              >
                <el-option v-for="c in promptPickerCategoryOptions" :key="c.value" :label="c.label" :value="c.value" />
              </el-select>
              <!-- <el-select
                v-model="promptPickerScope"
                placeholder="适用功能"
                filterable
                clearable
                size="small"
                style="width: 150px"
                @change="onPickerCategoryManualChange"
              >
                <el-option v-for="s in promptPickerScopeOptions" :key="s.value" :label="s.label" :value="s.value" />
              </el-select> -->
              <el-button link type="primary" size="small" :loading="promptPickerLoading" @click="loadPromptPicker">刷新</el-button>
              <span class="option-picker-bar-hint">选中显示名即等于选中其对应提示词，值自动填为提示词Key。</span>
            </div>

            <div v-for="(item, idx) in creationOptions" :key="idx" class="option-row">
              <!-- 绑定提示词库下拉：选择显示名 → 自动填 label / value -->
              <el-select
                :model-value="item._pickerKey"
                placeholder="从提示词库选择"
                filterable
                clearable
                no-data-text="请先在上方选择分类"
                class="option-picker"
                :loading="promptPickerLoading"
                @change="(val) => onPickPromptItem(item, val)"
              >
                <el-option
                  v-for="p in promptPickerItems"
                  :key="p.promptKey"
                  :label="p.label"
                  :value="p.promptKey"
                  :disabled="isPickerItemTaken(idx, p.promptKey)"
                >
                  <span class="picker-opt-label">{{ p.label }}</span>
                  <span v-if="isPickerItemTaken(idx, p.promptKey)" class="picker-opt-taken">已选用</span>
                  <span v-else class="picker-opt-key">{{ p.promptKey }}</span>
                </el-option>
              </el-select>

              <template v-for="field in creationOptionFields" :key="field.key">
                <!-- 列表绑定类配置（图片/阴影/尺寸）：选显示名即由下拉自动填好 label、value（及宽/高），
                     无需再手动填写这些列，故隐藏它们；图片列（仅图片/阴影类有）保留供上传/填写。
                     但如果有未匹配到提示词库的项（如旧数据或提示词库缺失某选项），则保留 label/value 列显示。 -->
                <template v-if="!(hideLabelValueColumns && (field.key === 'label' || field.key === 'value' || field.key === 'w' || field.key === 'h'))">
                  <el-input-number
                    v-if="field.type === 'number'"
                    v-model="item[field.key]"
                    :placeholder="field.label"
                    :controls="false"
                    class="option-input option-input-num"
                  />
                  <div v-else-if="field.type === 'image'" class="option-image-cell">
                    <div class="option-image-input-wrap">
                      <el-input
                        v-model="item[field.key]"
                        :placeholder="field.label + '（粘贴公网链接或点击右侧上传）'"
                        class="option-input"
                      />
                      <el-upload
                        class="option-image-uploader"
                        accept="image/*"
                        :show-file-list="false"
                        :action="creationUploadUrl"
                        :headers="creationUploadHeaders"
                        :before-upload="beforeOptionImageUpload"
                        :on-success="(res) => handleOptionImageSuccess(res, item)"
                        :on-error="handleOptionImageError"
                      >
                        <el-button size="small" type="primary" plain>上传</el-button>
                      </el-upload>
                    </div>
                    <img
                      v-if="item[field.key]"
                      :src="getImageUrl(item[field.key])"
                      class="option-image-preview"
                      alt=""
                    />
                  </div>
                  <el-input
                    v-else
                    v-model="item[field.key]"
                    :placeholder="field.label"
                    class="option-input"
                  />
                </template>
              </template>
              <el-button link type="danger" size="small" @click="removeOption(idx)">
                <el-icon><Delete /></el-icon>
              </el-button>
              <!-- 提示词内容预览已隐藏，不在界面显示 -->
              <!-- <div v-if="getPickerPromptText(item.value)" class="option-prompt-hint">
                📝 {{ getPickerPromptText(item.value) }}
              </div> -->
            </div>
            <el-button type="primary" plain size="small" @click="addOption">
              <el-icon><Plus /></el-icon>添加选项
            </el-button>
            <div class="option-tip">
              <template v-if="isImageOptionConfig">从提示词库选择显示名即自动填好名称与值（无需手动填），再为每项上传或填写图片即可。</template>
              <template v-else-if="pickerBoundOptionConfig">从提示词库选择显示名即自动填好名称与值（含尺寸），后续的值/宽/高无需手动填写。</template>
              <template v-else>每项至少填写"显示名"和"值"。可点上方"从提示词库选择"直接绑定已建好的显示名（值自动填为其提示词Key）。尺寸类配置可填宽/高；图片字段可填公网链接或上传到服务器。</template>
            </div>
          </div>
        </el-form-item>

        <el-form-item v-else-if="creationEditorType === 'string_list'" label="文本选项">
          <div class="tag-input-wrap">
            <el-tag
              v-for="(tag, idx) in creationStringList"
              :key="idx"
              closable
              @close="creationStringList.splice(idx, 1)"
              size="default"
            >{{ tag }}</el-tag>
            <el-input
              v-if="creationStringInputVisible"
              ref="creationStringInputRef"
              v-model="creationStringInputValue"
              size="small"
              style="width: 140px"
              @keyup.enter="addStringItem"
              @blur="addStringItem"
            />
            <el-button v-else size="small" @click="showStringInput">+ 添加</el-button>
          </div>
        </el-form-item>

        <el-form-item v-else-if="creationEditorType === 'number'" label="配置值">
          <el-input-number v-model="creationNumberValue" :min="1" :controls="true" style="width: 220px" />
        </el-form-item>

        <el-form-item v-else-if="creationEditorType === 'string'" label="配置值">
          <el-input v-model="creationStringValue" placeholder="请输入文本配置值" style="width: 100%" />
        </el-form-item>

        <el-form-item v-else label="配置值">
          <el-input
            v-model="creationJsonValue"
            type="textarea"
            :rows="8"
            placeholder='JSON格式，如 [{"label":"800×800","value":"800x800","w":800,"h":800}]'
          />
          <div class="option-tip">高级模式：直接编辑 JSON。切换到其它模式可重新结构化。</div>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="creationForm.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="creationForm.status">
            <el-radio label="0">启用</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="creationForm.remark" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="creationDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingCreation" @click="submitCreation">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import {
  addAdminCreationConfig,
  addAdminPromptLibrary,
  addAdminPromptTemplate,
  addAdminSystemConfig,
  addAdminTag,
  changeAdminCreationConfigStatus,
  delAdminCreationConfig,
  delAdminPromptTemplate,
  delAdminSystemConfig,
  delAdminTag,
  listAdminCreationConfigs,
  listAdminPromptLibrary,
  listAdminPromptTemplates,
  listAdminSystemConfigs,
  listAdminTags,
  removeAdminPromptLibrary,
  setAdminPromptTemplateDefault,
  updateAdminCreationConfig,
  updateAdminPromptLibrary,
  updateAdminPromptTemplate,
  updateAdminSystemConfig,
  updateAdminTag
} from '@/api/customer'
import { getImageUrl, normalizeImageUrl } from '@/utils/image'

const activeTab = ref('basic')
const configLoading = ref(false)
const creationLoading = ref(false)
const promptLoading = ref(false)
const tagLoading = ref(false)
const savingConfig = ref(false)
const savingPrompt = ref(false)
const savingTag = ref(false)
const savingCreation = ref(false)
const configDialogVisible = ref(false)
const promptDialogVisible = ref(false)
const tagDialogVisible = ref(false)
const creationDialogVisible = ref(false)

const configList = ref([])
const creationConfigList = ref([])
const promptTemplateList = ref([])
const tagList = ref([])

// ===== 分页状态 =====
// 提示词配置
const libraryPageNum = ref(1)
const libraryPageSize = ref(20)
const libraryTotal = ref(0)
// 创作配置
const creationPageNum = ref(1)
const creationPageSize = ref(20)
const creationTotal = ref(0)

const creationFilters = reactive({ configGroup: '', status: '' })
const promptFilters = reactive({ module: '', status: '' })
const tagFilters = reactive({ tagName: '', tagType: '', status: '' })

// 以下工作台的配置由提示词库自动同步，不需要手动新增
const autoSyncGroups = ['white_bg', 'bg_generation', 'main_image', 'detail_img', 'retouch', 'ai_model', 'size_mark', 'banner']
const canAddCreationConfig = computed(() => !autoSyncGroups.includes(creationFilters.configGroup))

const promptSubTab = ref('template')
const libraryLoading = ref(false)
const savingLibrary = ref(false)
const libraryDialogVisible = ref(false)
const promptLibraryList = ref([])
const libraryFilters = reactive({ category: '', label: '', promptKey: '', scope: '', status: '', referenced: '' })
// 存储被创作配置引用的 promptKeys
const referencedPromptKeys = ref(new Set())
// ===== 统一分类选项（提示词配置过滤 + 创作配置绑定共用一套值） =====
const unifiedCategoryOptions = [
  { value: 'opt_platform',   label: '平台' },
  { value: 'opt_scene',      label: '场景' },
  { value: 'opt_light',      label: '光线' },
  { value: 'opt_style',      label: '风格' },
  { value: 'opt_selling',    label: '卖点' },
  { value: 'opt_purpose',    label: '核心目的' },
  { value: 'opt_size',       label: '尺寸' },
  { value: 'opt_age',        label: '年龄' },
  { value: 'opt_gender',     label: '性别' },
  { value: 'opt_hairstyle',  label: '发型' },
  { value: 'opt_ethnicity',  label: '人种' },
  { value: 'opt_pose',       label: '姿势' },
  { value: 'opt_clothing',   label: '服装' },
  { value: 'opt_tool',       label: '精修工具' },
  { value: 'opt_banner_type',label: 'Banner类型' },
  { value: 'opt_template',   label: 'Banner模板' },
  { value: 'opt_page',       label: '内容结构' },
  { value: 'opt_quality',    label: '画质' },
  { value: 'opt_format',     label: '输出格式' },
  { value: 'opt_language',   label: '语言' },
  { value: 'opt_shadow',     label: '阴影' },
  { value: 'opt_line_style',  label: '线条样式' },
  { value: 'opt_unit',        label: '单位选择' },
 // { value: 'opt_size_template', label: '尺寸模板' },
  { value: 'function',       label: '约束' },
  { value: 'product',        label: '产品类别' },
  { value: 'material',       label: '材质' },
  { value: 'camera',         label: '镜头/角度' },
  { value: 'negative',       label: '负向约束' },
  { value: 'option',         label: '功能内选项' },
  { value: 'model_adapt',    label: '模型适配' }
]

// 兼容旧值 → 新值的映射（数据迁移用，将无 opt_ 前缀的分类值转为带 opt_ 的统一值）
const legacyCategoryMap = {
  function:    'opt_platform',   // "约束" → 归入"平台"（前台统一 scope=platform）
  platform:    'opt_platform',
  product:     'opt_platform',
  material:    'opt_platform',
  scene:       'opt_scene',
  style:       'opt_style',
  selling:     'opt_selling',
  size:        'opt_size',
  quality:     'opt_quality',
  negative:    'opt_quality',
  camera:      'opt_camera',
  option:      'opt_tool',
  model_adapt: 'opt_quality'
}

// 废弃：libraryCategoryOptions（已合并到 unifiedCategoryOptions）
// const libraryCategoryOptions = [...]
// 废弃：promptPickerCategoryOptions（已合并到 unifiedCategoryOptions）
// const promptPickerCategoryOptions = [...]

// 提示词配置过滤用：根据选中的工作台(scope)联动显示相关分类
// scope → configGroup 映射，复用 promptPickerCategoryOptions 的过滤逻辑
const scopeToConfigGroupMap = {
  white_bg: 'white_bg',
  change_bg: 'bg_generation',
  ai_model: 'ai_model',
  main_image: 'main_image',
  detail: 'detail_img',
  dimension: 'size_mark',
  retouch: 'retouch',
  banner: 'banner',
  batch: 'batch_process'
}
const libraryFilterCategoryOptions = computed(() => {
  // 未选择工作台时显示全量分类
  if (!libraryFilters.scope) return unifiedCategoryOptions
  const configGroup = scopeToConfigGroupMap[libraryFilters.scope]
  if (!configGroup) return unifiedCategoryOptions
  const optList = unifiedCategoryOptions.filter(o => o.value.startsWith('opt_'))
  let allowed = []
  if (configGroup === 'white_bg') {
    allowed = ['opt_shadow', 'opt_size']
  } else if (configGroup === 'bg_generation') {
    allowed = ['opt_platform', 'opt_scene', 'opt_light', 'opt_style', 'opt_size']
  } else if (configGroup === 'main_image') {
    allowed = ['opt_platform', 'opt_size', 'opt_purpose', 'opt_selling', 'opt_language']
  } else if (configGroup === 'detail_img') {
    allowed = ['opt_platform', 'opt_size', 'opt_selling', 'opt_page', 'opt_language']
  } else if (configGroup === 'size_mark') {
    allowed = ['opt_line_style', 'opt_size', 'opt_unit', 'opt_language']
  } else if (configGroup === 'retouch') {
    allowed = ['opt_tool', 'opt_quality', 'opt_format', 'opt_size']
  } else if (configGroup === 'ai_model') {
    allowed = ['opt_gender', 'opt_age', 'opt_hairstyle', 'opt_ethnicity', 'opt_pose', 'opt_clothing', 'opt_scene', 'opt_size']
  } else if (configGroup === 'banner') {
    allowed = ['opt_size', 'opt_banner_type', 'opt_purpose', 'opt_language']
  } else if (configGroup === 'batch_process') {
    allowed = ['opt_selling', 'opt_format', 'opt_quality', 'opt_size', 'opt_language']
  }
  // "约束"分类（function）始终出现在各工作台的联动分类列表中
  const functionOption = unifiedCategoryOptions.find(o => o.value === 'function')
  const filtered = optList.filter(o => allowed.includes(o.value))
  if (functionOption) filtered.push(functionOption)
  return filtered
})

// 创作配置绑定用（从统一列表中只取 opt_ 开头的 UI 选项库分类；AI白底图仅显示阴影+尺寸；白底图生成背景仅显示平台/场景/光线/风格/尺寸；主图设计仅显示平台/尺寸/用途/卖点；详情图/A+仅显示平台/尺寸/卖点/详情页模块）
const promptPickerCategoryOptions = computed(() => {
  const optList = unifiedCategoryOptions.filter(o => o.value.startsWith('opt_'))
  if (creationForm.configGroup === 'white_bg') {
    return optList.filter(o => ['opt_shadow', 'opt_size'].includes(o.value))
  }
  if (creationForm.configGroup === 'bg_generation') {
    return optList.filter(o => ['opt_platform', 'opt_scene', 'opt_light', 'opt_style', 'opt_size'].includes(o.value))
  }
  if (creationForm.configGroup === 'main_image') {
    return optList.filter(o => ['opt_platform', 'opt_size', 'opt_purpose', 'opt_selling', 'opt_language'].includes(o.value))
  }
  if (creationForm.configGroup === 'detail_img') {
    return optList.filter(o => ['opt_platform', 'opt_size', 'opt_selling', 'opt_page', 'opt_language'].includes(o.value))
  }
  if (creationForm.configGroup === 'size_mark') {
    return optList.filter(o => ['opt_line_style', 'opt_size', 'opt_size_template', 'opt_unit', 'opt_language'].includes(o.value))
  }
  if (creationForm.configGroup === 'retouch') {
    return optList.filter(o => ['opt_tool', 'opt_quality', 'opt_format', 'opt_size'].includes(o.value))
  }
  if (creationForm.configGroup === 'ai_model') {
    return optList.filter(o => ['opt_gender', 'opt_age', 'opt_hairstyle', 'opt_ethnicity', 'opt_pose', 'opt_clothing', 'opt_scene', 'opt_size'].includes(o.value))
  }
  if (creationForm.configGroup === 'banner') {
    return optList.filter(o => ['opt_size', 'opt_banner_type', 'opt_purpose', 'opt_language'].includes(o.value))
  }
  if (creationForm.configGroup === 'batch_process') {
    return optList.filter(o => ['opt_selling', 'opt_format', 'opt_quality', 'opt_size', 'opt_language'].includes(o.value))
  }
  return optList
})

const creationGroupOptions = [
  { value: 'common', label: '通用配置', desc: '语言列表、生成数量上限等', icon: '⚙' },
  { value: 'white_bg', label: 'AI白底图', desc: '阴影样式、输出尺寸', icon: '🖼' },
  { value: 'bg_generation', label: '白底图生成背景', desc: '平台、场景、光线、风格', icon: '🌅' },
  { value: 'retouch', label: '产品精修', desc: '精修工具、画质、格式', icon: '✨' },
  { value: 'ai_model', label: 'AI模特', desc: '性别、年龄、发型、姿势', icon: '👤' },
  { value: 'main_image', label: '主图设计', desc: '平台、画布尺寸、用途', icon: '📸' },
  { value: 'detail_img', label: '详情图/A+', desc: '页面尺寸、卖点、结构', icon: '📄' },
  { value: 'banner', label: 'Banner设计', desc: '画布尺寸、类型、目的', icon: '🎯' },
  { value: 'size_mark', label: '尺寸标记', desc: '线条样式、尺寸', icon: '📏' },
  { value: 'batch_process', label: '批量生成', desc: '卖点、输出设置', icon: '⚡' }
]

function getGroupLabel(value) {
  const g = creationGroupOptions.find(o => o.value === value)
  return g ? g.label : value
}

function getGroupCount(group) {
  return creationConfigList.value.filter(c => c.configGroup === group).length
}

// 格式化创作配置值：JSON 数组提取 label/value 展示，纯文本/数字原样展示
function formatConfigValue(raw) {
  if (!raw || typeof raw !== 'string') return raw || ''
  const text = raw.trim()
  if (!text) return ''
  // 尝试解析 JSON 数组
  if (text.startsWith('[')) {
    try {
      const arr = JSON.parse(text)
      if (Array.isArray(arr) && arr.length > 0) {
        if (typeof arr[0] === 'string') {
          return arr.join('、')
        }
        if (arr[0] && typeof arr[0] === 'object') {
          return arr.map(item => item.label || item.value || '').filter(Boolean).join('、')
        }
      }
    } catch { /* not JSON */ }
  }
  // 尝试解析 JSON 对象
  if (text.startsWith('{')) {
    try {
      const obj = JSON.parse(text)
      if (typeof obj === 'object' && obj !== null) {
        return Object.entries(obj).map(([k, v]) => `${k}: ${v}`).join('；')
      }
    } catch { /* not JSON */ }
  }
  return text
}

const tagTypeOptions = [
  { value: 'material', label: '素材标签' },
  { value: 'scene', label: '场景标签' },
  { value: 'style', label: '风格标签' },
  { value: 'product', label: '产品标签' },
  { value: 'platform', label: '平台标签' },
  { value: 'function', label: '功能标签' },
  { value: 'quality', label: '质量标签' },
  { value: 'user', label: '用户标签' },
  { value: 'custom', label: '自定义标签' }
]

function getTagTypeLabel(value) {
  const t = tagTypeOptions.find(o => o.value === value)
  return t ? t.label : value
}

// 提示词库分类/适用功能 中文显示（表格列展示用）
function getCategoryLabel(value) {
  if (!value) return '-'
  const c = unifiedCategoryOptions.find(o => o.value === value)
  return c ? c.label : value
}

function getScopeLabel(value) {
  if (!value) return '不限'
  // 支持逗号分隔的多 scope
  const parts = String(value).split(',').map(v => v.trim()).filter(Boolean)
  if (!parts.length) return '不限'
  const labels = parts.map(p => {
    const s = promptPickerScopeOptions.find(o => o.value === p)
    return s ? s.label : p
  })
  return labels.join('、')
}

const configForm = reactive(createDefaultConfigForm())
const promptForm = reactive(createDefaultPromptForm())
const tagForm = reactive(createDefaultTagForm())
const creationForm = reactive(createDefaultCreationForm())
const libraryForm = reactive(createDefaultLibraryForm())

// 提示词选项编辑：适用功能(scope)的多选绑定。scope 在后端为逗号分隔字符串，
// 这里用数组做下拉多选，提交时在 submitLibrary 中合并回 libraryForm.scope。
const libraryScopeArray = ref([])


// ========== 创作配置值结构化编辑器 ==========
const creationEditorType = ref('options')
const creationOptions = ref([])
const creationStringList = ref([])
const creationStringInputVisible = ref(false)
const creationStringInputValue = ref('')
const creationStringInputRef = ref(null)
const creationNumberValue = ref(0)
const creationStringValue = ref('')
const creationJsonValue = ref('')

// ========== 创作配置 ↔ 提示词库 联动选择器 ==========
// 选项列表编辑器里，每行可通过下拉直接选择提示词库的"显示名"，
// 选中后自动以其 promptKey 作为该选项的"值"，实现"选显示名=选对应提示词"。
const promptPickerItems = ref([])
const promptPickerLoading = ref(false)
const promptPickerCategory = ref('')
const promptPickerScope = ref('')
const promptPickerLoadedKey = ref('')
const promptPickerUserOverride = ref(false)

// 适用功能 scope（configGroup → scope 映射）
const promptPickerScopeOptions = [
  { value: '', label: '不限' },
  { value: 'change_bg', label: '白底图生成背景' },
  { value: 'white_bg', label: 'AI白底图' },
  { value: 'ai_model', label: 'AI模特' },
  { value: 'main_image', label: '主图设计' },
  { value: 'retouch', label: '产品精修' },
  { value: 'detail', label: '详情图/A+' },
  { value: 'dimension', label: '尺寸标记' },
  { value: 'banner', label: 'Banner设计' },
  { value: 'batch', label: '批量生成' }
]

// 由配置键反推提示词库分类（仅对"值=提示词Key"的配置键做自动推断；
// 出图参数类配置键不在此列，避免把尺寸字串误绑成 promptKey）
function inferPromptCategory(configKey) {
  const map = {
    platform_options: 'opt_platform',
    scene_list: 'opt_scene',
    light_options: 'opt_light',
    style_presets: 'opt_style',
    selling_options: 'opt_selling',
    selling_points: 'opt_selling',
    purpose_options: 'opt_purpose',
    purposes: 'opt_purpose',
    shadow_styles: 'opt_shadow',
    size_presets: 'opt_size',
    size_options: 'opt_size',
    output_sizes: 'opt_size',
    page_sizes: 'opt_size',
    language_options: 'opt_language',
    content_structure: 'opt_page',
    ratio_options: 'opt_size',
    line_styles: 'opt_line_style',
    templates: 'opt_size_template',
    unit_options: 'opt_unit',
    tools: 'opt_tool',
    quality_options: 'opt_quality',
    format_options: 'opt_format',
    gender_options: 'opt_gender',
    age_ranges: 'opt_age',
    hairstyles: 'opt_hairstyle',
    ethnicities: 'opt_ethnicity',
    poses: 'opt_pose',
    clothing_options: 'opt_clothing',
    scene_options: 'opt_scene',
    banner_types: 'opt_banner_type'
  }
  return map[configKey] || ''
}

// 由工作台 configGroup 反推提示词库 scope
function inferPromptScope(configGroup) {
  const map = {
    bg_generation: 'change_bg',
    white_bg: 'white_bg',
    ai_model: 'ai_model',
    main_image: 'main_image',
    retouch: 'retouch',
    detail_img: 'detail',
    size_mark: 'dimension',
    banner: 'banner',
    batch_process: 'batch'
  }
  return map[configGroup] || ''
}

async function loadPromptPicker() {
  const cat = promptPickerCategory.value
  const scope = promptPickerScope.value || undefined
  const key = `${cat}|${promptPickerScope.value || ''}`
  if (key === promptPickerLoadedKey.value && promptPickerItems.value.length) return
  promptPickerLoading.value = true
  try {
    // 先按 分类 + scope 精确查询
    let res = await listAdminPromptLibrary({
      pageNum: 1,
      pageSize: 500,
      category: cat || undefined,
      scope: scope,
      status: '0'
    })
    let rows = res.rows || []
    // 若精确查询无结果且有分类，回退为仅按分类查询（不限 scope），避免某些分类在该工作台下没有数据时下拉为空
    // 注意：分类为空时不回退，否则会返回所有选项导致下拉全列出来
    if (rows.length === 0 && cat && scope) {
      res = await listAdminPromptLibrary({
        pageNum: 1,
        pageSize: 500,
        category: cat || undefined,
        status: '0'
      })
      rows = res.rows || []
    }
    // 若分类为空且有 scope，仅返回该 scope 下的选项（不回退为全量）
    if (rows.length === 0 && !cat && scope) {
      // 已在上方按 scope 查询过了，不需要额外操作
    }
    promptPickerItems.value = rows
    promptPickerLoadedKey.value = key
    syncPickerSelection()
  } catch {
    promptPickerItems.value = []
  } finally {
    promptPickerLoading.value = false
  }
}

// 规范化字符串用于模糊比较：去除所有空格（含全角空格），转小写
function normalizeForMatch(s) {
  return String(s || '').replace(/\s+/g, '').toLowerCase()
}

// 已绑定的选项行回显选中态：若行 value 命中提示词库某个 promptKey，则下拉回显该显示名
// 若 value 无法精确匹配 promptKey，则回退通过 label 匹配提示词库的 label 来回显（支持模糊匹配，忽略空格差异）
function syncPickerSelection() {
  const keyMap = new Map(promptPickerItems.value.map(p => [p.promptKey, p]))
  // 精确 label 映射
  const labelMap = new Map(promptPickerItems.value.map(p => [p.label, p]))
  // 模糊 label 映射（去除空格后比较，解决 "800×800" vs "800 × 800" 的差异）
  const fuzzyLabelMap = new Map(promptPickerItems.value.map(p => [normalizeForMatch(p.label), p]))
  creationOptions.value.forEach(item => {
    if (item._pickerKey === undefined) item._pickerKey = ''
    // 1. 精确匹配 value → promptKey
    if (item.value && keyMap.has(item.value)) {
      item._pickerKey = item.value
    }
    // 2. 精确匹配 label
    else if (item.label && labelMap.has(item.label)) {
      const matched = labelMap.get(item.label)
      item._pickerKey = matched.promptKey
    }
    // 3. 模糊匹配 label（忽略空格差异）
    else if (item.label) {
      const fuzzy = normalizeForMatch(item.label)
      if (fuzzy && fuzzyLabelMap.has(fuzzy)) {
        const matched = fuzzyLabelMap.get(fuzzy)
        item._pickerKey = matched.promptKey
      } else if (!keyMap.has(item._pickerKey)) {
        item._pickerKey = ''
      }
    }
    // 4. 都不匹配，清空回显
    else if (!keyMap.has(item._pickerKey)) {
      item._pickerKey = ''
    }
  })
}

function onPickerCategoryManualChange() {
  promptPickerUserOverride.value = true
  promptPickerLoadedKey.value = ''
  loadPromptPicker()
}

function onConfigKeyInput() {
  if (promptPickerUserOverride.value) return
  const inferred = inferPromptCategory(creationForm.configKey)
  if (inferred !== promptPickerCategory.value) {
    promptPickerCategory.value = inferred
    promptPickerLoadedKey.value = ''
    loadPromptPicker()
  }
}

function onConfigGroupInput() {
  // 切换工作台时必须重置分类和用户覆盖标记，否则：
  // 1) promptPickerUserOverride=true 会阻止后续任何处理
  // 2) promptPickerCategory 保持旧值，导致用旧分类+新 scope 查询返回空
  promptPickerCategory.value = ''
  promptPickerUserOverride.value = false
  const inferred = inferPromptScope(creationForm.configGroup)
  if (inferred !== promptPickerScope.value) {
    promptPickerScope.value = inferred
    promptPickerLoadedKey.value = ''
    loadPromptPicker()
  }
}

// 选中某个提示词显示名：用其 label 作为选项显示名，promptKey 作为选项值
function onPickPromptItem(item, val) {
  if (!val) {
    item._pickerKey = ''
    return
  }
  const picked = promptPickerItems.value.find(p => p.promptKey === val)
  if (!picked) return
  item.label = picked.label
  item.value = picked.promptKey
  item._pickerKey = val
}

// 判断某提示词Key是否已被其他选项行选用（用于去重：已选过的不能再选）
function isPickerItemTaken(currentIdx, promptKey) {
  if (!promptKey) return false
  return creationOptions.value.some(
    (item, idx) => idx !== currentIdx && item._pickerKey === promptKey
  )
}

// 取某 promptKey 对应的提示词内容，用于在选项行下方展示
function getPickerPromptText(promptKey) {
  if (!promptKey) return ''
  const p = promptPickerItems.value.find(x => x.promptKey === promptKey)
  return p && p.promptText ? p.promptText : ''
}

const creationUploadUrl = '/api/common/upload'
const creationUploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + localStorage.getItem('gh_token')
}))

function beforeOptionImageUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) {
    ElMessage.error('只能上传图片文件，例如 JPG/PNG/WebP')
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB')
  }
  return isImage && isLt10M
}

function handleOptionImageSuccess(res, item) {
  const data = res.data || res
  if (res.code === 200 || res.code === 0) {
    const url = data.url || data.fileName || ''
    item.image = url ? normalizeImageUrl(url) : ''
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(res.msg || '图片上传失败')
  }
}

function handleOptionImageError() {
  ElMessage.error('图片上传请求失败，请检查网络或登录状态')
}

// 图片/阴影类配置（shadow_styles、bg_styles、preview_styles、style_presets）：
// 选项由提示词库下拉绑定，选中显示名即自动填好 label 与 value（英文Key），无需用户手动填写这两列，仅保留图片列。
// 尺寸类配置（size_presets/size_options/output_sizes/page_sizes）同理：选显示名即得值，
// 后面的"值/宽/高"等手动填写列无需用户填，由下拉自动填好。
// 仅当该配置键能反推出提示词库分类（即下拉有内容可绑）时才隐藏手动填写列，否则保留输入框作为兜底。
const imageOptionKeys = ['shadow_styles', 'bg_styles', 'preview_styles', 'style_presets']
const sizeOptionKeys = ['size_presets', 'size_options', 'output_sizes', 'page_sizes']
// 列表绑定类配置：选了显示名即得 label+value（及宽高/图片），无需用户手动填这些列。
// 只要该配置键能反推出提示词库分类（即下拉有内容可绑），就隐藏 label 和 value 列。
const pickerBoundOptionConfig = computed(() =>
  !!inferPromptCategory(creationForm.configKey)
  && !['max_generate_count', 'max_selling_count', 'size_min', 'size_max'].includes(creationForm.configKey)
)
// 绑定类配置一律隐藏 label/value/w/h 列，不保留"未匹配时回显"的逻辑
// （新增选项和已有选项都只通过提示词库下拉选择，不需要手动填写这两列）
const hideLabelValueColumns = computed(() => pickerBoundOptionConfig.value)
// 兼容旧名（图片/阴影类专用分支用），等价于 pickerBoundOptionConfig && 含图片列
const isImageOptionConfig = computed(() =>
  imageOptionKeys.includes(creationForm.configKey) && !!inferPromptCategory(creationForm.configKey)
)

const creationOptionFields = computed(() => {
  // 图片/阴影类配置：固定为 显示名/值/图片 三列。
  // label 与 value 由提示词库下拉自动填好（界面隐藏这两列），image 列保留供上传/填写图片。
  // 即使已存数据没有 image 字段，也强制补出该列，确保图片栏始终可见且参与序列化。
  if (isImageOptionConfig.value) {
    return [
      { key: 'label', label: '显示名', type: 'text' },
      { key: 'value', label: '值', type: 'text' },
      { key: 'image', label: '图片', type: 'image' }
    ]
  }
  // 尺寸类配置：固定为 显示名/值/宽/高 四列。label 与 value 由下拉自动填好（界面隐藏），
  // 宽/高也由下拉自动填好（界面隐藏），保留这几列仅为序列化兜底。
  if (sizeOptionKeys.includes(creationForm.configKey) && !!inferPromptCategory(creationForm.configKey)) {
    return [
      { key: 'label', label: '显示名', type: 'text' },
      { key: 'value', label: '值', type: 'text' },
      { key: 'w', label: '宽', type: 'number' },
      { key: 'h', label: '高', type: 'number' }
    ]
  }
  const samples = creationOptions.value.filter(i => i && typeof i === 'object')
  const knownOrder = ['label', 'value', 'name', 'desc', 'image', 'w', 'h']
  const hiddenKeys = new Set(['key']) // key 字段不作为可编辑列显示，但仍参与序列化
  const keySet = new Set()
  samples.forEach(item => Object.keys(item).forEach(k => {
    // 跳过内部字段（下划线前缀，如 _pickerKey）和隐藏字段（如 key），不作为可编辑列
    if (k && !k.startsWith('_') && !hiddenKeys.has(k)) keySet.add(k)
  }))
  if (keySet.size === 0) {
    return guessOptionFieldsByKey(creationForm.configKey)
  }
  const ordered = knownOrder.filter(k => keySet.has(k))
  const rest = Array.from(keySet).filter(k => !knownOrder.includes(k))
  return [...ordered, ...rest].map(k => ({
    key: k,
    label: fieldLabel(k),
    type: isNumberField(k) ? 'number' : (k === 'image' ? 'image' : 'text')
  }))
})

function guessOptionFieldsByKey(configKey) {
  const sizeKeys = ['size_presets', 'size_options', 'output_sizes', 'page_sizes', 'size_min']
  if (sizeKeys.includes(configKey)) {
    return [
      { key: 'label', label: '显示名', type: 'text' },
      { key: 'value', label: '值', type: 'text' },
      { key: 'w', label: '宽', type: 'number' },
      { key: 'h', label: '高', type: 'number' }
    ]
  }
  const imageKeys = ['shadow_styles', 'bg_styles', 'preview_styles', 'style_presets']
  if (imageKeys.includes(configKey)) {
    return [
      { key: 'label', label: '显示名', type: 'text' },
      { key: 'value', label: '值', type: 'text' },
      { key: 'image', label: '图片', type: 'image' }
    ]
  }
  return defaultOptionFields
}

function fieldLabel(key) {
  const map = { label: '显示名', value: '值', key: '键', name: '名称', desc: '描述', image: '图片', w: '宽', h: '高' }
  return map[key] || key
}

function isNumberField(key) {
  return ['w', 'h'].includes(key)
}

const defaultOptionFields = [
  { key: 'label', label: '显示名', type: 'text' },
  { key: 'value', label: '值', type: 'text' }
]

function activeOptionFields() {
  return creationOptionFields.value
}

function createEmptyOption() {
  const obj = {}
  activeOptionFields().forEach(f => {
    obj[f.key] = f.type === 'number' ? 0 : ''
  })
  obj._pickerKey = ''
  return obj
}

function addOption() {
  creationOptions.value.push(createEmptyOption())
}

function removeOption(idx) {
  creationOptions.value.splice(idx, 1)
}

function showStringInput() {
  creationStringInputVisible.value = true
  nextTick(() => creationStringInputRef.value?.focus())
}

function addStringItem() {
  const v = creationStringInputValue.value.trim()
  if (v && !creationStringList.value.includes(v)) {
    creationStringList.value.push(v)
  }
  creationStringInputVisible.value = false
  creationStringInputValue.value = ''
}

function parseCreationValue(raw) {
  if (!raw || typeof raw !== 'string' || !raw.trim()) {
    return { type: 'options', data: [] }
  }
  const text = raw.trim()
  if (text.startsWith('[') || text.startsWith('{')) {
    try {
      const parsed = JSON.parse(text)
      if (Array.isArray(parsed)) {
        if (parsed.length === 0) return { type: 'options', data: [] }
        if (parsed.every(i => typeof i === 'string')) {
          return { type: 'string_list', data: parsed }
        }
        if (parsed.every(i => i && typeof i === 'object')) {
          return { type: 'options', data: parsed }
        }
        return { type: 'json', data: text }
      }
      if (parsed && typeof parsed === 'object') {
        return { type: 'json', data: text }
      }
      return { type: 'string', data: String(parsed) }
    } catch {
      return { type: 'json', data: text }
    }
  }
  if (/^-?\d+(\.\d+)?$/.test(text)) {
    const n = Number(text)
    return { type: 'number', data: n }
  }
  return { type: 'string', data: text }
}

function loadCreationEditor(raw) {
  const result = parseCreationValue(raw)
  creationEditorType.value = result.type
  creationOptions.value = []
  creationStringList.value = []
  creationNumberValue.value = 0
  creationStringValue.value = ''
  creationJsonValue.value = ''
  if (result.type === 'options') {
    creationOptions.value = (result.data || []).map(item => {
      const base = createEmptyOption()
      return { ...base, ...item, _pickerKey: '' }
    })
    if (creationOptions.value.length === 0) {
      creationOptions.value.push(createEmptyOption())
    }
  } else if (result.type === 'string_list') {
    creationStringList.value = [...result.data]
  } else if (result.type === 'number') {
    creationNumberValue.value = result.data
  } else if (result.type === 'string') {
    creationStringValue.value = result.data
  } else if (result.type === 'json') {
    creationJsonValue.value = result.data
  }
  // 编辑回显：根据当前 configGroup / configKey 推断分类与 scope，并尝试回显已绑定项
  const inferredCat = inferPromptCategory(creationForm.configKey)
  // 校验推断出的分类是否在当前工作台的过滤列表中，不在则不设置（避免下拉显示不相关内容）
  const allowedCats = promptPickerCategoryOptions.value.map(o => o.value)
  promptPickerCategory.value = (inferredCat && allowedCats.includes(inferredCat)) ? inferredCat : ''
  promptPickerScope.value = inferPromptScope(creationForm.configGroup)
  promptPickerUserOverride.value = false
  promptPickerLoadedKey.value = ''
  if (creationEditorType.value === 'options') {
    loadPromptPicker()
  }
}

function serializeCreationEditor() {
  const type = creationEditorType.value
  if (type === 'options') {
    const fields = activeOptionFields()
    // 界面隐藏但仍需序列化保留的字段（如 Banner 的 key 字段）
    const hiddenSerializeKeys = ['key']
    const items = creationOptions.value
      .map(item => {
        const obj = {}
        fields.forEach(f => {
          const v = item[f.key]
          obj[f.key] = f.type === 'number' ? Number(v || 0) : (v ?? '')
        })
        // 保留界面隐藏但原有数据的字段
        hiddenSerializeKeys.forEach(hk => {
          if (item[hk] !== undefined && item[hk] !== '') obj[hk] = item[hk]
        })
        return obj
      })
      .filter(item => {
        const labelOk = item.label !== undefined && String(item.label).trim() !== ''
        const valueOk = item.value !== undefined && String(item.value).trim() !== ''
        const keyOk = item.key !== undefined && String(item.key).trim() !== ''
        const nameOk = item.name !== undefined && String(item.name).trim() !== ''
        return labelOk || valueOk || keyOk || nameOk
      })
    if (items.length === 0) return null
    return JSON.stringify(items)
  }
  // 注意：内部字段 _pickerKey 仅用于界面回显，不参与序列化（上述只取 fields 定义的字段，已天然排除）
  if (type === 'string_list') {
    const items = creationStringList.value.map(s => String(s).trim()).filter(Boolean)
    if (items.length === 0) return null
    return JSON.stringify(items)
  }
  if (type === 'number') {
    return String(creationNumberValue.value)
  }
  if (type === 'string') {
    const v = creationStringValue.value
    if (!v || !v.trim()) return null
    return v
  }
  if (type === 'json') {
    const raw = creationJsonValue.value.trim()
    if (!raw) return null
    try {
      return JSON.stringify(JSON.parse(raw))
    } catch {
      ElMessage.warning('JSON 格式不正确，请检查')
      return null
    }
  }
  return null
}

function onEditorTypeChange(newType) {
  const current = serializeCreationEditor()
  creationEditorType.value = newType
  creationOptions.value = []
  creationStringList.value = []
  creationNumberValue.value = 0
  creationStringValue.value = ''
  creationJsonValue.value = ''
  if (newType === 'options') {
    const parsed = parseCreationValue(current || '')
    if (parsed.type === 'options' && parsed.data.length) {
      creationOptions.value = parsed.data.map(item => ({ ...createEmptyOption(), ...item, _pickerKey: '' }))
    }
    if (creationOptions.value.length === 0) {
      creationOptions.value.push(createEmptyOption())
    }
    // 切到选项列表时，若有可绑定的分类则同步加载并回显
    const inferredCat = inferPromptCategory(creationForm.configKey)
    const allowedCats = promptPickerCategoryOptions.value.map(o => o.value)
    promptPickerCategory.value = (inferredCat && allowedCats.includes(inferredCat)) ? inferredCat : ''
    promptPickerScope.value = inferPromptScope(creationForm.configGroup)
    promptPickerUserOverride.value = false
    promptPickerLoadedKey.value = ''
    loadPromptPicker()
  } else if (newType === 'string_list') {
    const parsed = parseCreationValue(current || '')
    if (parsed.type === 'string_list') creationStringList.value = [...parsed.data]
    else if (parsed.type === 'options') creationStringList.value = parsed.data.map(i => i.label || i.value || '').filter(Boolean)
    else if (parsed.type === 'string') creationStringList.value = [parsed.data]
  } else if (newType === 'number') {
    const parsed = parseCreationValue(current || '')
    creationNumberValue.value = parsed.type === 'number' ? parsed.data : 0
  } else if (newType === 'string') {
    const parsed = parseCreationValue(current || '')
    creationStringValue.value = parsed.type === 'string' ? parsed.data : (current || '')
  } else if (newType === 'json') {
    creationJsonValue.value = current || ''
  }
}

const headerStyle = {
  background: '#f8fafc',
  color: '#64748b',
  fontSize: '12px'
}

function createDefaultConfigForm() {
  return { configId: null, configName: '', configKey: '', configValue: '', configType: 'N', remark: '' }
}

function createDefaultPromptForm() {
  return { id: null, module: '', name: '', content: '', isDefault: '0', sort: 0, status: '0' }
}

function createDefaultTagForm() {
  return { id: null, tagName: '', tagType: '', sort: 0, status: '0' }
}

function createDefaultCreationForm() {
  return { id: null, configGroup: '', configKey: '', configName: '', configValue: '', sort: 0, status: '0', remark: '' }
}

function resetConfigForm() {
  Object.assign(configForm, createDefaultConfigForm())
}

function resetPromptForm() {
  Object.assign(promptForm, createDefaultPromptForm())
}

function resetTagForm() {
  Object.assign(tagForm, createDefaultTagForm())
}

function resetCreationForm() {
  Object.assign(creationForm, createDefaultCreationForm())
}

function createDefaultLibraryForm() {
  return {
    id: null,
    category: '',
    promptKey: '',
    label: '',
    promptText: '',
    scope: '',
    priority: 100,
    isDefault: '0',
    sort: 0,
    status: '0',
    remark: ''
  }
}

function resetLibraryForm() {
  Object.assign(libraryForm, createDefaultLibraryForm())
}

async function fetchSystemConfigs() {
  configLoading.value = true
  try {
    const res = await listAdminSystemConfigs({ pageNum: 1, pageSize: 100 })
    configList.value = res.rows || []
  } catch (error) {
    ElMessage.error(error.message || '获取系统参数失败')
  } finally {
    configLoading.value = false
  }
}

async function fetchCreationConfigs() {
  creationLoading.value = true
  try {
    const res = await listAdminCreationConfigs({
      pageNum: creationPageNum.value,
      pageSize: creationPageSize.value,
      configGroup: creationFilters.configGroup || undefined,
      status: creationFilters.status || undefined
    })
    creationConfigList.value = res.rows || []
    creationTotal.value = res.total || 0

    // 创作配置变化后，重新收集引用关系
    await collectReferencedPromptKeys()
  } catch (error) {
    ElMessage.error(error.message || '获取创作配置失败')
  } finally {
    creationLoading.value = false
  }
}

async function fetchPromptTemplates() {
  promptLoading.value = true
  try {
    const res = await listAdminPromptTemplates({
      pageNum: 1,
      pageSize: 100,
      module: promptFilters.module || undefined,
      status: promptFilters.status || undefined
    })
    promptTemplateList.value = res.rows || []
  } catch (error) {
    ElMessage.error(error.message || '获取提示词模板失败')
  } finally {
    promptLoading.value = false
  }
}

async function fetchTags() {
  tagLoading.value = true
  try {
    const res = await listAdminTags({
      pageNum: 1,
      pageSize: 100,
      tagName: tagFilters.tagName || undefined,
      tagType: tagFilters.tagType || undefined,
      status: tagFilters.status || undefined
    })
    tagList.value = res.rows || []
  } catch (error) {
    ElMessage.error(error.message || '获取标签失败')
  } finally {
    tagLoading.value = false
  }
}

function openConfigDialog(row) {
  resetConfigForm()
  if (row) Object.assign(configForm, row)
  configDialogVisible.value = true
}

function openPromptDialog(row) {
  resetPromptForm()
  if (row) Object.assign(promptForm, row)
  promptDialogVisible.value = true
}

function openTagDialog(row) {
  resetTagForm()
  if (row) Object.assign(tagForm, row)
  tagDialogVisible.value = true
}

function openCreationDialog(row) {
  resetCreationForm()
  if (row) Object.assign(creationForm, row)
  // 新增时自动继承当前选中的工作台分组
  if (!row && creationFilters.configGroup) {
    creationForm.configGroup = creationFilters.configGroup
    // 手动触发工作台变更逻辑，加载对应的提示词库分类与 scope
    onConfigGroupInput()
  }
  loadCreationEditor(row ? row.configValue : '')
  creationDialogVisible.value = true
}

async function submitConfig() {
  savingConfig.value = true
  try {
    const payload = { ...configForm }
    if (payload.configId) {
      await updateAdminSystemConfig(payload)
    } else {
      await addAdminSystemConfig(payload)
    }
    ElMessage.success('参数保存成功')
    configDialogVisible.value = false
    fetchSystemConfigs()
  } catch (error) {
    ElMessage.error(error.message || '保存参数失败')
  } finally {
    savingConfig.value = false
  }
}

async function submitPrompt() {
  savingPrompt.value = true
  try {
    const payload = { ...promptForm }
    if (payload.id) {
      await updateAdminPromptTemplate(payload)
    } else {
      await addAdminPromptTemplate(payload)
    }
    ElMessage.success('提示词模板保存成功')
    promptDialogVisible.value = false
    fetchPromptTemplates()
  } catch (error) {
    ElMessage.error(error.message || '保存提示词模板失败')
  } finally {
    savingPrompt.value = false
  }
}

async function submitTag() {
  savingTag.value = true
  try {
    const payload = { ...tagForm }
    if (payload.id) {
      await updateAdminTag(payload)
    } else {
      await addAdminTag(payload)
    }
    ElMessage.success('标签保存成功')
    tagDialogVisible.value = false
    fetchTags()
  } catch (error) {
    ElMessage.error(error.message || '保存标签失败')
  } finally {
    savingTag.value = false
  }
}

async function submitCreation() {
  if (!creationForm.configGroup) {
    return ElMessage.warning('请输入配置分组')
  }
  if (!creationForm.configName) {
    return ElMessage.warning('请输入配置名称')
  }
  // 配置键已对用户隐藏：新增时若未填写则自动生成（数据库 config_key 非空，且与 config_group 联合唯一）
  if (!creationForm.configKey) {
    creationForm.configKey = 'cfg_' + Date.now()
  }
  const serialized = serializeCreationEditor()
  if (serialized === null) {
    return ElMessage.warning('请检查配置值：选项至少填写显示名和值，或切换为高级JSON模式')
  }
  // 数值类型配置（如生成数量上限）最小值为1，不允许设为0
  if (creationEditorType.value === 'number' && creationNumberValue.value < 1) {
    return ElMessage.warning('数值配置最小值为1，不允许设为0')
  }
  creationForm.configValue = serialized
  savingCreation.value = true
  try {
    const payload = { ...creationForm }
    if (payload.id) {
      await updateAdminCreationConfig(payload)
      ElMessage.success('创作配置修改成功')
    } else {
      await addAdminCreationConfig(payload)
      ElMessage.success('创作配置新增成功')
    }
    creationDialogVisible.value = false
    fetchCreationConfigs()
  } catch (error) {
    ElMessage.error(error.message || '保存创作配置失败')
  } finally {
    savingCreation.value = false
  }
}

async function setPromptDefault(row) {
  try {
    await setAdminPromptTemplateDefault(row.id)
    ElMessage.success('默认模板已更新')
    fetchPromptTemplates()
  } catch (error) {
    ElMessage.error(error.message || '设置默认模板失败')
  }
}

async function togglePromptStatus(row, enabled) {
  try {
    await updateAdminPromptTemplate({ ...row, status: enabled ? '0' : '1' })
    row.status = enabled ? '0' : '1'
    ElMessage.success('状态已更新')
  } catch (error) {
    ElMessage.error(error.message || '更新状态失败')
  }
}

async function handleDeletePromptTemplate(row) {
  try {
    await ElMessageBox.confirm('确认删除该提示词模板吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await delAdminPromptTemplate(row.id)
    ElMessage.success('删除成功')
    fetchPromptTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除提示词模板失败')
    }
  }
}

async function toggleCreationStatus(row, enabled) {
  try {
    await changeAdminCreationConfigStatus(row.id, enabled ? '0' : '1')
    row.status = enabled ? '0' : '1'
    ElMessage.success('创作配置状态已更新')
  } catch (error) {
    ElMessage.error(error.message || '更新创作配置状态失败')
  }
}

async function handleDeleteCreation(row) {
  try {
    await ElMessageBox.confirm('确认删除该创作配置吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await delAdminCreationConfig(row.id)
    ElMessage.success('删除成功')
    fetchCreationConfigs()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除创作配置失败')
    }
  }
}

async function handleDeleteConfig(row) {
  try {
    await ElMessageBox.confirm('确认删除该系统参数吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await delAdminSystemConfig(row.configId)
    ElMessage.success('删除成功')
    fetchSystemConfigs()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除系统参数失败')
    }
  }
}

async function handleDeleteTag(row) {
  try {
    await ElMessageBox.confirm('确认删除该标签吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await delAdminTag(row.id)
    ElMessage.success('删除成功')
    fetchTags()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除标签失败')
    }
  }
}

// 工作台选择联动：选择后清空分类（避免选中不属于新工作台的分类），重置分页并自动查询
function onLibraryScopeChange() {
  libraryFilters.category = ''
  libraryPageNum.value = 1
  fetchPromptLibrary()
}

async function fetchPromptLibrary() {
  libraryLoading.value = true
  try {
    // 先收集引用关系（用于引用状态筛选和标签显示）
    await collectReferencedPromptKeys()

    // 构建查询参数
    const queryParams = {
      pageNum: libraryPageNum.value,
      pageSize: libraryPageSize.value,
      category: libraryFilters.category || undefined,
      label: libraryFilters.label || undefined,
      promptKey: libraryFilters.promptKey || undefined,
      scope: libraryFilters.scope || undefined,
      status: libraryFilters.status || undefined
    }

    // 引用状态筛选：传给后端做精确查询，避免前端二次过滤与分页冲突
    if (libraryFilters.referenced) {
      queryParams.referenced = libraryFilters.referenced
      // 将被引用的 promptKey 列表传给后端
      if (referencedPromptKeys.value.size > 0) {
        queryParams.referencedKeys = Array.from(referencedPromptKeys.value).join(',')
      }
    }

    const res = await listAdminPromptLibrary(queryParams)
    promptLibraryList.value = res.rows || []
    libraryTotal.value = res.total || 0
  } catch (error) {
    ElMessage.error(error.message || '获取提示词选项库失败')
  } finally {
    libraryLoading.value = false
  }
}

// 收集所有被创作配置引用的 promptKeys
async function collectReferencedPromptKeys() {
  try {
    const res = await listAdminCreationConfigs({
      pageNum: 1,
      pageSize: 1000  // 获取所有创作配置
    })
    const configs = res.rows || []
    const referencedKeys = new Set()

    configs.forEach(config => {
      if (config.configValue) {
        try {
          const items = JSON.parse(config.configValue)
          if (Array.isArray(items)) {
            items.forEach(item => {
              if (item.value && typeof item.value === 'string') {
                referencedKeys.add(item.value)
              }
            })
          }
        } catch {
          // 忽略无法解析的配置
        }
      }
    })

    referencedPromptKeys.value = referencedKeys
  } catch (error) {
    console.error('收集引用关系失败:', error)
    referencedPromptKeys.value = new Set()
  }
}

function openLibraryDialog(row) {
  resetLibraryForm()
  if (row) Object.assign(libraryForm, row)
  // 同步 scope 字符串 → 多选数组（编辑回显）
  libraryScopeArray.value = String(libraryForm.scope || '')
    .split(',')
    .map(v => v.trim())
    .filter(Boolean)
  libraryDialogVisible.value = true
}

async function submitLibrary() {
  if (!libraryForm.category) {
    return ElMessage.warning('请选择分类')
  }
  if (!libraryForm.label) {
    return ElMessage.warning('请输入显示名')
  }
  if (!libraryScopeArray.value || libraryScopeArray.value.length === 0) {
    return ElMessage.warning('请选择适用功能')
  }
  // 适用功能多选 → 逗号分隔字符串（与后端 scope 字段约定一致）
  libraryForm.scope = libraryScopeArray.value.join(',')
  // promptKey 已对用户隐藏：新增时若为空则自动生成（数据库要求唯一标识）
  if (!libraryForm.promptKey) {
    libraryForm.promptKey = 'pk_' + Date.now()
  }
  savingLibrary.value = true
  try {
    const payload = { ...libraryForm }
    if (payload.id) {
      await updateAdminPromptLibrary(payload)
      ElMessage.success('提示词选项修改成功')
    } else {
      await addAdminPromptLibrary(payload)
      ElMessage.success('提示词选项新增成功')
    }
    libraryDialogVisible.value = false
    fetchPromptLibrary()
  } catch (error) {
    ElMessage.error(error.message || '保存提示词选项失败')
  } finally {
    savingLibrary.value = false
  }
}

async function toggleLibraryStatus(row, enabled) {
  try {
    await updateAdminPromptLibrary({ ...row, status: enabled ? '0' : '1' })
    row.status = enabled ? '0' : '1'
    ElMessage.success('状态已更新')
  } catch (error) {
    ElMessage.error(error.message || '更新状态失败')
  }
}

async function handleDeleteLibrary(row) {
  // 检查是否被创作配置引用
  if (referencedPromptKeys.value.has(row.promptKey)) {
    ElMessage.warning('该提示词选项已被创作配置引用，无法删除')
    return
  }

  try {
    await ElMessageBox.confirm('确认删除该提示词选项吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await removeAdminPromptLibrary(row.id)
    ElMessage.success('删除成功')
    fetchPromptLibrary()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除提示词选项失败')
    }
  }
}

onMounted(() => {
  fetchSystemConfigs()
  fetchCreationConfigs()
  fetchPromptTemplates()
  fetchPromptLibrary()
  fetchTags()
})
</script>

<style lang="scss" scoped>
.admin-system {
  display: flex;
  flex-direction: column;
}

.system-tabs {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.04);
  border: none;

  :deep(.el-tabs__header) {
    background: transparent;
    border-bottom-color: #f1f5f9;
  }

  :deep(.el-tabs__item) {
    font-size: 14px;
    font-weight: 500;
    border: none !important;

    &.is-active {
      color: var(--gh-primary);
      background: rgba(37, 99, 255, 0.06) !important;
    }
  }

  :deep(.el-tabs__content) {
    padding: 24px;
  }
}

.section-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.form-section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 6px;
}

.section-desc {
  margin: 0;
  font-size: 13px;
  color: #64748b;
}

.filter-inline {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.creation-group-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.creation-group-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #eff6ff;
    border-color: #2563ff;
    box-shadow: 0 2px 8px rgba(37, 99, 255, 0.1);
  }
}

.creation-group-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.creation-group-info {
  flex: 1;
  min-width: 0;
}

.creation-group-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 2px;
}

.creation-group-desc {
  font-size: 12px;
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.creation-group-count {
  font-size: 13px;
  color: #2563ff;
  font-weight: 600;
  flex-shrink: 0;
}

.prompt-sub-tabs {
  margin-bottom: 16px;

  :deep(.el-radio-button__inner) {
    font-weight: 500;
  }
}

:deep(.el-table) {
  --el-table-border-color: #f1f5f9;
  --el-table-header-bg-color: #f8fafc;
  --el-table-header-text-color: #64748b;
  border-radius: 8px;
  overflow: hidden;
}

@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: stretch;
  }
}

.option-editor {
  width: 100%;
}

.option-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.option-input {
  flex: 1;
  min-width: 120px;
}

.option-input-num {
  max-width: 110px;
  flex: 0 0 110px;
}

.option-image-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1 1 100%;
  min-width: 240px;
}

.option-image-input-wrap {
  display: flex;
  gap: 8px;
  align-items: center;
}

.option-image-uploader {
  flex-shrink: 0;

  :deep(.el-upload) {
    display: inline-flex;
  }
}

.option-image-preview {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.option-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #94a3b8;
}

/* 提示词库绑定工具条 */
.option-picker-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 10px;
  padding: 8px 10px;
  background: #f0f7ff;
  border: 1px dashed #93c5fd;
  border-radius: 8px;
}

.option-picker-bar-label {
  font-size: 12px;
  font-weight: 600;
  color: #1d4ed8;
  white-space: nowrap;
}

.option-picker-bar-hint {
  font-size: 12px;
  color: #64748b;
}

/* 行内"从提示词库选择"下拉 */
.option-picker {
  width: 200px;
  flex: 0 0 200px;
}

.picker-opt-label {
  float: left;
}

.picker-opt-taken {
  float: right;
  color: #f56c6c;
  font-size: 12px;
  font-weight: 500;
}

.picker-opt-key {
  float: right;
  color: #94a3b8;
  font-size: 12px;
}

/* 已绑定提示词内容预览 */
.option-prompt-hint {
  flex: 1 1 100%;
  margin: -2px 0 6px 0;
  padding: 6px 10px;
  font-size: 12px;
  color: #475569;
  background: #f8fafc;
  border-left: 3px solid #2563ff;
  border-radius: 4px;
  line-height: 1.5;
  word-break: break-all;
}

.tag-input-wrap {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.form-item-hint {
  margin-left: 10px;
  font-size: 12px;
  color: #94a3b8;
}

// ===== 表格分页 =====
.table-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
