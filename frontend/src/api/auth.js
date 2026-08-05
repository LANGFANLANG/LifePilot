import { request } from './http'

export function fetchCaptcha() {
  return request('/api/auth/captcha')
}

export function login(payload) {
  return request('/api/auth/login', { method: 'POST', body: payload })
}

export function register(payload) {
  return request('/api/auth/register', { method: 'POST', body: payload })
}

export function logout() {
  return request('/api/auth/logout', { method: 'POST' })
}

export function fetchMe() {
  return request('/api/auth/me')
}
