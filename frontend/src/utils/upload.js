export const IMAGE_UPLOAD_MAX_BYTES = 5 * 1024 * 1024
export const IMAGE_UPLOAD_MAX_MB = 5

const ALLOWED_EXTENSIONS = ['.jpg', '.jpeg', '.png', '.gif', '.webp']

/** @returns {string|null} 错误信息；通过校验返回 null */
export function validateImageFile(file) {
  if (!file) return '请选择文件'

  const name = file.name || ''
  const dot = name.lastIndexOf('.')
  const ext = dot >= 0 ? name.slice(dot).toLowerCase() : ''
  if (!ALLOWED_EXTENSIONS.includes(ext)) {
    return '仅支持 JPG、PNG、GIF、WEBP 格式'
  }
  if (file.size > IMAGE_UPLOAD_MAX_BYTES) {
    return `图片大小不能超过 ${IMAGE_UPLOAD_MAX_MB}MB`
  }
  return null
}
