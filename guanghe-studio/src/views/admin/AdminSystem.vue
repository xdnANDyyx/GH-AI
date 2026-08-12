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
          <el-table-column prop="configKey" label="参数键名" min-width="220" show-overflow-tooltip />
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
            <el-button type="primary" @click="openCreationDialog()">新增配置</el-button>
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
          <el-table-column prop="configKey" label="配置键" min-width="200" show-overflow-tooltip />
          <el-table-column prop="configValue" label="配置值" min-width="280" show-overflow-tooltip />
          <el-table-column prop="sort" label="排序" width="90" />
          <el-table-column prop="status" label="状态" width="110">
            <template #default="{ row }">
              <el-switch :model-value="row.status === '0'" @change="(value) => toggleCreationStatus(row, value)" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openCreationDialog(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDeleteCreation(row)">删除</el-button>
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

              <el-select v-model="libraryFilters.category" placeholder="分类" clearable filterable style="width: 160px">
                <el-option v-for="c in libraryCategoryOptions" :key="c.value" :label="c.label" :value="c.value" />
              </el-select>
              <el-input v-model="libraryFilters.label" placeholder="显示名" clearable style="width: 160px" />
              <!-- <el-input v-model="libraryFilters.promptKey" placeholder="Key" clearable style="width: 180px" /> -->
              <el-select v-model="libraryFilters.status" placeholder="状态" clearable style="width: 120px">
                <el-option label="全部" value="" />
                <el-option label="启用" value="0" />
                <el-option label="停用" value="1" />
              </el-select>
              <el-button @click="fetchPromptLibrary">查询</el-button>
              <el-button type="primary" @click="openLibraryDialog()">新增选项</el-button>
              <el-button @click="openAbTestDialog()">A/B对比</el-button>
              <el-button @click="openAutoRecommendDialog()">自动推荐</el-button>
            </div>
          </div>

          <el-table v-loading="libraryLoading" :data="promptLibraryList" :header-cell-style="headerStyle">
            <el-table-column prop="category" label="分类" width="120" />
            <el-table-column prop="promptKey" label="Key" min-width="180" show-overflow-tooltip />
            <el-table-column prop="label" label="显示名" min-width="140" show-overflow-tooltip />
            <el-table-column prop="promptText" label="提示词内容" min-width="260" show-overflow-tooltip />
            <el-table-column prop="scope" label="适用功能" width="150" show-overflow-tooltip />
            <el-table-column prop="priority" label="优先级" width="90" />
            <el-table-column prop="sort" label="排序" width="80" />
            <el-table-column prop="version" label="版本" width="90">
              <template #default="{ row }">
                <el-tag v-if="row.version" size="small" type="info">{{ row.version }}</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="abGroup" label="A/B组" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.abGroup" size="small" type="success">{{ row.abGroup }}</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="successRate" label="成功率" width="90">
              <template #default="{ row }">
                <span v-if="row.successRate != null" :style="{ color: row.successRate >= 70 ? '#22C55E' : row.successRate >= 40 ? '#F59E0B' : '#EF4444', fontWeight: 600 }">{{ row.successRate }}%</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-switch :model-value="row.status === '0'" @change="(value) => toggleLibraryStatus(row, value)" />
              </template>
            </el-table-column>
            <el-table-column prop="isDefault" label="默认" width="80">
              <template #default="{ row }">
                <el-tag :type="row.isDefault === '1' ? 'warning' : 'info'" size="small">{{ row.isDefault === '1' ? '默认' : '普通' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="openLibraryDialog(row)">编辑</el-button>
                <el-button link type="success" size="small" @click="openFeedbackDialog(row)">反馈</el-button>
                <el-button link type="danger" size="small" @click="handleDeleteLibrary(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

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
            <el-option v-for="c in libraryCategoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="Key" required>
          <el-input v-model="libraryForm.promptKey" placeholder="点分式唯一标识，如 product.sofa" />
        </el-form-item>
        <el-form-item label="显示名" required>
          <el-input v-model="libraryForm.label" placeholder="前端显示名称" />
        </el-form-item>
        <el-form-item label="提示词内容">
          <el-input v-model="libraryForm.promptText" type="textarea" :rows="5" placeholder="提示词内容（正向约束/负向约束/参数描述）" />
        </el-form-item>
        <el-form-item label="适用功能">
          <el-input v-model="libraryForm.scope" placeholder="逗号分隔，如 white_bg,change_bg；留空表示所有功能" />
        </el-form-item>
        <el-form-item label="适配模型">
          <el-select v-model="libraryForm.model" style="width: 160px">
            <el-option label="全部" value="all" />
            <el-option label="GPT" value="gpt" />
            <el-option label="Gemini" value="gemini" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="libraryForm.priority" :min="0" />
        </el-form-item>
        <el-form-item label="默认">
          <el-select v-model="libraryForm.isDefault" style="width: 160px">
            <el-option label="否" value="0" />
            <el-option label="是" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="libraryForm.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="libraryForm.status" style="width: 160px">
            <el-option label="启用" value="0" />
            <el-option label="停用" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本">
          <el-input v-model="libraryForm.version" placeholder="如 1.0.0" />
        </el-form-item>
        <el-form-item label="A/B测试组">
          <el-select v-model="libraryForm.abGroup" style="width: 160px" placeholder="选择测试组">
            <el-option label="无" value="" />
            <el-option label="A组" value="A" />
            <el-option label="B组" value="B" />
          </el-select>
        </el-form-item>
        <el-form-item label="成功率(%)">
          <el-input-number v-model="libraryForm.successRate" :min="0" :max="100" :precision="2" :step="0.1" />
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
          <el-select v-model="creationForm.configGroup" placeholder="请选择工作台" filterable style="width: 100%">
            <el-option v-for="g in creationGroupOptions" :key="g.value" :label="g.label" :value="g.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置名称" required>
          <el-input v-model="creationForm.configName" placeholder="请输入配置名称，如：输出尺寸预设" />
        </el-form-item>
        <el-form-item label="配置键" required>
          <el-input v-model="creationForm.configKey" placeholder="请输入配置键（英文唯一标识），如：size_presets" />
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
            <div v-for="(item, idx) in creationOptions" :key="idx" class="option-row">
              <template v-for="field in creationOptionFields" :key="field.key">
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
              <el-button link type="danger" size="small" @click="removeOption(idx)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button type="primary" plain size="small" @click="addOption">
              <el-icon><Plus /></el-icon>添加选项
            </el-button>
            <div class="option-tip">每项至少填写"显示名"和"值"。尺寸类配置可填宽/高；图片字段可填公网链接或上传到服务器。</div>
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
          <el-input-number v-model="creationNumberValue" :controls="true" style="width: 220px" />
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

    <!-- 效果反馈弹窗 -->
    <el-dialog v-model="feedbackDialogVisible" title="效果反馈录入" width="520px">
      <el-form :model="feedbackForm" label-width="120px">
        <el-form-item label="提示词">{{ feedbackForm.label }}</el-form-item>
        <el-form-item label="分类">{{ feedbackForm.category }}</el-form-item>
        <el-form-item label="A/B测试组">
          <el-select v-model="feedbackForm.abGroup" style="width: 160px" placeholder="选择测试组">
            <el-option label="无" value="" />
            <el-option label="A组" value="A" />
            <el-option label="B组" value="B" />
          </el-select>
        </el-form-item>
        <el-form-item label="成功率(%)">
          <el-input-number v-model="feedbackForm.successRate" :min="0" :max="100" :precision="2" :step="0.1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="版本号">
          <el-input v-model="feedbackForm.version" placeholder="如 1.0.0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feedbackDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingFeedback" @click="submitFeedback">保存</el-button>
      </template>
    </el-dialog>

    <!-- A/B对比弹窗 -->
    <el-dialog v-model="abTestDialogVisible" title="A/B测试对比" width="800px">
      <el-table :data="abTestData" :header-cell-style="headerStyle" max-height="500">
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="label" label="提示词" min-width="160" />
        <el-table-column prop="abGroup" label="组别" width="80">
          <template #default="{ row }">
            <el-tag :type="row.abGroup === 'A' ? 'primary' : 'success'" size="small">{{ row.abGroup }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="successRate" label="成功率" width="100">
          <template #default="{ row }">
            <span v-if="row.successRate != null" :style="{ color: row.successRate >= 70 ? '#22C55E' : row.successRate >= 40 ? '#F59E0B' : '#EF4444', fontWeight: 600 }">{{ row.successRate }}%</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="90" />
        <el-table-column label="推荐" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.successRate != null && row.successRate >= 70" type="success" size="small">推荐</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div class="ab-test-summary" v-if="abTestData.length">
        <p><strong>A组平均成功率：</strong>{{ getAbAvgRate('A') }}%</p>
        <p><strong>B组平均成功率：</strong>{{ getAbAvgRate('B') }}%</p>
        <p><strong>最优组：</strong>{{ getAbAvgRate('A') >= getAbAvgRate('B') ? 'A组' : 'B组' }}</p>
      </div>
    </el-dialog>

    <!-- 自动推荐弹窗 -->
    <el-dialog v-model="autoRecommendDialogVisible" title="自动推荐（按成功率排序）" width="800px">
      <p class="section-desc" style="margin-bottom: 12px;">系统根据历史效果反馈数据，自动推荐成功率最高的提示词选项。</p>
      <el-table :data="autoRecommendData" :header-cell-style="headerStyle" max-height="500">
        <el-table-column label="排名" width="70" type="index" :index="1" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="label" label="提示词" min-width="160" />
        <el-table-column prop="successRate" label="成功率" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.successRate >= 70 ? '#22C55E' : row.successRate >= 40 ? '#F59E0B' : '#EF4444', fontWeight: 600 }">{{ row.successRate }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="90" />
        <el-table-column prop="abGroup" label="A/B组" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.abGroup" size="small" type="success">{{ row.abGroup }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openLibraryDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
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

const creationFilters = reactive({ configGroup: '', status: '' })
const promptFilters = reactive({ module: '', status: '' })
const tagFilters = reactive({ tagName: '', tagType: '', status: '' })

const promptSubTab = ref('template')
const libraryLoading = ref(false)
const savingLibrary = ref(false)
const libraryDialogVisible = ref(false)
const promptLibraryList = ref([])
const libraryFilters = reactive({ category: '', label: '', promptKey: '', status: '' })
const libraryCategoryOptions = [
  { value: 'function', label: '功能' },
  { value: 'platform', label: '平台' },
  { value: 'product', label: '产品类别' },
  { value: 'material', label: '材质' },
  { value: 'scene', label: '场景' },
  { value: 'style', label: '风格' },
  { value: 'selling', label: '卖点' },
  { value: 'size', label: '尺寸/输出' },
  { value: 'quality', label: '质量约束' },
  { value: 'negative', label: '负向约束' },
  { value: 'camera', label: '镜头/角度/占比' },
  { value: 'option', label: '功能内选项' },
  { value: 'model_adapt', label: '模型适配' }
]

const creationGroupOptions = [
  { value: 'common', label: '通用配置', desc: '语言列表、生成数量上限等', icon: '⚙' },
  { value: 'white_bg', label: 'AI白底图', desc: '阴影样式、输出尺寸', icon: '🖼' },
  { value: 'bg_generation', label: '白底图生成背景', desc: '平台、场景、光线、风格', icon: '🌅' },
  { value: 'retouch', label: '产品精修', desc: '精修工具、画质、格式', icon: '✨' },
  { value: 'ai_model', label: 'AI模特', desc: '性别、年龄、发型、姿势', icon: '👤' },
  { value: 'main_image', label: '主图设计', desc: '平台、画布尺寸、用途', icon: '📸' },
  { value: 'detail_img', label: '详情图/A+', desc: '页面尺寸、卖点、结构', icon: '📄' },
  { value: 'banner', label: 'Banner设计', desc: '画布尺寸、类型、目的', icon: '🎯' },
  { value: 'size_mark', label: '尺寸标记', desc: '线条样式、输出比例', icon: '📏' },
  { value: 'batch_process', label: '批量生成', desc: '卖点、输出设置', icon: '⚡' }
]

function getGroupLabel(value) {
  const g = creationGroupOptions.find(o => o.value === value)
  return g ? g.label : value
}

function getGroupCount(group) {
  return creationConfigList.value.filter(c => c.configGroup === group).length
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

const configForm = reactive(createDefaultConfigForm())
const promptForm = reactive(createDefaultPromptForm())
const tagForm = reactive(createDefaultTagForm())
const creationForm = reactive(createDefaultCreationForm())
const libraryForm = reactive(createDefaultLibraryForm())

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

const creationOptionFields = computed(() => {
  const samples = creationOptions.value.filter(i => i && typeof i === 'object')
  const knownOrder = ['label', 'value', 'key', 'name', 'desc', 'image', 'w', 'h']
  const keySet = new Set()
  samples.forEach(item => Object.keys(item).forEach(k => { if (k) keySet.add(k) }))
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
      return { ...base, ...item }
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
}

function serializeCreationEditor() {
  const type = creationEditorType.value
  if (type === 'options') {
    const fields = activeOptionFields()
    const items = creationOptions.value
      .map(item => {
        const obj = {}
        fields.forEach(f => {
          const v = item[f.key]
          obj[f.key] = f.type === 'number' ? Number(v || 0) : (v ?? '')
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
      creationOptions.value = parsed.data.map(item => ({ ...createEmptyOption(), ...item }))
    }
    if (creationOptions.value.length === 0) {
      creationOptions.value.push(createEmptyOption())
    }
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
    model: 'all',
    priority: 100,
    isDefault: '0',
    sort: 0,
    status: '0',
    version: '1.0.0',
    abGroup: '',
    successRate: null,
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
      pageNum: 1,
      pageSize: 100,
      configGroup: creationFilters.configGroup || undefined,
      status: creationFilters.status || undefined
    })
    creationConfigList.value = res.rows || []
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
  if (!creationForm.configKey) {
    return ElMessage.warning('请输入配置键')
  }
  const serialized = serializeCreationEditor()
  if (serialized === null) {
    return ElMessage.warning('请检查配置值：选项至少填写显示名和值，或切换为高级JSON模式')
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

async function fetchPromptLibrary() {
  libraryLoading.value = true
  try {
    const res = await listAdminPromptLibrary({
      pageNum: 1,
      pageSize: 200,
      category: libraryFilters.category || undefined,
      label: libraryFilters.label || undefined,
      promptKey: libraryFilters.promptKey || undefined,
      status: libraryFilters.status || undefined
    })
    promptLibraryList.value = res.rows || []
  } catch (error) {
    ElMessage.error(error.message || '获取提示词选项库失败')
  } finally {
    libraryLoading.value = false
  }
}

function openLibraryDialog(row) {
  resetLibraryForm()
  if (row) Object.assign(libraryForm, row)
  libraryDialogVisible.value = true
}

async function submitLibrary() {
  if (!libraryForm.category) {
    return ElMessage.warning('请选择分类')
  }
  if (!libraryForm.promptKey) {
    return ElMessage.warning('请输入Key')
  }
  if (!libraryForm.label) {
    return ElMessage.warning('请输入显示名')
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

// ========== 效果反馈 ==========
const feedbackDialogVisible = ref(false)
const savingFeedback = ref(false)
const feedbackForm = reactive({ id: null, label: '', category: '', abGroup: '', successRate: null, version: '' })

function openFeedbackDialog(row) {
  Object.assign(feedbackForm, {
    id: row.id,
    label: row.label,
    category: row.category,
    abGroup: row.abGroup || '',
    successRate: row.successRate ?? null,
    version: row.version || ''
  })
  feedbackDialogVisible.value = true
}

async function submitFeedback() {
  savingFeedback.value = true
  try {
    const row = promptLibraryList.value.find(r => r.id === feedbackForm.id)
    if (row) {
      await updateAdminPromptLibrary({
        ...row,
        abGroup: feedbackForm.abGroup,
        successRate: feedbackForm.successRate,
        version: feedbackForm.version
      })
      ElMessage.success('效果反馈已保存')
      feedbackDialogVisible.value = false
      fetchPromptLibrary()
    }
  } catch (error) {
    ElMessage.error(error.message || '保存效果反馈失败')
  } finally {
    savingFeedback.value = false
  }
}

// ========== A/B 测试对比 ==========
const abTestDialogVisible = ref(false)
const abTestData = ref([])

function openAbTestDialog() {
  abTestData.value = promptLibraryList.value
    .filter(r => r.abGroup === 'A' || r.abGroup === 'B')
    .sort((a, b) => {
      if (a.category !== b.category) return a.category.localeCompare(b.category)
      if (a.abGroup !== b.abGroup) return a.abGroup.localeCompare(b.abGroup)
      return (b.successRate ?? 0) - (a.successRate ?? 0)
    })
  if (!abTestData.value.length) {
    ElMessage.info('暂无A/B测试数据，请先在提示词选项中设置A/B组')
    return
  }
  abTestDialogVisible.value = true
}

function getAbAvgRate(group) {
  const items = abTestData.value.filter(r => r.abGroup === group && r.successRate != null)
  if (!items.length) return '0.00'
  const sum = items.reduce((acc, r) => acc + r.successRate, 0)
  return (sum / items.length).toFixed(2)
}

// ========== 自动推荐 ==========
const autoRecommendDialogVisible = ref(false)
const autoRecommendData = ref([])

function openAutoRecommendDialog() {
  autoRecommendData.value = promptLibraryList.value
    .filter(r => r.successRate != null && r.status === '0')
    .sort((a, b) => (b.successRate ?? 0) - (a.successRate ?? 0))
    .slice(0, 20)
  if (!autoRecommendData.value.length) {
    ElMessage.info('暂无效果反馈数据，请先录入成功率')
    return
  }
  autoRecommendDialogVisible.value = true
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

.ab-test-summary {
  margin-top: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;

  p {
    margin: 4px 0;
    font-size: 14px;
    color: #1e293b;
  }
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
</style>
