import { request } from './http'

export function getTodayPlan() {
  return request('/api/planning/today')
}
