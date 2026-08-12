import request from './request'

// ==================== Banner 管理 ====================

// 获取 Banner 列表
export function getBannerList(params) {
  return request({
    url: '/business/banner/list',
    method: 'get',
    params
  })
}

// 获取 Banner 详情
export function getBannerDetail(id) {
  return request({
    url: `/business/banner/${id}`,
    method: 'get'
  })
}

// 新增 Banner
export function addBanner(data) {
  return request({
    url: '/business/banner',
    method: 'post',
    data
  })
}

// 更新 Banner
export function updateBanner(data) {
  return request({
    url: '/business/banner',
    method: 'put',
    data
  })
}

// 删除 Banner
export function deleteBanner(ids) {
  return request({
    url: `/business/banner/${ids}`,
    method: 'delete'
  })
}

// 更改 Banner 状态
export function changeBannerStatus(id, status) {
  return request({
    url: `/business/banner/status/${id}/${status}`,
    method: 'put'
  })
}

// 获取 Banner 点击趋势
export function getBannerClickTrend(id, params) {
  return request({
    url: `/business/banner/clickTrend/${id}`,
    method: 'get',
    params
  })
}

// 记录 Banner 点击
export function recordBannerClick(id) {
  return request({
    url: `/business/banner/click/${id}`,
    method: 'post'
  })
}

// ==================== 官方素材管理 ====================

// 获取素材列表
export function getMaterialList(params) {
  return request({
    url: '/business/officialMaterial/list',
    method: 'get',
    params
  })
}

// 获取素材详情
export function getMaterialDetail(id) {
  return request({
    url: `/business/officialMaterial/${id}`,
    method: 'get'
  })
}

// 新增素材
export function addMaterial(data) {
  return request({
    url: '/business/officialMaterial',
    method: 'post',
    data
  })
}

// 更新素材
export function updateMaterial(data) {
  return request({
    url: '/business/officialMaterial',
    method: 'put',
    data
  })
}

// 删除素材
export function deleteMaterial(ids) {
  return request({
    url: `/business/officialMaterial/${ids}`,
    method: 'delete'
  })
}

// 更改素材状态
export function changeMaterialStatus(id, status) {
  return request({
    url: `/business/officialMaterial/status/${id}/${status}`,
    method: 'put'
  })
}

// 批量更改素材状态
export function batchChangeMaterialStatus(data) {
  return request({
    url: '/business/officialMaterial/batchStatus',
    method: 'put',
    data
  })
}

// ==================== 标签管理 ====================

// 获取标签列表
export function getTagList(params) {
  return request({
    url: '/business/tag/list',
    method: 'get',
    params
  })
}

// 新增标签
export function addTag(data) {
  return request({
    url: '/business/tag',
    method: 'post',
    data
  })
}

// 更新标签
export function updateTag(data) {
  return request({
    url: '/business/tag',
    method: 'put',
    data
  })
}

// 删除标签
export function deleteTag(ids) {
  return request({
    url: `/business/tag/${ids}`,
    method: 'delete'
  })
}

// ==================== AI模特管理 ====================

// 获取 AI 模特列表
export function getAiModelList(params) {
  return request({
    url: '/business/aiModel/list',
    method: 'get',
    params
  })
}

// 获取 AI 模特详情
export function getAiModelDetail(id) {
  return request({
    url: `/business/aiModel/${id}`,
    method: 'get'
  })
}

// 新增 AI 模特
export function addAiModel(data) {
  return request({
    url: '/business/aiModel',
    method: 'post',
    data
  })
}

// 更新 AI 模特
export function updateAiModel(data) {
  return request({
    url: '/business/aiModel',
    method: 'put',
    data
  })
}

// 删除 AI 模特
export function deleteAiModel(ids) {
  return request({
    url: `/business/aiModel/${ids}`,
    method: 'delete'
  })
}

// 更改 AI 模特状态（上架/下架）
export function changeAiModelStatus(id, status) {
  return request({
    url: `/business/aiModel/status/${id}/${status}`,
    method: 'put'
  })
}

// 获取已上架的 AI 模特列表（供前台选择）
export function getPublishedAiModels(params) {
  return request({
    url: '/business/aiModel/published',
    method: 'get',
    params
  })
}

// ==================== 功能开关管理 ====================

// 获取功能开关列表
export function getFeatureToggleList(params) {
  return request({
    url: '/business/featureToggle/list',
    method: 'get',
    params
  })
}

// 获取所有启用的功能开关（供前台调用）
export function getEnabledToggles() {
  return request({
    url: '/business/featureToggle/enabled',
    method: 'get'
  })
}

// 公开接口：获取所有启用的功能开关（无需登录）
export function getPublicEnabledToggles() {
  return request({
    url: '/business/featureToggle/public/enabled',
    method: 'get'
  })
}

// 公开接口：获取所有功能开关（含关闭的，无需登录）
export function getPublicAllToggles() {
  return request({
    url: '/business/featureToggle/public/list',
    method: 'get'
  })
}

// 批量更新开关状态
export function updateToggleStatusBatch(data) {
  return request({
    url: '/business/featureToggle/batchStatus',
    method: 'put',
    data
  })
}

// ==================== 收藏管理 ====================

// 切换素材收藏状态
export function toggleMaterialFavorite(materialId) {
  return request({
    url: `/customer/material-favorite/toggle/${materialId}`,
    method: 'post'
  })
}
