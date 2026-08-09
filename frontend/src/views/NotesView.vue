<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { createNote, getNote, listNotes, uploadNote } from '../api/notes'
import { formatDate, greeting } from '../utils/format'

const notes = ref([])
const loading = ref(true)
const error = ref('')
const selected = ref(null)

const form = reactive({ title: '', content: '' })
const saving = ref(false)
const formError = ref('')
const uploadInput = ref(null)
const uploading = ref(false)
const uploadError = ref('')

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

async function uploadSelected(event) {
  const file = event.target.files?.[0]
  if (!file) return

  uploading.value = true
  uploadError.value = ''
  try {
    const created = await uploadNote(file)
    notes.value = [created, ...notes.value]
    selected.value = created
    event.target.value = ''
  } catch (e) {
    uploadError.value = e.message
  } finally {
    uploading.value = false
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

      <div class="card form-panel note-upload" style="margin: 0">
        <div class="form-title">上传笔记文件</div>
        <p>支持 Markdown、文本、CSV、Word 和 Excel，上传后可在右侧查看预览。</p>
        <div v-if="uploadError" class="form-error">{{ uploadError }}</div>
        <input
          ref="uploadInput"
          type="file"
          accept=".md,.markdown,.txt,.csv,.doc,.docx,.xls,.xlsx"
          :disabled="uploading"
          @change="uploadSelected"
        />
        <button type="button" class="btn btn-ghost" :disabled="uploading" @click="uploadInput?.click()">
          {{ uploading ? '上传中…' : '选择文件上传' }}
        </button>
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
          <span v-if="note.sourceType === 'FILE'" class="note-kind">文件</span>
          <p class="note-preview">{{ note.content }}</p>
          <div class="note-date">{{ formatDate(note.createdAt) }}</div>
        </button>
      </div>
    </div>

    <div class="card note-reader" :class="{ 'empty-state': !selected }">
      <template v-if="selected">
        <h2>{{ selected.title }}</h2>
        <div class="note-date">{{ formatDate(selected.createdAt) }}</div>
        <div v-if="selected.sourceType === 'FILE'" class="note-file-meta">
          {{ selected.originalFilename }}<template v-if="selected.fileSize"> · {{ Math.ceil(selected.fileSize / 1024) }} KB</template>
        </div>
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
