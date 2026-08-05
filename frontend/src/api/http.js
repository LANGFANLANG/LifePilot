export class ApiError extends Error {
  constructor(message, code) {
    super(message)
    this.name = 'ApiError'
    this.code = code
  }
}

export async function request(path, { method = 'GET', body } = {}) {
  const options = { method, headers: {} }
  if (body !== undefined) {
    options.headers['Content-Type'] = 'application/json'
    options.body = JSON.stringify(body)
  }
  let res
  try {
    res = await fetch(path, options)
  } catch {
    throw new ApiError('无法连接到服务，请确认后端已启动', 'NETWORK')
  }
  if (!res.ok) {
    throw new ApiError(`请求失败（HTTP ${res.status}）`, `HTTP_${res.status}`)
  }
  const payload = await res.json()
  if (!payload.success) {
    throw new ApiError(payload.message || '请求失败', payload.code || 'UNKNOWN')
  }
  return payload.data
}
