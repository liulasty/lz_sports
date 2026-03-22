<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">500</div>
      <div class="error-graphic">
        <svg viewBox="0 0 200 140" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect x="55" y="30" width="90" height="22" rx="4" fill="rgba(99,102,241,0.12)" stroke="#6366f1" stroke-width="1.5" />
          <rect x="55" y="59" width="90" height="22" rx="4" fill="rgba(99,102,241,0.12)" stroke="#6366f1" stroke-width="1.5" />
          <rect x="55" y="88" width="90" height="22" rx="4" fill="rgba(99,102,241,0.12)" stroke="#6366f1" stroke-width="1.5" />
          <circle cx="72" cy="41" r="3" fill="#6366f1" opacity="0.7" />
          <circle cx="84" cy="41" r="3" fill="#6366f1" opacity="0.4" />
          <circle cx="72" cy="70" r="3" fill="#ef4444" />
          <circle cx="84" cy="70" r="3" fill="#6366f1" opacity="0.4" />
          <circle cx="72" cy="99" r="3" fill="#6366f1" opacity="0.7" />
          <circle cx="84" cy="99" r="3" fill="#6366f1" opacity="0.4" />
          <path d="M108 64 L103 73 H107 L102 81 L113 70 H109 Z" fill="#ef4444" />
          <path d="M168 50 L175 58 L165 66" stroke="rgba(99,102,241,0.4)" stroke-width="1.5" stroke-linecap="round" fill="none" />
          <circle cx="38" cy="60" r="3" fill="rgba(99,102,241,0.2)" />
        </svg>
      </div>
      <h2 class="error-title">服务器错误</h2>
      <p class="error-desc">服务器遇到了一些问题，我们正在紧急处理中，请稍后重试。</p>
      <div class="error-actions">
        <button class="btn-primary btn-indigo" @click="retry">重新加载</button>
        <button class="btn-ghost" @click="goHome">返回首页</button>
      </div>
      <div class="error-hint">
        如问题持续存在，请
        <a href="mailto:support@lzsports.com" class="hint-link">联系技术支持</a>
      </div>
    </div>
    <div class="error-bg">
      <div class="bg-num bg-num-indigo">500</div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const retry = () => {
  router.go(0)
}

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
@import './error-shared.css';

.error-code {
  color: #6366f1;
}

.bg-num-indigo {
  color: rgba(99, 102, 241, 0.04);
}

.btn-primary.btn-indigo {
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.3);
}

.btn-primary.btn-indigo:hover {
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.4);
}

.error-hint {
  margin-top: 20px;
  font-size: 12px;
  color: var(--text-secondary, var(--el-text-color-secondary));
}

.hint-link {
  color: #6366f1;
  text-decoration: none;
}

.hint-link:hover {
  text-decoration: underline;
}
</style>
