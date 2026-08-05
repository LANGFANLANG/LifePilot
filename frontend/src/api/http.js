const TOKEN_KEY = 'lifepilot_token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

export class ApiError extends Error {
  constructor(message, code) {
    super(message)
    this.name = 'ApiError'
    this.code = code
  }
}

function handleUnauthorized() {
  setToken(null)
  if (!window.location.pathname.startsWith('/login')) {
    window.location.assign('/login')
  }
}

export async function request(path, { method = 'GET', body } = {}) {
  const options = { method, headers: {} }
  const token = getToken()
  if (token) {
    options.headers['Authorization'] = token
  }
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
  if (res.status === 401) {
    handleUnauthorized()
    throw new ApiError('登录已失效，请重新登录', 'UNAUTHORIZED')
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
