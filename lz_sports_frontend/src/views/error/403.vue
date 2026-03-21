<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">403</div>
      <div class="error-graphic">
        <svg viewBox="0 0 200 140" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path
            d="M100 20 L148 42 L148 80 C148 108 100 125 100 125 C100 125 52 108 52 80 L52 42 Z"
            fill="rgba(239,68,68,0.1)"
            stroke="#ef4444"
            stroke-width="2"
          />
          <line x1="82" y1="62" x2="118" y2="98" stroke="#ef4444" stroke-width="3" stroke-linecap="round" />
          <line x1="118" y1="62" x2="82" y2="98" stroke="#ef4444" stroke-width="3" stroke-linecap="round" />
          <circle cx="38" cy="40" r="3" fill="rgba(239,68,68,0.25)" />
          <circle cx="165" cy="70" r="4" fill="rgba(239,68,68,0.2)" />
          <circle cx="42" cy="100" r="2" fill="rgba(239,68,68,0.2)" />
          <line x1="155" y1="30" x2="165" y2="30" stroke="rgba(239,68,68,0.35)" stroke-width="1.5" stroke-linecap="round" />
        </svg>
      </div>
      <h2 class="error-title">禁止访问</h2>
      <p class="error-desc">您没有访问该页面的权限。如有疑问，请联系管理员。</p>
      <div class="error-actions">
        <button class="btn-primary btn-danger" @click="goBack">返回上一页</button>
        <button class="btn-ghost" @click="goHome">返回首页</button>
      </div>
    </div>
    <div class="error-bg">
      <div class="bg-num bg-num-danger">403</div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.back()
}

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
@import './error-shared.css';

.error-code {
  color: #ef4444;
}

.bg-num-danger {
  color: rgba(239, 68, 68, 0.04);
}

.btn-primary.btn-danger {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  box-shadow: 0 4px 16px rgba(239, 68, 68, 0.3);
}

.btn-primary.btn-danger:hover {
  box-shadow: 0 8px 24px rgba(239, 68, 68, 0.4);
}
</style>
