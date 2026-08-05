import { request } from './http'

export function listNotes() {
  return request('/api/notes')
}

export function getNote(id) {
  return request(`/api/notes/${id}`)
}

export function createNote(payload) {
  return request('/api/notes', { method: 'POST', body: payload })
}
