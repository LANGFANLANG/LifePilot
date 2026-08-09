<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  createNote,
  deleteNote,
  getNote,
  getNoteFileUrl,
  listNotes,
  replaceNoteFile,
  updateNote,
  uploadNote,
} from '../api/notes'
import { formatDate, greeting } from '../utils/format'
import { renderMarkdown } from '../utils/markdown'

const notes = ref([])
const loading = ref(true)
const error = ref('')
const selected = ref(null)
const fileUrl = ref('')

const form = reactive({ title: '', content: '' })
const saving = ref(false)
const formError = ref('')

const uploadInput = ref(null)
const uploading = ref(false)
const uploadError = ref('')

const editing = ref(false)
const editForm = reactive({ title: '', content: '' })
const editSaving = ref(false)
const editError = ref('')

const replaceInput = ref(null)
const replacing = ref(false)

const headline = computed(() => {
  const count = notes.value.length
  return count ? `${greeting()}，已经攒下 ${count} 页随手记` : `${greeting()}，留点只言片语`
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    notes.value = await listNotes()
    const first = notes.value[0]
    if (first && (!selected.value || !notes.value.some((note) => note.id === selected.value?.id))) {
      await openNote(first)
    }
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function openNote(note) {
  if (selected.value?.id === note.id && note.content != null && !editing.value) return
  selected.value = note
  fileUrl.value = ''
  editing.value = false
  editError.value = ''
  try {
    const detail = await getNote(note.id)
    selected.value = detail
    if (isPdfNote(detail)) {
      fileUrl.value = (await getNoteFileUrl(detail.id)).url
    }
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
    await openNote(created)
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
    await openNote(created)
    event.target.value = ''
  } catch (e) {
    uploadError.value = e.message
  } finally {
    uploading.value = false
  }
}

function startEdit() {
  if (!selected.value) return
  editForm.title = selected.value.title || ''
  editForm.content = selected.value.content || ''
  editError.value = ''
  editing.value = true
}

function cancelEdit() {
  editing.value = false
  editError.value = ''
}

async function saveEdit() {
  if (!selected.value) return
  const title = editForm.title.trim()
  const content = editForm.content.trim()
  if (!title || !content) {
    editError.value = '请填写标题和内容'
    return
  }
  editSaving.value = true
  editError.value = ''
  try {
    const updated = await updateNote(selected.value.id, { title, content })
    notes.value = notes.value.map((note) => note.id === updated.id ? updated : note)
    selected.value = updated
    editing.value = false
  } catch (e) {
    editError.value = e.message
  } finally {
    editSaving.value = false
  }
}

async function deleteSelected() {
  if (!selected.value) return
  if (!window.confirm(`确定删除「${selected.value.title}」吗？`)) return
  const id = selected.value.id
  try {
    await deleteNote(id)
    notes.value = notes.value.filter((note) => note.id !== id)
    selected.value = null
    fileUrl.value = ''
    editing.value = false
    if (notes.value[0]) await openNote(notes.value[0])
  } catch (e) {
    error.value = e.message
  }
}

async function replaceSelectedFile(event) {
  const file = event.target.files?.[0]
  if (!file || !selected.value) return
  replacing.value = true
  error.value = ''
  try {
    const updated = await replaceNoteFile(selected.value.id, file)
    notes.value = notes.value.map((note) => note.id === updated.id ? updated : note)
    await openNote(updated)
    event.target.value = ''
  } catch (e) {
    error.value = e.message
  } finally {
    replacing.value = false
  }
}

function isPdfNote(note) {
  return note?.sourceType === 'FILE' && (
    note.contentType === 'application/pdf' ||
    note.originalFilename?.toLowerCase().endsWith('.pdf')
  )
}

function isMarkdownNote(note) {
  const filename = note?.originalFilename?.toLowerCase() || ''
  return note?.sourceType === 'FILE' && (
    note.contentType === 'text/markdown' ||
    filename.endsWith('.md') ||
    filename.endsWith('.markdown')
  )
}

async function openOriginal(download = false) {
  if (!selected.value?.id) return
  const link = await getNoteFileUrl(selected.value.id, download)
  window.open(link.url, '_blank', 'noopener')
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
            <textarea v-model="form.content" placeholder="把此刻的念头留下来..."></textarea>
          </div>
          <div class="form-foot">
            <button type="submit" class="btn btn-primary" :disabled="saving">{{ saving ? '保存中…' : '保存' }}</button>
          </div>
        </form>
      </div>

      <div class="card form-panel note-upload" style="margin: 0">
        <div class="form-title">上传笔记文件</div>
        <p>支持 Markdown、文本、CSV、PDF、Word 和 Excel，上传后可在右侧查看预览。</p>
        <div v-if="uploadError" class="form-error">{{ uploadError }}</div>
        <input
          ref="uploadInput"
          type="file"
          accept=".md,.markdown,.txt,.csv,.pdf,.doc,.docx,.xls,.xlsx"
          :disabled="uploading"
          @change="uploadSelected"
        />
        <button type="button" class="btn btn-ghost" :disabled="uploading" @click="uploadInput?.click()">
          {{ uploading ? '上传中…' : '选择文件上传' }}
        </button>
      </div>

      <div v-if="loading" class="empty">
        <span class="empty-glyph">···</span>
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
        <div class="note-reader-head">
          <div>
            <h2>{{ selected.title }}</h2>
            <div class="note-date">{{ formatDate(selected.createdAt) }}</div>
          </div>
          <div class="note-reader-actions">
            <button v-if="!editing" type="button" class="btn btn-ghost" @click="startEdit">编辑</button>
            <button type="button" class="btn btn-ghost danger" @click="deleteSelected">删除</button>
          </div>
        </div>

        <div v-if="selected.sourceType === 'FILE'" class="note-file-meta">
          {{ selected.originalFilename }}<template v-if="selected.fileSize"> · {{ Math.ceil(selected.fileSize / 1024) }} KB</template>
          <div class="note-file-actions">
            <button type="button" class="btn btn-ghost" @click="openOriginal(false)">查看原文件</button>
            <button type="button" class="btn btn-ghost" @click="openOriginal(true)">下载</button>
            <input
              ref="replaceInput"
              type="file"
              accept=".md,.markdown,.txt,.csv,.pdf,.doc,.docx,.xls,.xlsx"
              style="display: none"
              :disabled="replacing"
              @change="replaceSelectedFile"
            />
            <button type="button" class="btn btn-ghost" :disabled="replacing" @click="replaceInput?.click()">
              {{ replacing ? '替换中…' : '替换文件' }}
            </button>
          </div>
        </div>

        <div v-if="editing" class="note-edit-panel">
          <div v-if="editError" class="form-error">{{ editError }}</div>
          <input v-model="editForm.title" maxlength="200" placeholder="标题" />
          <textarea v-model="editForm.content" rows="12" placeholder="内容"></textarea>
          <div class="note-edit-actions">
            <button type="button" class="btn btn-ghost" :disabled="editSaving" @click="cancelEdit">取消</button>
            <button type="button" class="btn btn-primary" :disabled="editSaving" @click="saveEdit">
              {{ editSaving ? '保存中…' : '保存修改' }}
            </button>
          </div>
        </div>

        <template v-else>
          <iframe v-if="isPdfNote(selected) && fileUrl" class="note-pdf-viewer" :src="fileUrl" title="PDF 预览"></iframe>
          <div v-else-if="isMarkdownNote(selected)" class="note-content markdown" v-html="renderMarkdown(selected.content)"></div>
          <div v-else class="note-content">{{ selected.content }}</div>
        </template>
      </template>

      <template v-else>
        <div class="empty" style="border: none; padding: 40px">
          <span class="empty-glyph">✓</span>
          <p>{{ loading ? '加载中…' : '从左侧选择一则笔记，或先写点什么' }}</p>
        </div>
      </template>
    </div>
  </div>
</template>
