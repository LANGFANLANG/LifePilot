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

export function uploadNote(file) {
  const body = new FormData()
  body.append('file', file)
  return request('/api/notes/upload', { method: 'POST', body })
}

export function getNoteFileUrl(id, download = false) {
  return request(`/api/notes/${id}/file-url?download=${download}`)
}
