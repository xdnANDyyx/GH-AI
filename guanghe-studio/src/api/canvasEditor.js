/**
 * 画布编辑相关API
 */
import request from '@/api/request'

/**
 * 图片扩图
 * @param {Object} params - { imageUrl, ratio, width, height }
 */
export function extendImage(params) {
  return request({
    url: '/ai/image/extend',
    method: 'post',
    data: params,
    timeout: 300000
  })
}

/**
 * 多角度生成
 * @param {Object} params - { imageUrl, count, type }
 */
export function generateMultiAngle(params) {
  return request({
    url: '/ai/image/multi-angle',
    method: 'post',
    data: params,
    timeout: 300000
  })
}

/**
 * 修改图片文字
 * @param {Object} params - { imageUrl, originalText, newText, font }
 */
export function editImageText(params) {
  return request({
    url: '/ai/image/edit-text',
    method: 'post',
    data: params,
    timeout: 300000
  })
}

/**
 * 局部重绘
 * @param {Object} params - { imageUrl, description, mask }
 */
export function partialRedraw(params) {
  return request({
    url: '/ai/image/partial-redraw',
    method: 'post',
    data: params,
    timeout: 300000
  })
}

/**
 * 图层炸开/检测
 * @param {Object} params - { imageUrl }
 */
export function detectLayers(params) {
  return request({
    url: '/ai/image/detect-layers',
    method: 'post',
    data: params,
    timeout: 300000
  })
}

/**
 * 下载图层文件
 * @param {Object} params - { imageUrl, layers, format }
 */
export function downloadLayers(params) {
  return request({
    url: '/ai/image/download-layers',
    method: 'post',
    data: params,
    timeout: 300000
  })
}

/**
 * OCR识别图片文字
 * @param {String} imageUrl - 图片URL
 */
export function recognizeText(imageUrl) {
  return request({
    url: '/ai/image/ocr',
    method: 'post',
    data: { imageUrl }
  })
}

/**
 * 查询图片编辑历史
 * @param {String} imageId - 图片ID
 */
export function getEditHistory(imageId) {
  return request({
    url: `/ai/image/${imageId}/history`,
    method: 'get'
  })
}

/**
 * 撤销图片编辑
 * @param {String} imageId - 图片ID
 * @param {Number} version - 版本号
 */
export function revertImage(imageId, version) {
  return request({
    url: '/ai/image/revert',
    method: 'post',
    data: { imageId, version }
  })
}
