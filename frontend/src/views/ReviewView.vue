<script setup>
import { computed, reactive, ref } from 'vue'
import { draftDailyReview, getDailyReview, saveDailyReview } from '../api/dailyReviews'
import { formatDate } from '../utils/format'

const today = new Date().toISOString().slice(0, 10)
const reviewDate = ref(today)
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const loaded = ref(false)

const form = reactive({
  completedSummary: '',
  unfinishedSummary: '',
  newTasksSummary: '',
  reflection: '',
  tomorrowPlan: '',
})

const statusLabel = computed(() => loaded.value ? '已载入' : '未生成')

function assign(review) {
  form.completedSummary = review.completedSummary || ''
  form.unfinishedSummary = review.unfinishedSummary || ''
  form.newTasksSummary = review.newTasksSummary || ''
  form.reflection = review.reflection || ''
  form.tomorrowPlan = review.tomorrowPlan || ''
  loaded.value = true
}

async function loadExisting() {
  loading.value = true
  error.value = ''
  try {
    assign(await getDailyReview(reviewDate.value))
  } catch (e) {
    error.value = e.code === 'BAD_REQUEST' ? '这一天还没有复盘，先生成草稿。' : e.message
    loaded.value = false
  } finally {
    loading.value = false
  }
}

async function draft() {
  loading.value = true
  error.value = ''
  try {
    assign(await draftDailyReview(reviewDate.value))
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function save() {
  saving.value = true
  error.value = ''
  try {
    assign(await saveDailyReview(reviewDate.value, { ...form }))
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <header class="page-head review-head">
    <div>
      <h1 class="page-title"><em>{{ formatDate(reviewDate) || '每日复盘' }}</em></h1>
      <p class="today-subtitle">{{ statusLabel }}</p>
    </div>
    <div class="review-date-picker">
      <input v-model="reviewDate" type="date" @change="loadExisting" />
      <button class="btn btn-ghost" :disabled="loading" @click="loadExisting">读取</button>
      <button class="btn btn-primary" :disabled="loading" @click="draft">{{ loading ? '生成中…' : '生成草稿' }}</button>
    </div>
  </header>

  <div v-if="error" class="form-error">{{ error }}</div>

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
