import { request } from './http'

export function draftDailyReview(date) {
  return request(`/api/reviews/daily/${date}/draft`, { method: 'POST' })
}

export function getDailyReview(date) {
  return request(`/api/reviews/daily/${date}`)
}

export function saveDailyReview(date, payload) {
  return request(`/api/reviews/daily/${date}`, { method: 'PUT', body: payload })
}
