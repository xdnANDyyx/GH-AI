/**
 * 图片压缩工具
 */

/**
 * 将图片 File/Blob 压缩到指定大小以下
 * @param {File} file - 原文件
 * @param {number} maxSizeMB - 最大限制（MB）
 * @returns {Promise<File>} 压缩后的 File 对象
 */
export async function compressImage(file, maxSizeMB = 7) {
  const maxSizeBytes = maxSizeMB * 1024 * 1024
  if (file.size <= maxSizeBytes) return file

  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = (e) => {
      const img = new Image()
      img.src = e.target.result
      img.onload = () => {
        const canvas = document.createElement('canvas')
        let width = img.width
        let height = img.height

        // 简单压缩：如果图片太大，逐步缩小尺寸
        let quality = 0.9
        const ctx = canvas.getContext('2d')

        const draw = () => {
          canvas.width = width
          canvas.height = height
          ctx.clearRect(0, 0, width, height)
          ctx.drawImage(img, 0, 0, width, height)
          return canvas.toDataURL(file.type, quality)
        }

        let dataUrl = draw()
        
        // 简单循环压缩
        while (dataUrl.length > maxSizeBytes * 1.33 && quality > 0.1) {
          quality -= 0.1
          if (quality < 0.3) {
             width *= 0.9
             height *= 0.9
          }
          dataUrl = draw()
        }

        // 转回 Blob/File
        const arr = dataUrl.split(',')
        const mime = arr[0].match(/:(.*?);/)[1]
        const bstr = atob(arr[1])
        let n = bstr.length
        const u8arr = new Uint8Array(n)
        while (n--) {
          u8arr[n] = bstr.charCodeAt(n)
        }
        resolve(new File([u8arr], file.name, { type: mime }))
      }
    }
  })
}
