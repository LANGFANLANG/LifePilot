<script setup>
import { computed, onMounted, ref } from 'vue'
import { completeTodo, deleteTodo, listTodos } from '../api/todos'
import { greeting } from '../utils/format'
import TodoForm from '../components/TodoForm.vue'
import TodoItem from '../components/TodoItem.vue'

const todos = ref([])
const loading = ref(true)
const error = ref('')
const filter = ref('ALL')
const editing = ref(null)

const filters = [
  { key: 'ALL', label: '全部' },
  { key: 'PENDING', label: '进行中' },
  { key: 'COMPLETED', label: '已完成' },
]

const filteredTodos = computed(() => {
  if (filter.value === 'ALL') return todos.value
  return todos.value.filter((t) => t.status === filter.value)
})

const stats = computed(() => {
  const pending = todos.value.filter((t) => t.status === 'PENDING')
  const minutes = pending.reduce((sum, t) => sum + (t.estimatedMinutes || 0), 0)
  return {
    pending: pending.length,
    completed: todos.value.length - pending.length,
    minutes,
  }
})

const headline = computed(() => {
  const g = greeting()
  const pending = stats.value.pending
  if (pending === 0) return `${g}，今日待办已经清空`
  return `${g}，还有 ${pending} 件事在等你`
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    todos.value = await listTodos()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function onToggle(todo) {
  if (todo.status === 'COMPLETED') return
  try {
    await completeTodo(todo.id)
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function onRemove(todo) {
  if (!window.confirm(`确定删除「${todo.title}」吗？`)) return
  try {
    await deleteTodo(todo.id)
    if (editing.value?.id === todo.id) editing.value = null
    await load()
  } catch (e) {
    error.value = e.message
  }
}

function startEdit(todo) {
  editing.value = todo
}

onMounted(load)
</script>

<template>
  <header class="page-head">
    <div>
      <h1 class="page-title"><em>{{ headline }}</em></h1>
      <p class="chat-hint" style="text-align: left; padding: 8px 0 0">
        已完成 {{ stats.completed }} 项 · 待投入 {{ stats.minutes }} 分钟
      </p>
    </div>
  </header>

  <div v-if="error" class="form-error">{{ error }}</div>

  <TodoForm :editing="editing" @saved="load" />

  <div class="toolbar">
    <button v-for="f in filters" :key="f.key" class="tab" :class="{ active: filter === f.key }" @click="filter = f.key">
      {{ f.label }}<span class="count">{{ f.key === 'ALL' ? todos.length : stats[f.key === 'PENDING' ? 'pending' : 'completed'] }}</span>
    </button>
    <span class="spacer"></span>
  </div>

  <div v-if="loading" class="empty">
    <span class="empty-glyph">· · ·</span>
    <p>正在整理今日清单…</p>
  </div>

  <div v-else-if="filteredTodos.length" class="todo-list">
    <TodoItem
      v-for="(todo, i) in filteredTodos"
      :key="todo.id"
      :todo="todo"
      :style="{ animationDelay: `${i * 0.04}s` }"
      @toggle="onToggle"
      @edit="startEdit"
      @remove="onRemove"
    />
  </div>

  <div v-else class="empty">
    <span class="empty-glyph">☕</span>
    <p>{{ filter === 'COMPLETED' ? '还没有完成过的事项' : '此刻一片清净，值得庆祝' }}</p>
  </div>
</template>
