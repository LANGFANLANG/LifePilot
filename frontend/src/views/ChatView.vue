<script setup>
import { computed, nextTick, ref } from 'vue'
import { useRouter } from 'vue-router'
import { sendChatMessage } from '../api/chat'
import { confirmPlanPreview, getPlanPreview, rejectPlanPreview } from '../api/planPreviews'
import { greeting } from '../utils/format'

const router = useRouter()
const messages = ref([])
const conversationId = ref(null)
const input = ref('')
const busy = ref(false)
const error = ref('')
const logEl = ref(null)

const headline = computed(() => `${greeting()}，想聊点什么？`)

async function scrollToBottom() {
  await nextTick()
  if (logEl.value) logEl.value.scrollTop = logEl.value.scrollHeight
}

async function send() {
  const text = input.value.trim()
  if (!text || busy.value) return

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
</script>

<template>
  <header class="page-head">
    <h1 class="page-title"><em>{{ headline }}</em></h1>
  </header>

  <div class="chat-shell">
    <div ref="logEl" class="chat-log">
      <div v-if="!messages.length" class="chat-hint">
        <p style="margin: 0 0 6px">我可以帮你把想法落成计划</p>
        <p style="margin: 0; font-size: 11.5px; color: var(--hairline-strong)">例如：「帮我规划今天的安排」或「整理一下待办清单」</p>
      </div>

      <div v-for="(msg, i) in messages" :key="i" class="msg" :class="msg.role">
        <div class="avatar">{{ msg.role === 'user' ? '我' : 'P' }}</div>
        <div class="bubble">
          <div>{{ msg.content }}</div>

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
                {{ plan.status === 'CONFIRMED' ? `已创建 ${plan.createdCount || 0} 个待办` : plan.status === 'SAVING' ? '处理中…' : '确认生成待办' }}
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
        placeholder="说点什么，按下 Enter 发送…"
        :disabled="busy"
        @keydown="onKeydown"
      ></textarea>
      <button class="btn btn-primary" :disabled="busy || !input.trim()" @click="send">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M22 2 11 13M22 2l-7 20-4-9-9-4Z" />
        </svg>
        发送
      </button>
    </div>
  </div>
</template>
