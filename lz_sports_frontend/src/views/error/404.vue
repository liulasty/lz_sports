<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">404</div>
      <div class="error-graphic">
        <svg viewBox="0 0 200 140" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="100" cy="70" r="38" fill="rgba(59,130,246,0.12)" stroke="#3b82f6" stroke-width="2" />
          <circle cx="86" cy="62" r="4" fill="#3b82f6" />
          <circle cx="114" cy="62" r="4" fill="#3b82f6" />
          <path d="M84 86C89 79 95 76 100 76C105 76 111 79 116 86" stroke="#3b82f6" stroke-width="2" stroke-linecap="round" />
          <line x1="45" y1="45" x2="55" y2="45" stroke="rgba(59,130,246,0.35)" stroke-width="1.5" stroke-linecap="round" />
          <line x1="145" y1="97" x2="155" y2="97" stroke="rgba(59,130,246,0.35)" stroke-width="1.5" stroke-linecap="round" />
          <circle cx="42" cy="95" r="3" fill="rgba(59,130,246,0.2)" />
          <circle cx="160" cy="40" r="4" fill="rgba(59,130,246,0.2)" />
        </svg>
      </div>
      <h2 class="error-title">页面不存在</h2>
      <p class="error-desc">您访问的页面可能已被删除、重命名或暂时不可用。</p>
      <div class="error-actions">
        <button class="btn-primary btn-blue" @click="goHome">返回首页</button>
        <button class="btn-ghost" @click="goBack">返回上一页</button>
      </div>
    </div>
    <div class="error-bg">
      <div class="bg-num bg-num-blue">404</div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
@import './error-shared.css';

.error-code {
  color: #3b82f6;
}

.bg-num-blue {
  color: rgba(59, 130, 246, 0.04);
}

.btn-primary.btn-blue {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.3);
}

.btn-primary.btn-blue:hover {
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.4);
}
</style>
