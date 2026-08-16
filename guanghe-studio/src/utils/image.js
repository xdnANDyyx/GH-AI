/**
 * 统一图片 URL 处理工具
 * 后端上传接口返回 /profile/xxx 格式路径
 * 前端需通过 /api 代理访问后端静态资源
 */

/**
 * 将后端返回的图片路径规范化为前端可访问的完整 URL
 * @param {string} url - 后端返回的图片路径
 * @returns {string} 前端可访问的完整 URL
 */
export function getImageUrl(url) {
  if (!url) return ''
  if (typeof url !== 'string') return ''

  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:') || url.startsWith('blob:')) {
    return url
  }

  if (url.startsWith('/api/profile/')) {
    return url
  }

  if (url.startsWith('/profile/')) {
    return '/api' + url
  }

  if (url.startsWith('/profile')) {
    return '/api' + url
  }

  return url
}

/**
 * 上传成功后规范化图片路径（用于存储到数据库）
 * 返回 /profile/xxx 格式（与后端一致）
 * @param {string} url - 后端上传接口返回的路径
 * @returns {string} 规范化后的路径
 */
export function normalizeImageUrl(url) {
  if (!url) return ''
  if (typeof url !== 'string') return ''

  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:') || url.startsWith('blob:')) {
    return url
  }

  if (url.startsWith('/profile/')) {
    return url
  }

  if (url.startsWith('/api/profile/')) {
    return url.replace('/api', '')
  }

  let cleanPath = url.replace(/^.*?uploadPath[\\/]?/, '')
  cleanPath = cleanPath.replace(/^\/+/, '')
  return '/profile/' + cleanPath
}

/**
 * 将图片 URL 转换为 File 对象
 * 用于跨页面图片接力（右键“放入白底生成背景 / 产品精修”）时重建可上传文件
 * @param {string} url - 图片 URL（支持 http/https/data:/同源相对路径）
 * @param {string} filename - 文件名（不含扩展名时按 blob 类型补全）
 * @returns {Promise<File>}
 */
export async function urlToFile(url, filename = 'image.png') {
  const res = await fetch(url)
  const blob = await res.blob()
  const ext = (blob.type?.split('/')[1] || 'png').split(';')[0]
  const baseName = filename.replace(/\.[^.]+$/, '')
  return new File([blob], `${baseName}.${ext}`, { type: blob.type || 'image/png' })
}