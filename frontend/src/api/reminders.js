import { request } from './http'

export function listRecentReminders() {
  return request('/api/reminders/recent')
}
