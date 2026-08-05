import { request } from './http'

export function listTodos() {
  return request('/api/todos')
}

export function createTodo(payload) {
  return request('/api/todos', { method: 'POST', body: payload })
}

export function updateTodo(id, payload) {
  return request(`/api/todos/${id}`, { method: 'PUT', body: payload })
}

export function completeTodo(id) {
  return request(`/api/todos/${id}/complete`, { method: 'POST' })
}

export function deleteTodo(id) {
  return request(`/api/todos/${id}`, { method: 'DELETE' })
}
