import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import 'element-plus/dist/index.css' // Import Element Plus styles if not auto-imported correctly or for reset
import '@/assets/styles/main.scss' // Global styles

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
