<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listChatConversations, listChatMessages, sendChatMessage } from '../api/chat'
import { confirmPlanPreview, getPlanPreview, rejectPlanPreview } from '../api/planPreviews'
import { greeting } from '../utils/format'
import { renderMarkdown } from '../utils/markdown'

const router = useRouter()
const conversations = ref([])
const messages = ref([])
const conversationId = ref(null)
const input = ref('')
const busy = ref(false)
const loadingConversations = ref(false)
const loadingMessages = ref(false)
const error = ref('')
const logEl = ref(null)

const headline = computed(() => `${greeting()}，想聊点什么？`)
const composerDisabled = computed(() => busy.value || loadingMessages.value)

async function scrollToBottom() {
  await nextTick()
  if (logEl.value) logEl.value.scrollTop = logEl.value.scrollHeight
}

function normalizeMessage(message) {
  return {
    id: message.id,
    role: String(message.role || '').toLowerCase(),
    content: message.content,
    createdAt: message.createdAt,
  }
}

function formatConversationTime(value) {
  if (!value) return ''
  return new Intl.DateTimeFormat('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}

async function loadConversations(selectLatest = true) {
  loadingConversations.value = true
  error.value = ''
  try {
    conversations.value = await listChatConversations()
    if (selectLatest && conversations.value.length) {
      await selectConversation(conversations.value[0].id)
    }
  } catch (e) {
    error.value = e.message
  } finally {
    loadingConversations.value = false
  }
}

async function selectConversation(id) {
  if (!id || loadingMessages.value) return
  conversationId.value = id
  loadingMessages.value = true
  error.value = ''
  try {
    const loaded = await listChatMessages(id)
    messages.value = loaded
      .map(normalizeMessage)
      .filter((message) => ['user', 'assistant'].includes(message.role))
    await scrollToBottom()
  } catch (e) {
    error.value = e.message
    conversationId.value = null
    messages.value = []
    await loadConversations(false)
  } finally {
    loadingMessages.value = false
  }
}

function startNewConversation() {
  conversationId.value = null
  messages.value = []
  error.value = ''
}

async function send() {
  const text = input.value.trim()
  if (!text || composerDisabled.value) return

  const wasNewConversation = !conversationId.value
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  busy.value = true
  error.value = ''
  scrollToBottom()

  try {
    const reply = await sendChatMessage(conversationId.value, text)
    conversationId.value = reply.conversationId
    const plans = await loadPlanActions(reply.actions || [])
    messages.value.push({ role: 'assistant', content: reply.content, plans })
    await loadConversations(false)
    if (wasNewConversation && !conversations.value.some((conversation) => conversation.id === reply.conversationId)) {
      conversations.value.unshift({
        id: reply.conversationId,
        title: text,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      })
    }
  } catch (e) {
    error.value = e.message
    messages.value.push({ role: 'assistant', content: `抱歉，出错了：${e.message}` })
  } finally {
    busy.value = false
    scrollToBottom()
  }
}

async function loadPlanActions(actions) {
  const planActions = actions.filter((action) => action.type === 'PLAN_PREVIEW' && action.resourceId)
  return Promise.all(planActions.map(async (action) => ({
    action,
    preview: await getPlanPreview(action.resourceId),
    status: 'READY',
    error: '',
  })))
}

async function confirmPlan(plan) {
  plan.status = 'SAVING'
  plan.error = ''
  try {
    const todos = await confirmPlanPreview(plan.preview.id)
    plan.status = 'CONFIRMED'
    plan.createdCount = todos.length
    await router.push('/')
  } catch (e) {
    plan.status = 'READY'
    plan.error = e.message
  }
}

async function rejectPlan(plan) {
  plan.status = 'SAVING'
  plan.error = ''
  try {
    plan.preview = await rejectPlanPreview(plan.preview.id)
    plan.status = 'REJECTED'
  } catch (e) {
    plan.status = 'READY'
    plan.error = e.message
  }
}

function onKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    send()
  }
}

onMounted(() => {
  loadConversations()
})
</script>

<template>
  <header class="page-head">
    <h1 class="page-title"><em>{{ headline }}</em></h1>
  </header>

  <div class="chat-workspace">
    <aside class="chat-history">
      <div class="chat-history-head">
        <span>会话记录</span>
        <button class="btn btn-ghost" type="button" @click="startNewConversation">新对话</button>
      </div>

      <div v-if="loadingConversations" class="chat-history-empty">正在读取会话...</div>
      <div v-else-if="!conversations.length" class="chat-history-empty">还没有会话</div>
      <button
        v-for="conversation in conversations"
        v-else
        :key="conversation.id"
        type="button"
        class="chat-history-item"
        :class="{ active: conversation.id === conversationId }"
        @click="selectConversation(conversation.id)"
      >
        <span>{{ conversation.title }}</span>
        <time>{{ formatConversationTime(conversation.updatedAt) }}</time>
      </button>
    </aside>

    <div class="chat-shell">
      <div ref="logEl" class="chat-log">
        <div v-if="error" class="form-error">{{ error }}</div>

        <div v-if="loadingMessages" class="chat-hint">正在读取消息...</div>
        <div v-else-if="!messages.length" class="chat-hint">
          <p style="margin: 0 0 6px">我可以帮你把想法落成计划</p>
          <p style="margin: 0; font-size: 11.5px; color: var(--hairline-strong)">例如：「帮我规划今天的安排」或「整理一下待办清单」</p>
        </div>

        <div v-for="(msg, i) in messages" :key="msg.id || i" class="msg" :class="msg.role">
          <div class="avatar">{{ msg.role === 'user' ? '我' : 'P' }}</div>
          <div class="bubble">
            <div v-if="msg.role === 'assistant'" class="markdown" v-html="renderMarkdown(msg.content)"></div>
            <div v-else>{{ msg.content }}</div>

            <div v-for="plan in msg.plans || []" :key="plan.preview.id" class="plan-preview">
              <div class="plan-preview-head">
                <div>
                  <div class="plan-preview-title">{{ plan.preview.goal }}</div>
                  <div class="plan-preview-meta">{{ plan.preview.tasks.length }} 个任务 · {{ plan.preview.status }}</div>
                </div>
              </div>

              <div class="plan-preview-tasks">
                <div v-for="task in plan.preview.tasks" :key="task.id" class="plan-preview-task">
                  <span>{{ task.title }}</span>
                  <small>
                    {{ task.priority || 'MEDIUM' }}
                    <template v-if="task.estimatedMinutes"> · {{ task.estimatedMinutes }} 分钟</template>
                  </small>
                </div>
              </div>

              <div v-if="plan.error" class="form-error">{{ plan.error }}</div>

              <div class="plan-preview-actions">
                <button class="btn btn-ghost" :disabled="plan.status !== 'READY'" @click="rejectPlan(plan)">拒绝</button>
                <button class="btn btn-primary" :disabled="plan.status !== 'READY'" @click="confirmPlan(plan)">
                  {{ plan.status === 'CONFIRMED' ? `已创建 ${plan.createdCount || 0} 个待办` : plan.status === 'SAVING' ? '处理中...' : '确认生成待办' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="busy" class="msg assistant">
          <div class="avatar">P</div>
          <div class="bubble">
            <span class="typing"><i></i><i></i><i></i></span>
          </div>
        </div>
      </div>

      <div class="chat-box">
        <textarea
          v-model="input"
          rows="1"
          placeholder="说点什么，按下 Enter 发送..."
          :disabled="composerDisabled"
          @keydown="onKeydown"
        ></textarea>
        <button class="btn btn-primary" :disabled="composerDisabled || !input.trim()" @click="send">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 2 11 13M22 2l-7 20-4-9-9-4Z" />
          </svg>
          发送
        </button>
      </div>
    </div>
  </div>
</template>
