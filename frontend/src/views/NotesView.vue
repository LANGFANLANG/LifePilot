<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { createNote, getNote, listNotes } from '../api/notes'
import { formatDate, greeting } from '../utils/format'

const notes = ref([])
const loading = ref(true)
const error = ref('')
const selected = ref(null)

const form = reactive({ title: '', content: '' })
const saving = ref(false)
const formError = ref('')

const headline = computed(() => {
  const g = greeting()
  const count = notes.value.length
  return count ? `${g}，已经攒下 ${count} 页随手记` : `${g}，留点只言片语`
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    notes.value = await listNotes()
    const first = notes.value[0]
    if (first && (!selected.value || !notes.value.some((n) => n.id === selected.value?.id))) {
      selected.value = first
    }
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function openNote(note) {
  if (selected.value?.id === note.id && note.content != null) return
  selected.value = note
  try {
    const detail = await getNote(note.id)
    selected.value = detail
  } catch (e) {
    error.value = e.message
  }
}

async function submit() {
  const title = form.title.trim()
  const content = form.content.trim()
  if (!title) {
    formError.value = '请填写标题'
    return
  }
  if (!content) {
    formError.value = '请写点什么内容'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    const created = await createNote({ title, content })
    notes.value = [created, ...notes.value]
    form.title = ''
    form.content = ''
    selected.value = created
  } catch (e) {
    formError.value = e.message
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <header class="page-head">
    <div>
      <h1 class="page-title"><em>{{ headline }}</em></h1>
    </div>
  </header>

  <div v-if="error" class="form-error">{{ error }}</div>

  <div class="notes-layout">
    <div class="notes-panel">
      <div class="card form-panel note-new" style="margin: 0">
        <div class="form-title">写一则笔记</div>
        <div v-if="formError" class="form-error">{{ formError }}</div>
        <form @submit.prevent="submit">
          <div class="field">
            <input v-model="form.title" maxlength="200" placeholder="标题" style="margin-bottom: 10px" />
          </div>
          <div class="field">
            <textarea v-model="form.content" placeholder="把此刻的念头留下来…"></textarea>
          </div>
          <div class="form-foot">
            <button type="submit" class="btn btn-primary" :disabled="saving">{{ saving ? '保存中…' : '保存' }}</button>
          </div>
        </form>
      </div>

      <div v-if="loading" class="empty">
        <span class="empty-glyph">· · ·</span>
        <p>正在翻阅笔记本…</p>
      </div>

      <div v-else class="note-list">
        <button
          v-for="note in notes"
          :key="note.id"
          class="note-card"
          :class="{ active: selected?.id === note.id }"
          @click="openNote(note)"
        >
          <h3>{{ note.title }}</h3>
          <p class="note-preview">{{ note.content }}</p>
          <div class="note-date">{{ formatDate(note.createdAt) }}</div>
        </button>
      </div>
    </div>

    <div class="card note-reader" :class="{ 'empty-state': !selected }">
      <template v-if="selected">
        <h2>{{ selected.title }}</h2>
        <div class="note-date">{{ formatDate(selected.createdAt) }}</div>
        <div class="note-content">{{ selected.content }}</div>
      </template>
      <template v-else>
        <div class="empty" style="border: none; padding: 40px">
          <span class="empty-glyph">✎</span>
          <p>{{ loading ? '加载中…' : '从左侧选择一则笔记，或先写点什么' }}</p>
        </div>
      </template>
    </div>
  </div>
</template>
