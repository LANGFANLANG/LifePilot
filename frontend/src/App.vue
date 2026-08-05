<script setup>
import { RouterLink, RouterView } from 'vue-router'
import { computed } from 'vue'

const navItems = [
  { to: '/', label: '待办', index: '01' },
  { to: '/notes', label: '笔记', index: '02' },
  { to: '/chat', label: 'AI 助手', index: '03' },
]

const dateLine = computed(() => {
  const now = new Date()
  const week = ['日', '一', '二', '三', '四', '五', '六'][now.getDay()]
  const month = `${now.getMonth() + 1}`.padStart(2, '0')
  return `${now.getFullYear()} 年 ${month} 月 ${now.getDate()} 日 · 周${week}`
})
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 15V8l5 6 3-6v10" />
          </svg>
        </div>
        <div>
          <div class="brand-name">LifePilot</div>
          <div class="brand-sub">生活掌舵</div>
        </div>
      </div>

      <nav class="nav">
        <div class="nav-label">Navigation</div>
        <RouterLink v-for="item in navItems" :key="item.to" :to="item.to" class="nav-link">
          <span class="nav-index">{{ item.index }}</span>
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>

      <div class="sidebar-foot">—— 掌舵今日，也掌舵远方。</div>
    </aside>

    <main class="main">
      <RouterView />
    </main>
  </div>
</template>
