<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { completeTodo } from '../api/todos'
import { getTodayPlan } from '../api/planning'
import { listRecentReminders } from '../api/reminders'
import { formatDateTime, greeting } from '../utils/format'

const router = useRouter()
const plan = ref(null)
const reminders = ref([])
const loading = ref(true)
const error = ref('')

const focus = computed(() => plan.value?.focus || [])
const timeline = computed(() => plan.value?.timeline || [])
const inbox = computed(() => plan.value?.inbox || [])

const completionHint = computed(() => {
  const pending = plan.value?.pendingCount || 0
  if (!pending) return '今天没有待处理事项'
  const overdue = plan.value?.overdueCount || 0
  if (overdue) return `${overdue} 项已逾期，先把失控的部分收回来`
  return `${pending} 项待处理，预计 ${plan.value?.estimatedMinutes || 0} 分钟`
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [todayPlan, recentReminders] = await Promise.all([
      getTodayPlan(),
      listRecentReminders(),
    ])
    plan.value = todayPlan
    reminders.value = recentReminders
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function complete(item) {
  try {
    await completeTodo(item.id)
    await load()
  } catch (e) {
    error.value = e.message
  }
}

function openChat() {
  router.push('/chat')
}

onMounted(load)
</script>

<template>
  <header class="page-head today-head">
    <div>
      <h1 class="page-title"><em>{{ greeting() }}，先看今天</em></h1>
      <p class="today-subtitle">{{ completionHint }}</p>
    </div>
    <button class="btn btn-primary" @click="openChat">
      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M12 3v18M3 12h18" />
      </svg>
      找 AI 规划
    </button>
  </header>

  <div v-if="error" class="form-error">{{ error }}</div>

  <div v-if="loading" class="empty">
    <span class="empty-glyph">· · ·</span>
    <p>正在生成今日计划…</p>
  </div>

  <template v-else-if="plan">
    <section v-if="reminders.length" class="reminder-strip">
      <div class="reminder-strip-head">
        <span>最近提醒</span>
        <strong>{{ reminders.length }}</strong>
      </div>
      <div class="reminder-strip-list">
        <article v-for="reminder in reminders.slice(0, 3)" :key="reminder.id" class="reminder-pill">
          <span>{{ reminder.message }}</span>
          <time>{{ formatDateTime(reminder.createdAt) }}</time>
        </article>
      </div>
    </section>

    <section class="today-metrics">
      <div class="metric">
        <span>待处理</span>
        <strong>{{ plan.pendingCount }}</strong>
      </div>
      <div class="metric">
        <span>已逾期</span>
        <strong>{{ plan.overdueCount }}</strong>
      </div>
      <div class="metric">
        <span>预计投入</span>
        <strong>{{ plan.estimatedMinutes }}m</strong>
      </div>
    </section>

    <section class="today-grid">
      <div class="today-panel focus-panel">
        <div class="panel-head">
          <h2>今日重点</h2>
          <span>最多 3 项</span>
        </div>
        <div v-if="focus.length" class="focus-list">
          <article v-for="item in focus" :key="item.id" class="focus-item" :class="{ overdue: item.overdue }">
            <button class="todo-check" :aria-label="`完成 ${item.title}`" @click="complete(item)"></button>
            <div>
              <div class="focus-rank">#{{ item.rank }}</div>
              <h3>{{ item.title }}</h3>
              <p v-if="item.description">{{ item.description }}</p>
              <div class="todo-meta">
                <span class="chip" :class="`priority-${item.priority}`">{{ item.priority }}</span>
                <span v-if="item.category" class="chip">{{ item.category }}</span>
                <span v-if="item.estimatedMinutes" class="chip">{{ item.estimatedMinutes }} 分钟</span>
                <span v-if="item.dueAt" class="chip">截止 {{ formatDateTime(item.dueAt) }}</span>
              </div>
            </div>
          </article>
        </div>
        <div v-else class="panel-empty">没有重点任务，今天的航线很轻。</div>
      </div>

      <div class="today-panel">
        <div class="panel-head">
          <h2>今日时间线</h2>
          <span>{{ timeline.length }} 项</span>
        </div>
        <div v-if="timeline.length" class="timeline-list">
          <article v-for="item in timeline" :key="item.id" class="timeline-item">
            <time>{{ formatDateTime(item.plannedStartAt).slice(11) }}</time>
            <div>
              <h3>{{ item.title }}</h3>
              <p v-if="item.estimatedMinutes">{{ item.estimatedMinutes }} 分钟</p>
            </div>
          </article>
        </div>
        <div v-else class="panel-empty">还没有安排具体开始时间。</div>
      </div>

      <div class="today-panel">
        <div class="panel-head">
          <h2>待规划</h2>
          <span>{{ inbox.length }} 项</span>
        </div>
        <div v-if="inbox.length" class="inbox-list">
          <article v-for="item in inbox" :key="item.id" class="inbox-item">
            <span>{{ item.title }}</span>
            <button class="btn btn-ghost" @click="router.push('/todos')">去规划</button>
          </article>
        </div>
        <div v-else class="panel-empty">未规划事项已经清空。</div>
      </div>
    </section>
  </template>
</template>
