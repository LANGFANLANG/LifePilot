import { createRouter, createWebHistory } from 'vue-router'
import TodosView from '../views/TodosView.vue'
import NotesView from '../views/NotesView.vue'
import ChatView from '../views/ChatView.vue'
import LoginView from '../views/LoginView.vue'
import { getToken } from '../api/http'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: LoginView, meta: { title: '登录', public: true } },
    { path: '/', name: 'todos', component: TodosView, meta: { title: '待办' } },
    { path: '/notes', name: 'notes', component: NotesView, meta: { title: '笔记' } },
    { path: '/chat', name: 'chat', component: ChatView, meta: { title: 'AI 助手' } },
  ],
})

router.beforeEach((to) => {
  if (!to.meta.public && !getToken()) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && getToken()) {
    return { name: 'todos' }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · LifePilot` : 'LifePilot'
})

export default router
