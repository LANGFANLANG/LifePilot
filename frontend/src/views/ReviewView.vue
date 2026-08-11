<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { draftDailyReview, getDailyReview, saveDailyReview } from '../api/dailyReviews'
import { formatDate } from '../utils/format'

function localDateInputValue(date = new Date()) {
  const pad = (n) => `${n}`.padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

const today = localDateInputValue()
const reviewDate = ref(today)
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const message = ref('')
const reviewStatus = ref('')

const form = reactive({
  completedSummary: '',
  unfinishedSummary: '',
  newTasksSummary: '',
  reflection: '',
  tomorrowPlan: '',
})

const statusLabel = computed(() => {
  if (saving.value) return '保存中'
  if (loading.value) return '读取中'
  if (reviewStatus.value === 'SAVED') return '已保存'
  if (reviewStatus.value === 'DRAFT') return '草稿'
  return '未保存'
})

function resetForm() {
  form.completedSummary = ''
  form.unfinishedSummary = ''
  form.newTasksSummary = ''
  form.reflection = ''
  form.tomorrowPlan = ''
  reviewStatus.value = ''
}

function assign(review) {
  form.completedSummary = review.completedSummary || ''
  form.unfinishedSummary = review.unfinishedSummary || ''
  form.newTasksSummary = review.newTasksSummary || ''
  form.reflection = review.reflection || ''
  form.tomorrowPlan = review.tomorrowPlan || ''
  reviewStatus.value = review.status || 'DRAFT'
}

async function loadExisting() {
  loading.value = true
  error.value = ''
  message.value = ''
  try {
    assign(await getDailyReview(reviewDate.value))
    message.value = '已读取这一天的复盘记录。'
  } catch (e) {
    resetForm()
    error.value = e.code === 'BAD_REQUEST' ? '这一天还没有复盘，可以直接填写后保存，或先生成草稿。' : e.message
  } finally {
    loading.value = false
  }
}

async function draft() {
  loading.value = true
  error.value = ''
  message.value = ''
  try {
    assign(await draftDailyReview(reviewDate.value))
    message.value = '草稿已生成，确认后记得保存。'
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function save() {
  saving.value = true
  error.value = ''
  message.value = ''
  try {
    assign(await saveDailyReview(reviewDate.value, { ...form }))
    message.value = '复盘已保存，可以通过日期再次读取。'
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}

onMounted(loadExisting)
</script>

<template>
  <header class="page-head review-head">
    <div>
      <h1 class="page-title"><em>{{ formatDate(reviewDate) || '每日复盘' }}</em></h1>
      <p class="today-subtitle">{{ statusLabel }}</p>
    </div>
    <div class="review-date-picker">
      <input v-model="reviewDate" type="date" @change="loadExisting" />
      <button class="btn btn-ghost" :disabled="loading" @click="loadExisting">读取复盘</button>
      <button class="btn btn-primary" :disabled="loading" @click="draft">{{ loading ? '生成中…' : 'AI 生成草稿' }}</button>
      <button class="btn btn-primary" :disabled="saving || loading" @click="save">
        {{ saving ? '保存中…' : '保存复盘' }}
      </button>
    </div>
  </header>

  <div v-if="error" class="form-error">{{ error }}</div>
  <div v-else-if="message" class="review-message">{{ message }}</div>

  <section class="review-grid">
    <label class="review-field">
      <span>已完成工作</span>
      <textarea v-model="form.completedSummary"></textarea>
    </label>
    <label class="review-field">
      <span>未完成工作</span>
      <textarea v-model="form.unfinishedSummary"></textarea>
    </label>
    <label class="review-field">
      <span>新增任务</span>
      <textarea v-model="form.newTasksSummary"></textarea>
    </label>
    <label class="review-field">
      <span>反思</span>
      <textarea v-model="form.reflection"></textarea>
    </label>
    <label class="review-field span-2">
      <span>明日计划</span>
      <textarea v-model="form.tomorrowPlan"></textarea>
    </label>
  </section>

  <div class="review-actions">
    <button class="btn btn-primary" :disabled="saving || loading" @click="save">
      {{ saving ? '保存中…' : '保存复盘' }}
    </button>
  </div>
</template>
