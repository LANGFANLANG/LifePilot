import { createRouter, createWebHistory } from 'vue-router'
import TodosView from '../views/TodosView.vue'
import NotesView from '../views/NotesView.vue'
import ChatView from '../views/ChatView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'todos', component: TodosView, meta: { title: '待办' } },
    { path: '/notes', name: 'notes', component: NotesView, meta: { title: '笔记' } },
    { path: '/chat', name: 'chat', component: ChatView, meta: { title: 'AI 助手' } },
  ],
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · LifePilot` : 'LifePilot'
})

export default router
