import { request } from './http'

export function listChatConversations() {
  return request('/api/chat/conversations')
}

export function listChatMessages(conversationId) {
  return request(`/api/chat/conversations/${conversationId}/messages`)
}

export function sendChatMessage(conversationId, message) {
  return request('/api/chat', {
    method: 'POST',
    body: { conversationId, message },
  })
}
