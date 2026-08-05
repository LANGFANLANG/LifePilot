import { request } from './http'

export function getPlanPreview(id) {
  return request(`/api/plan-previews/${id}`)
}

export function confirmPlanPreview(id) {
  return request(`/api/plan-previews/${id}/confirm`, { method: 'POST' })
}

export function rejectPlanPreview(id) {
  return request(`/api/plan-previews/${id}/reject`, { method: 'POST' })
}
