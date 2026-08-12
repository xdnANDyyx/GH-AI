/**
 * 光合AI Studio — 客户端 API 服务模块
 * 所有接口均对应后端 /customer/** 控制器
 */
import api from './request'

// ======================== 登录认证 ========================

/** 获取图形验证码 */
export const getCaptchaImage = () =>
  api.get('/captchaImage')

/** 系统账号密码登录 */
export const loginByPassword = (username, password, code = '', uuid = '') =>
  api.post('/login', { username, password, code, uuid })

/** 系统短信验证码发送 */
export const sendSystemSmsCode = (phone) =>
  api.post('/login/sms/sendCode', { phone })

/** 系统短信登录 */
export const loginSystemBySms = (phone, code) =>
  api.post('/login/sms', { phone, code })

/** 账号注册 */
export const registerAccount = (data) =>
  api.post('/register', data)

/** 发送找回密码验证码 */
export const sendResetPasswordCode = (phone) =>
  api.post('/register/resetPassword/sendCode', { phone })

/** 找回密码重置 */
export const resetPassword = (data) =>
  api.post('/register/resetPassword', data)

/** 创建微信登录状态 */
export const createWechatLoginState = () =>
  api.post('/login/wechat/state')

/** 轮询微信登录状态 */
export const pollWechatLogin = (state) =>
  api.get('/login/wechat/poll', { params: { state } })

/** 开发态模拟微信扫码确认 */
export const mockConfirmWechatLogin = (state, unionId) =>
  api.post('/login/wechat/mockConfirm', null, { params: { state, unionId } })

/** 微信绑定手机号并登录 */
export const bindWechatLogin = (data) =>
  api.post('/login/wechat/bind', data)

/** 发送短信验证码 */
export const sendSmsCode = (phone) =>
  api.post('/customer/login/sms/sendCode', { phone })

/** 短信验证码登录 → 返回 { token } */
export const loginBySms = (phone, code, inviteCode) =>
  api.post('/customer/login/sms', { phone, code, inviteCode })

/** 获取当前登录用户信息 */
export const getInfo = () =>
  api.get('/customer/getInfo')

/** 获取当前管理员信息 */
export const getAdminInfo = () =>
  api.get('/getInfo')

/** 退出登录 */
export const logoutApi = () =>
  api.delete('/customer/logout')

// ======================== 文件上传 ========================

/** 上传单个文件 → 返回 { url, fileName, ... } */
export const uploadFile = (file, source) => {
  const formData = new FormData()
  formData.append('file', file)
  if (source) formData.append('source', source)
  return api.post('/customer/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  })
}

/** 批量上传文件 */
export const uploadFiles = (files, source) => {
  const formData = new FormData()
  files.forEach(f => formData.append('files', f))
  if (source) formData.append('source', source)
  return api.post('/customer/file/uploads', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 300000
  })
}

// ======================== AI 图片生成 ========================

/**
 * Omni Image 多图生成（核心接口）
 * @param {Object} params
 * @param {string} params.prompt - 正向提示词（必填）
 * @param {string} params.sessionType - 会话类型：white_bg / main_image / render / image_to_prompt
 * @param {number} [params.sessionId] - 会话ID（不传则自动创建）
 * @param {string} [params.chatContent] - 聊天内容
 * @param {string[]} [params.productImages] - 产品图URL列表（最多4张）
 * @param {string[]} [params.referenceImages] - 参考图URL列表（最多2张）
 * @param {string[]} [params.sceneImages] - 场景图URL列表（最多2张）
 * @param {string[]} [params.modelImages] - 模特图URL列表（最多2张）
 * @param {number[]} [params.materialIds] - 素材ID列表
 * @param {number} [params.n=1] - 生成数量（1-9）
 * @param {string} [params.type] - 记录类型（1:对话, 2:画布）
 * @param {string} [params.deductTypeCode] - 扣分类型编码
 * @param {Object} [params.extraOptions] - 扩展参数
 * @returns {Promise} → AiImageGenerateVO { id, sessionId, status, images, ... }
 */
export const generateOmniImage = (params) =>
  api.post('/customer/ai-image/generate/omni-image', params, {
    // AI 生图耗时可达 20~50 秒甚至更久（2K 大图），默认 30 秒超时会提前断开
    timeout: 300000
  })

/** 查询生成结果 */
export const queryGenerateResult = (recordId) =>
  api.get(`/customer/ai-image/result/${recordId}`)

/** 反向提示词（图生提示词） */
export const reversePrompt = (params) =>
  api.post('/customer/ai/image/prompt/reverse', params)

/** 测试 AI 模型是否可用 */
export const testAiModel = () =>
  api.get('/customer/ai/image/test')

// ======================== AI 配置与选项 ========================

/** 获取指定类型的选项配置 */
export const getImageOptions = (optionKey) =>
  api.get(`/customer/ai/image/options/${optionKey}`)

/** 批量获取选项配置 */
export const getImageOptionsBatch = (params) =>
  api.post('/customer/ai/image/options', params)

/** 获取像素配置列表 */
export const getPixelConfigs = () =>
  api.get('/customer/ai/image/pixel-configs')

/** 获取扣分类型列表 */
export const getDeductTypes = () =>
  api.get('/customer/ai/image/deduct-types')

/** 获取会话类型列表 */
export const getSessionTypes = () =>
  api.get('/customer/ai/image/session-types')

/** 获取会话类型的提示词信息 */
export const getSessionTypePromptInfo = (sessionType) =>
  api.get('/customer/ai/image/session-type/prompt-info', { params: { sessionType } })

// ======================== 会话管理 ========================

/** 创建会话 → 返回 sessionId */
export const createSession = (sessionType, prompt) =>
  api.post('/customer/ai/image/session', null, {
    params: { sessionType, prompt }
  })

/** 获取会话列表 */
export const listSessions = (pageNum = 1, pageSize = 10, sessionType) =>
  api.get('/customer/ai/image/sessions', {
    params: { pageNum, pageSize, sessionType }
  })

/** 获取会话详情 */
export const getSessionDetail = (sessionId) =>
  api.get(`/customer/ai/image/session/${encodeURIComponent(sessionId)}`)

/** 获取会话详情（备用） */
export const getSessionDetailAlt = (sessionId, recordType) =>
  api.get('/customer/ai/image/session', { params: { sessionId, recordType } })

/** 删除会话 */
export const deleteSession = (sessionId) =>
  api.delete(`/customer/ai/image/session/${sessionId}`)

/** 保存画布参数 */
export const saveCanvasParams = (sessionId, canvasParams) =>
  api.post('/customer/ai/image/session/canvas', { sessionId, canvasParams })

// ======================== AI 对话 ========================

/** AI 对话 */
export const aiDialogue = (params) =>
  api.post('/customer/ai/dialogue/chat', params)

// ======================== 积分系统 ========================

/** 获取剩余积分 */
export const getRemainingPoints = () =>
  api.get('/customer/points/remaining')

/** 获取积分统计 */
export const getPointsStats = () =>
  api.get('/customer/points/stats')

/** 获取积分套餐列表 */
export const getPointsPackages = (params) =>
  api.get('/customer/points/package/list', { params })

/** 购买积分套餐 → 返回 { paymentRecordId, ... } */
export const purchasePackage = (params) =>
  api.post('/customer/points/package/purchase', params)

/** 查询支付状态 */
export const queryPaymentStatus = (paymentRecordId) =>
  api.get(`/customer/points/payment/status/${paymentRecordId}`)

/** 获取购买记录 */
export const getPackageRecords = (pageNum = 1, pageSize = 10) =>
  api.get('/customer/points/package/records', { params: { pageNum, pageSize } })

/** 获取扣费记录 */
export const getDeductRecords = () =>
  api.get('/customer/points/deduct/records')

// ======================== 注册信息 ========================

/** 提交注册信息 */
export const submitRegisterInfo = (data) =>
  api.post('/customer/register/info/submit', data)

/** 获取用户注册信息 */
export const getRegisterInfo = () =>
  api.get('/customer/register/info/info')

// ======================== 问卷调查 ========================

/** 获取问卷调查状态 */
export const getSurveyStatus = () =>
  api.get('/customer/survey/status')

/** 提交问卷调查 */
export const submitSurvey = (data) =>
  api.post('/customer/survey/submit', data)

/** 获取用户问卷信息 */
export const getSurveyInfo = () =>
  api.get('/customer/survey/info')

// ======================== 用户资料 ========================

/** 更新个人资料 */
export const updateProfile = (params) =>
  api.put('/customer/profile', params)

/** 发送修改手机验证码（旧手机） */
export const sendChangePhoneCode = () =>
  api.post('/customer/phone/sendChangeCode')

/** 发送新手机验证码 */
export const sendNewPhoneCode = (newPhone) =>
  api.post('/customer/phone/sendNewPhoneCode', null, { params: { newPhone } })

/** 修改手机号 */
export const updatePhone = (params) =>
  api.put('/customer/phone', params)

// ======================== 资产管理 ========================

/** 获取资产列表 */
export const listAssets = (params) =>
  api.get('/customer/asset/list', { params })

/** 批量删除资产 */
export const batchDeleteAssets = (assetIds) =>
  api.delete('/customer/asset/batch', { data: assetIds })

// ======================== 素材广场 ========================

/** 获取素材类型列表 */
export const listMaterialTypes = (typeName) =>
  api.get('/customer/material/type/list', { params: { typeName } })

/** 素材广场列表 */
export const listMaterialPlaza = (params) =>
  api.post('/customer/material/plaza/list', params)

/** 我的上传列表 */
export const listMyUpload = (params) =>
  api.post('/customer/material/my/list', params)

/** 我的收藏列表 */
export const listMyFavorite = (params) =>
  api.post('/customer/material/favorite/list', params)

/** 上架素材到广场 */
export const uploadMaterial = (params) =>
  api.post('/customer/material/upload', params)

/** 修改素材上架状态 */
export const changeMaterialShelfStatus = (params) =>
  api.post('/customer/material/shelf/status', params)

/** 更新我的素材 */
export const updateMyMaterial = (params) =>
  api.put('/customer/material/my/update', params)

/** 收藏素材 */
export const favoriteMaterial = (params) =>
  api.post('/customer/material/favorite', params)

/** 取消收藏素材 */
export const cancelFavoriteMaterial = (params) =>
  api.post('/customer/material/favorite/cancel', params)

/** 素材下架历史 */
export const listShelfHistory = (params) =>
  api.post('/customer/material/shelf/history/list', params)

// ======================== 自定义提示词 ========================

/** 获取自定义提示词列表 */
export const listCustomPrompts = (params) =>
  api.post('/customer/custom/prompt/list', params)

/** 添加自定义提示词 */
export const addCustomPrompt = (params) =>
  api.post('/customer/custom/prompt/add', params)

/** 编辑自定义提示词 */
export const editCustomPrompt = (params) =>
  api.post('/customer/custom/prompt/edit', params)

/** 删除自定义提示词 */
export const removeCustomPrompt = (params) =>
  api.post('/customer/custom/prompt/remove', params)

// ======================== 系统提示词 ========================

/** 获取系统提示词列表 */
export const listSystemPrompts = (params) =>
  api.get('/customer/prompt/list', { params })

// ======================== 字典/配置 ========================

/** 批量获取字典数据 */
export const getDictDataBatch = (types) =>
  api.post('/customer/dict/data/batch', { types })

// ======================== CORS 账号 ========================

/** 删除 CORS 账号 */
export const deleteCorsAccount = () =>
  api.delete('/customer/corsAccount/delete')

// ======================== 管理端：订单中心 ========================

/** 获取支付订单列表 */
export const listAdminPayments = (params) =>
  api.get('/business/payment/list', { params })

/** 导出支付订单 */
export const exportAdminPayments = (params) =>
  api.get('/business/payment/export', { params, responseType: 'blob' })

/** 获取客户列表 */
export const listAdminCustomers = (params) =>
  api.get('/system/customer/list', { params })

/** 获取退款记录列表 */
export const listAdminRefunds = (params) =>
  api.get('/business/refund/list', { params })

/** 处理退款 */
export const processAdminRefund = (data) =>
  api.put('/business/refund/process', data)

// ======================== 管理端：系统设置 ========================

/** 获取积分套餐列表 */
export const listAdminPackages = (params) =>
  api.get('/business/package/list', { params })

/** 新增积分套餐 */
export const addAdminPackage = (data) =>
  api.post('/business/package', data)

/** 修改积分套餐 */
export const updateAdminPackage = (data) =>
  api.put('/business/package', data)

/** 修改积分套餐状态 */
export const changeAdminPackageStatus = (data) =>
  api.put('/business/package/changeStatus', data)

/** 删除积分套餐 */
export const deleteAdminPackage = (ids) =>
  api.delete(`/business/package/${ids}`)

/** 获取系统参数列表 */
export const listAdminSystemConfigs = (params) =>
  api.get('/system/config/list', { params })

/** 新增系统参数 */
export const addAdminSystemConfig = (data) =>
  api.post('/system/config', data)

/** 修改系统参数 */
export const updateAdminSystemConfig = (data) =>
  api.put('/system/config', data)

/** 删除系统参数 */
export const delAdminSystemConfig = (ids) =>
  api.delete(`/system/config/${ids}`)

/** 获取创作配置列表 */
export const listAdminCreationConfigs = (params) =>
  api.get('/business/creationConfig/list', { params })

/** 获取创作配置详情 */
export const getAdminCreationConfig = (id) =>
  api.get(`/business/creationConfig/${id}`)

/** 按分组获取创作配置 */
export const getAdminCreationConfigByGroup = (group) =>
  api.get(`/business/creationConfig/group/${group}`)

/** 新增创作配置 */
export const addAdminCreationConfig = (data) =>
  api.post('/business/creationConfig', data)

/** 修改创作配置 */
export const updateAdminCreationConfig = (data) =>
  api.put('/business/creationConfig', data)

/** 删除创作配置 */
export const delAdminCreationConfig = (ids) =>
  api.delete(`/business/creationConfig/${ids}`)

/** 更新创作配置状态 */
export const changeAdminCreationConfigStatus = (id, status) =>
  api.put(`/business/creationConfig/status/${id}/${status}`)

/** 公开接口：按分组获取创作配置（无需登录） */
export const getPublicCreationConfigByGroup = (group) =>
  api.get(`/business/creationConfig/public/group/${group}`)

/** 获取提示词模板列表 */
export const listAdminPromptTemplates = (params) =>
  api.get('/business/promptTemplate/list', { params })

/** 新增提示词模板 */
export const addAdminPromptTemplate = (data) =>
  api.post('/business/promptTemplate', data)

/** 修改提示词模板 */
export const updateAdminPromptTemplate = (data) =>
  api.put('/business/promptTemplate', data)

/** 设置默认提示词模板 */
export const setAdminPromptTemplateDefault = (id) =>
  api.put(`/business/promptTemplate/default/${id}`)

/** 删除提示词模板 */
export const delAdminPromptTemplate = (ids) =>
  api.delete(`/business/promptTemplate/${ids}`)

/** 获取标签列表 */
export const listAdminTags = (params) =>
  api.get('/business/tag/list', { params })

/** 新增标签 */
export const addAdminTag = (data) =>
  api.post('/business/tag', data)

/** 修改标签 */
export const updateAdminTag = (data) =>
  api.put('/business/tag', data)

/** 删除标签 */
export const delAdminTag = (ids) =>
  api.delete(`/business/tag/${ids}`)

// ======================== 管理端：积分管理 ========================

/** 获取积分明细列表 */
export const listAdminPointsRecords = (params) =>
  api.get('/business/points/list', { params })

/** 导出积分明细 */
export const exportAdminPointsRecords = (params) =>
  api.get('/business/points/export', { params, responseType: 'blob' })

/** 手动调整积分 */
export const adjustAdminPoints = (data) =>
  api.post('/business/points/adjust', data)

/** 批量调整积分 */
export const batchAdjustAdminPoints = (data) =>
  api.post('/business/points/batchAdjust', data)

/** 删除积分变动记录 */
export const deleteAdminPointsRecord = (ids) =>
  api.delete(`/business/points/${ids}`)

/** 前台获取已发布的官方素材列表 */
export const listOfficialMaterials = (params) =>
  api.get('/business/officialMaterial/public/list', { params })

/** 前台获取已发布的Banner列表 */
export const listPublicBanners = (params) =>
  api.get('/business/banner/public/list', { params })

/** 记录 Banner 点击（公开接口） */
export const recordBannerClick = (id) =>
  api.post(`/business/banner/click/${id}`)

// ======================== 提示词选项库（生图前列表选择） ========================

/** 按分类拉取启用中的提示词选项列表 */
export const listPromptLibrary = (category, scope) =>
  api.get('/customer/promptLibrary/list', { params: { category, scope } })

/** 批量按分类拉取提示词选项（返回按分类分组的Map） */
export const listPromptLibraryBatch = (categories, scope) =>
  api.get('/customer/promptLibrary/listBatch', { params: { categories, scope } })

// ---- 管理端：提示词选项库 ----

/** 管理端：查询提示词选项库列表 */
export const listAdminPromptLibrary = (params) =>
  api.get('/business/promptLibrary/list', { params })

/** 管理端：获取选项详情 */
export const getAdminPromptLibrary = (id) =>
  api.get(`/business/promptLibrary/${id}`)

/** 管理端：新增选项 */
export const addAdminPromptLibrary = (data) =>
  api.post('/business/promptLibrary', data)

/** 管理端：修改选项 */
export const updateAdminPromptLibrary = (data) =>
  api.put('/business/promptLibrary', data)

/** 管理端：删除选项 */
export const removeAdminPromptLibrary = (ids) =>
  api.delete(`/business/promptLibrary/${ids}`)

// ======================== AI模特（前台公开接口） ========================

/** 前台：获取已发布且已授权的AI模特列表（公开接口，无需登录） */
export const getPublicPublishedAiModels = () =>
  api.get('/business/aiModel/public/published')
