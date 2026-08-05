<script setup>
import { reactive, ref, watch } from 'vue'
import { createTodo, updateTodo } from '../api/todos'
import { fromDatetimeInput, toLocalDatetimeInput } from '../utils/format'

const emit = defineEmits(['saved'])

const props = defineProps({
  editing: { type: Object, default: null },
})

const open = ref(false)
const saving = ref(false)
const error = ref('')

const blank = () => ({
  title: '',
  description: '',
  priority: 'MEDIUM',
  category: '',
  estimatedMinutes: '',
  dueAt: '',
})

const form = reactive(blank())

watch(props.editing, (value) => {
  open.value = !!value
  error.value = ''
  if (value) {
    Object.assign(form, {
      title: value.title || '',
      description: value.description || '',
      priority: value.priority || 'MEDIUM',
      category: value.category || '',
      estimatedMinutes: value.estimatedMinutes ?? '',
      dueAt: toLocalDatetimeInput(value.dueAt),
    })
  }
})

function reset() {
  Object.assign(form, blank())
}

function openNew() {
  error.value = ''
  reset()
  open.value = true
}

async function submit() {
  const title = form.title.trim()
  if (!title) {
    error.value = '请填写待办标题'
    return
  }
  const payload = {
    title,
    description: form.description.trim() || null,
    priority: form.priority,
    category: form.category.trim() || null,
    estimatedMinutes: form.estimatedMinutes === '' ? null : Number(form.estimatedMinutes),
    dueAt: fromDatetimeInput(form.dueAt),
    plannedStartAt: null,
    reminderAt: null,
    parentTodoId: null,
    source: 'web',
  }
  saving.value = true
  error.value = ''
  try {
    if (props.editing) {
      await updateTodo(props.editing.id, payload)
    } else {
      await createTodo(payload)
    }
    open.value = false
    reset()
    emit('saved')
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div>
    <button class="btn btn-primary" @click="open ? (open = false) : openNew()">
      <svg v-if="!open" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round">
        <path d="M12 5v14M5 12h14" />
      </svg>
      <span>{{ editing ? '编辑待办' : open ? '收起' : '新增待办' }}</span>
    </button>

    <div v-if="open" class="card form-panel">
      <div class="form-title">
        <span>{{ editing ? `编辑 · ${editing.title}` : '新建待办' }}</span>
      </div>

      <div v-if="error" class="form-error">{{ error }}</div>

      <form class="form-grid" @submit.prevent="submit">
        <div class="field span-2">
          <label for="todo-title">标题</label>
          <input id="todo-title" v-model="form.title" maxlength="200" placeholder="今天想做点什么？" autofocus />
        </div>

        <div class="field span-2">
          <label for="todo-desc">描述</label>
          <textarea id="todo-desc" v-model="form.description" placeholder="补充细节（可选）"></textarea>
        </div>

        <div class="field">
          <label for="todo-priority">优先级</label>
          <select id="todo-priority" v-model="form.priority">
            <option value="LOW">低</option>
            <option value="MEDIUM">中</option>
            <option value="HIGH">高</option>
          </select>
        </div>

        <div class="field">
          <label for="todo-category">分类</label>
          <input id="todo-category" v-model="form.category" maxlength="80" placeholder="如：工作 / 学习 / 生活" />
        </div>

        <div class="field">
          <label for="todo-due">截止时间</label>
          <input id="todo-due" type="datetime-local" v-model="form.dueAt" />
        </div>

        <div class="field">
          <label for="todo-estimate">预计耗时（分钟）</label>
          <input id="todo-estimate" type="number" min="0" v-model="form.estimatedMinutes" placeholder="30" />
        </div>

        <div class="form-foot span-2">
          <button type="button" class="btn btn-ghost" @click="open = false">取消</button>
          <button type="submit" class="btn btn-primary" :disabled="saving">{{ saving ? '保存中…' : editing ? '保存修改' : '创建' }}</button>
        </div>
      </form>
    </div>
  </div>
</template>
