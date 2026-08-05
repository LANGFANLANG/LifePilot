<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchCaptcha, login, register } from '../api/auth'
import { setToken } from '../api/http'

const route = useRoute()
const router = useRouter()

const mode = ref('login')
const loading = ref(false)
const error = ref('')
const captchaId = ref('')
const captchaImg = ref('')

const form = reactive({
  username: '',
  password: '',
  displayName: '',
  captchaCode: '',
})

async function refreshCaptcha() {
  try {
    const captcha = await fetchCaptcha()
    captchaId.value = captcha.captchaId
    captchaImg.value = captcha.imageBase64
    form.captchaCode = ''
  } catch (e) {
    error.value = e.message
  }
}

function switchMode(next) {
  mode.value = next
  error.value = ''
  form.captchaCode = ''
}

async function submit() {
  const username = form.username.trim()
  const password = form.password
  if (!username || !password) {
    error.value = '请输入用户名和密码'
    return
  }
  if (!form.captchaCode.trim()) {
    error.value = '请输入验证码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const payload = {
      username,
      password,
      captchaId: captchaId.value,
      captchaCode: form.captchaCode.trim(),
    }
    const result =
      mode.value === 'register'
        ? await register({ ...payload, displayName: form.displayName.trim() || null })
        : await login(payload)
    if (mode.value === 'register') {
      switchMode('login')
      refreshCaptcha()
      return
    }
    setToken(result.token)
    router.replace(typeof route.query.redirect === 'string' ? route.query.redirect : '/')
  } catch (e) {
    error.value = e.message
    if (e.code !== 'UNAUTHORIZED') refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(refreshCaptcha)
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-brand">
        <div class="brand-mark">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 15V8l5 6 3-6v10" />
          </svg>
        </div>
        <h1>LifePilot</h1>
        <p>生活掌舵</p>
      </div>

      <div class="login-tabs">
        <button class="tab" :class="{ active: mode === 'login' }" @click="switchMode('login')">登录</button>
        <button class="tab" :class="{ active: mode === 'register' }" @click="switchMode('register')">注册</button>
      </div>

      <div v-if="error" class="form-error">{{ error }}</div>

      <form class="login-form" @submit.prevent="submit">
        <div class="field">
          <label for="login-username">用户名</label>
          <input id="login-username" v-model="form.username" maxlength="64" autocomplete="username" placeholder="你的登录账号" autofocus />
        </div>

        <div v-if="mode === 'register'" class="field">
          <label for="login-display">显示名称</label>
          <input id="login-display" v-model="form.displayName" maxlength="64" placeholder="展示给别人的名字（可选）" />
        </div>

        <div class="field">
          <label for="login-password">密码</label>
          <input id="login-password" v-model="form.password" type="password" autocomplete="current-password" placeholder="至少 6 位" />
        </div>

        <div class="field">
          <label for="login-captcha">验证码</label>
          <div class="captcha-row">
            <input id="login-captcha" v-model="form.captchaCode" maxlength="6" placeholder="计算结果" />
            <button type="button" class="captcha-img" title="点击刷新" @click="refreshCaptcha">
              <img v-if="captchaImg" :src="captchaImg" alt="验证码" />
              <span v-else>加载中…</span>
            </button>
          </div>
        </div>

        <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
          {{ loading ? '请稍候…' : mode === 'login' ? '登 录' : '注 册' }}
        </button>
      </form>

      <p class="login-foot">—— 掌舵今日，也掌舵远方。</p>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px 16px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: var(--surface);
  border: 1px solid var(--hairline);
  border-radius: var(--radius);
  box-shadow: var(--shadow-lg);
  padding: 40px 36px 28px;
}

.login-brand {
  text-align: center;
  margin-bottom: 24px;
}

.login-brand .brand-mark {
  width: 52px;
  height: 52px;
  margin: 0 auto 12px;
  border-radius: 16px;
  background: var(--accent);
  color: #fff;
  display: grid;
  place-items: center;
}

.login-brand .brand-mark svg {
  width: 26px;
  height: 26px;
}

.login-brand h1 {
  font-size: 26px;
  color: var(--ink);
}

.login-brand p {
  margin: 6px 0 0;
  color: var(--muted);
  font-size: 13px;
  letter-spacing: 0.12em;
}

.login-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.login-tabs .tab {
  flex: 1;
}

.captcha-row {
  display: flex;
  gap: 10px;
  align-items: stretch;
}

.captcha-row input {
  flex: 1;
}

.captcha-img {
  width: 112px;
  height: 42px;
  border: 1px solid var(--hairline);
  border-radius: var(--radius-sm);
  background: var(--surface-2);
  overflow: hidden;
  padding: 0;
  display: grid;
  place-items: center;
  color: var(--muted);
  font-size: 12px;
}

.captcha-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.btn-block {
  width: 100%;
  margin-top: 8px;
}

.login-foot {
  margin: 22px 0 0;
  text-align: center;
  color: var(--muted);
  font-size: 12px;
  letter-spacing: 0.06em;
}
</style>
