<script setup>
import { formatDateTime } from '../utils/format'

const props = defineProps({
  todo: { type: Object, required: true },
})

const emit = defineEmits(['toggle', 'edit', 'remove'])

const priorityLabels = { HIGH: '高优先级', MEDIUM: '中优先级', LOW: '低优先级' }
</script>

<template>
  <article class="card todo-item" :class="{ done: todo.status === 'COMPLETED' }">
    <input
      class="todo-check"
      type="checkbox"
      :checked="todo.status === 'COMPLETED'"
      :aria-label="todo.title"
      @change="emit('toggle', todo)"
    />

    <div class="todo-body">
      <div class="todo-title">{{ todo.title }}</div>
      <p v-if="todo.description" class="todo-desc">{{ todo.description }}</p>

      <div class="todo-meta">
        <span v-if="todo.priority" class="chip" :class="`priority-${todo.priority}`">{{ priorityLabels[todo.priority] }}</span>
        <span v-if="todo.category" class="chip">{{ todo.category }}</span>
        <span v-if="todo.dueAt" class="chip">{{ formatDateTime(todo.dueAt) }}</span>
        <span v-if="todo.estimatedMinutes" class="chip">{{ todo.estimatedMinutes }} 分钟</span>
      </div>
    </div>

    <div class="todo-actions">
      <button class="icon-btn" :aria-label="`编辑 ${todo.title}`" @click="emit('edit', todo)">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 20h9M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4Z" />
        </svg>
      </button>
      <button class="icon-btn danger" :aria-label="`删除 ${todo.title}`" @click="emit('remove', todo)">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 6h18M8 6V4a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1v2m3 0v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6M10 11v6M14 11v6" />
        </svg>
      </button>
    </div>
  </article>
</template>
