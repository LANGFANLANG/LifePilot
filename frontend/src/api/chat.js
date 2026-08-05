import { request } from './http'

export function sendChatMessage(conversationId, message) {
  return request('/api/chat', {
    method: 'POST',
    body: { conversationId, message },
  })
}
