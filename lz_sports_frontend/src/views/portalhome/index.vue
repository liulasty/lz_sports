<template>
  <div class="portal-container" ref="containerRef" @scroll="handleScroll">
    <!-- ── Animated Background ── -->
    <div class="bg-layer">
      <div class="bg-circle c1"></div>
      <div class="bg-circle c2"></div>
      <div class="bg-circle c3"></div>
      <div class="bg-grid"></div>
    </div>

    <!-- ── Navigation Bar ── -->
    <nav class="navbar">
      <div class="nav-inner">
        <div class="nav-brand">
          <img v-if="configStore.logoUrl" :src="configStore.logoUrl" class="nav-logo" alt="Logo" />
          <div v-else class="nav-logo-icon">
            <svg viewBox="0 0 32 32" fill="none"><circle cx="16" cy="16" r="14" stroke="#FF6B35" stroke-width="2"/><path d="M9 16 Q16 8 23 16 Q16 24 9 16Z" fill="#FF6B35" opacity="0.9"/></svg>
          </div>
          <span class="nav-name">{{ configStore.schoolName || 'LZ Sports' }}</span>
        </div>
        <div class="nav-links">
          <a
            v-for="item in navItems" :key="item.id"
            class="nav-link"
            :class="{ active: activeSection === item.id }"
            href="#"
            @click.prevent="scrollToSection(item.id)"
          >{{ item.label }}</a>
          <a class="nav-link nav-link-route" href="#" @click.prevent="$router.push('/public-scores')">成绩公示</a>
        </div>
        <div class="nav-actions">
          <template v-if="!userStore.token">
            <button class="nav-btn-outline" @click="$router.push('/login')">登录</button>
            <button class="nav-btn-solid" @click="$router.push('/register')">立即注册</button>
          </template>
          <template v-else>
            <button class="nav-btn-solid" @click="$router.push('/dashboard')">进入后台</button>
          </template>
        </div>
        <button class="nav-mobile-toggle" @click="mobileMenuOpen = !mobileMenuOpen">
          <svg viewBox="0 0 24 24" fill="none"><path d="M3 12h18M3 6h18M3 18h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
        </button>
      </div>
      <div class="mobile-menu" :class="{ open: mobileMenuOpen }">
        <a v-for="item in navItems" :key="item.id" class="mobile-link" :class="{ active: activeSection === item.id }" href="#" @click.prevent="scrollToSection(item.id); mobileMenuOpen=false">{{ item.label }}</a>
        <a class="mobile-link" href="#" @click.prevent="$router.push('/public-scores'); mobileMenuOpen=false">成绩公示</a>
        <template v-if="!userStore.token">
          <button class="nav-btn-outline w-full" @click="$router.push('/login')">登录</button>
          <button class="nav-btn-solid w-full" @click="$router.push('/register')">立即注册</button>
        </template>
        <template v-else>
          <button class="nav-btn-solid w-full" @click="$router.push('/dashboard')">进入后台</button>
        </template>
      </div>
    </nav>

    <!-- ── Hero Section ── -->
    <section class="hero" id="home" ref="sectionHome">
      <div class="hero-content">
        <div class="hero-badge">
          <span class="badge-dot"></span>
          SCHOOL SPORTS MANAGEMENT
        </div>
        <h1 class="hero-title">
          竞技<span class="accent">无界</span><br />
          <span class="hero-title-sub">让每一次拼搏都被记录</span>
        </h1>
        <p class="hero-desc">
          {{ configStore.schoolName || '凌智体育中学' }}官方体育赛事管理平台，<br />
          从报名审核到成绩发布，全流程数字化管理，<br />
          让每一位同学的努力都被看见。
        </p>
        <div class="hero-cta">
          <button v-if="!userStore.token" class="cta-primary" @click="$router.push('/register')">
            免费注册参赛
            <svg viewBox="0 0 24 24" fill="none" class="cta-icon"><path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </button>
          <button class="cta-secondary" @click="scrollToSection('events')">
            <svg viewBox="0 0 24 24" fill="none" class="cta-icon"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/><polygon points="10,8 16,12 10,16" fill="currentColor"/></svg>
            浏览赛事
          </button>
        </div>
        <div class="hero-trust">
          <div class="trust-avatars">
            <div class="trust-avatar" v-for="c in ['张','李','王','陈']" :key="c" :style="{ background: avatarColor(c) }">{{ c }}</div>
          </div>
          <span class="trust-text">全校 <strong>1,200+</strong> 名同学已参与</span>
        </div>
      </div>
      <div class="hero-visual">
        <div class="visual-card main-card-vis">
          <div class="vis-header">
            <div class="vis-dots"><span></span><span></span><span></span></div>
            <span class="vis-title">春季运动会 · 实时成绩榜</span>
          </div>
          <div class="vis-rows">
            <div class="vis-row" v-for="(r, i) in leaderboard" :key="i" :class="{ highlight: i === 0 }">
              <div class="vis-rank" :class="rankClass(i+1)">{{ i+1 }}</div>
              <div class="vis-ath">
                <div class="vis-avatar" :style="{ background: avatarColor(r.name) }">{{ r.name.charAt(0) }}</div>
                <div>
                  <div class="vis-name">{{ r.name }}</div>
                  <div class="vis-event">{{ r.event }}</div>
                </div>
              </div>
              <div class="vis-score">{{ r.score }}</div>
              <div class="vis-bar-wrap"><div class="vis-bar" :style="{ width: r.pct + '%' }"></div></div>
            </div>
          </div>
        </div>
        <div class="visual-card side-card-vis">
          <div class="side-label">本学期赛事</div>
          <div class="side-num">8</div>
          <div class="side-sub">场赛事进行中</div>
          <div class="side-progress">
            <div class="side-prog-fill" style="width: 72%"></div>
          </div>
          <div class="side-meta">平均报名率 72%</div>
        </div>
        <div class="visual-card mini-card-vis">
          <svg viewBox="0 0 24 24" fill="none" class="mini-icon"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="#FF6B35" stroke-width="2" stroke-linecap="round"/><polyline points="22 4 12 14.01 9 11.01" stroke="#FF6B35" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          <div class="mini-text">成绩已发布</div>
          <div class="mini-num">+86</div>
        </div>
        <div class="deco-ring r1"></div>
        <div class="deco-ring r2"></div>
      </div>
    </section>

    <!-- ── Events Section ── -->
    <section class="events-section" id="events" ref="sectionEvents">
      <div class="section-header">
        <div class="section-eyebrow">LIVE EVENTS</div>
        <h2 class="section-title">正在报名的赛事</h2>
        <p class="section-sub">第一时间掌握赛事动态，错过报名截止时间将无法参赛</p>
      </div>
      <div class="events-grid">
        <div class="event-card" v-for="(ev, i) in activeEvents" :key="i">
          <div class="event-card-top">
            <div class="event-status-dot" :class="ev.status === 'OPEN' ? 'live' : 'reg'"></div>
            <span class="event-status-text">{{ ev.status === 'OPEN' ? '报名中' : '即将开始' }}</span>
            <span class="event-deadline">截止 {{ ev.deadline }}</span>
          </div>
          <div class="event-card-body">
            <div class="event-category">{{ ev.category }}</div>
            <h3 class="event-name">{{ ev.name }}</h3>
            <div class="event-meta">
              <div class="event-meta-item">
                <svg viewBox="0 0 24 24" fill="none" class="meta-icon"><rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="1.5"/><path d="M3 9H21M8 2V5M16 2V5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
                {{ ev.date }}
              </div>
              <div class="event-meta-item">
                <svg viewBox="0 0 24 24" fill="none" class="meta-icon"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/><circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="1.5"/></svg>
                {{ ev.count }} 人已报名 / 限 {{ ev.max }} 人
              </div>
            </div>
            <div class="event-progress-wrap">
              <div class="event-progress-bar">
                <div class="event-progress-fill" :style="{ width: ev.pct + '%' }" :class="ev.pct > 85 ? 'prog-hot' : ''"></div>
              </div>
              <span class="event-pct" :style="{ color: ev.pct > 85 ? '#ef4444' : 'var(--accent)' }">{{ ev.pct }}%</span>
            </div>
            <div v-if="ev.pct > 85" class="event-hot-tip">🔥 即将报满</div>
          </div>
          <div class="event-card-footer">
            <button class="event-btn" @click="$router.push(userStore.token ? '/event' : '/login')">查看详情 →</button>
          </div>
        </div>
      </div>
    </section>

    <!-- ── Feature Grid ── -->
    <section class="features-section" id="features" ref="sectionFeatures">
      <div class="section-header">
        <div class="section-eyebrow">PLATFORM FEATURES</div>
        <h2 class="section-title">为师生设计的每个功能</h2>
        <p class="section-sub">无论是运动员报名还是裁判录入成绩，都做到了极简易用</p>
      </div>
      <div class="features-grid">
        <div class="feature-card" v-for="(f, i) in features" :key="i" :class="{ featured: f.featured }">
          <div class="feature-icon-wrap" :style="{ background: f.iconBg }">
            <div v-html="f.icon" class="feature-icon-svg"></div>
          </div>
          <h3 class="feature-title">{{ f.title }}</h3>
          <p class="feature-desc">{{ f.desc }}</p>
          <div class="feature-tag">{{ f.tag }}</div>
        </div>
      </div>
    </section>

    <!-- ── Stats Band ── -->
    <section class="stats-band" id="stats" ref="sectionStats">
      <div class="stats-inner">
        <div class="stat-block" v-for="s in statsData" :key="s.label">
          <div class="stat-num-big">{{ s.value }}</div>
          <div class="stat-label-big">{{ s.label }}</div>
          <div class="stat-desc-big">{{ s.desc }}</div>
        </div>
      </div>
    </section>

    <!-- ── CTA Banner ── -->
    <section class="cta-banner">
      <div class="cta-banner-inner">
        <div class="cta-deco-ring"></div>
        <div class="cta-content">
          <div class="section-eyebrow">GET STARTED</div>
          <h2 class="cta-title">准备好出发了吗？</h2>
          <p class="cta-sub">注册账号，提交运动员认证，即可报名参加学校各类体育赛事</p>
          <div class="cta-btns">
            <template v-if="!userStore.token">
              <button class="cta-primary" @click="$router.push('/register')">
                注册账号
                <svg viewBox="0 0 24 24" fill="none" class="cta-icon"><path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </button>
              <button class="cta-secondary" @click="$router.push('/login')">已有账号，去登录</button>
            </template>
            <template v-else>
              <button class="cta-primary" @click="$router.push('/dashboard')">
                进入工作台
                <svg viewBox="0 0 24 24" fill="none" class="cta-icon"><path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </button>
            </template>
          </div>
        </div>
      </div>
    </section>

    <!-- ── Footer ── -->
    <footer class="site-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <div class="footer-logo">
            <svg viewBox="0 0 32 32" fill="none"><circle cx="16" cy="16" r="14" stroke="#FF6B35" stroke-width="2"/><path d="M9 16 Q16 8 23 16 Q16 24 9 16Z" fill="#FF6B35" opacity="0.9"/></svg>
          </div>
          <span class="footer-name">{{ configStore.schoolName || 'LZ Sports' }}</span>
          <p class="footer-tagline">校园体育管理，竞技无界</p>
        </div>
        <div class="footer-copy">© {{ new Date().getFullYear() }} {{ configStore.schoolName || 'LZ Sports' }} 体育管理平台. All rights reserved.</div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useConfigStore } from '@/stores/config'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const configStore = useConfigStore()
const userStore = useUserStore()

const mobileMenuOpen = ref(false)
const activeSection = ref('home')

// ── Template refs for each section ──────────────
const containerRef  = ref(null)
const sectionHome   = ref(null)
const sectionEvents = ref(null)
const sectionFeatures = ref(null)
const sectionStats  = ref(null)

const navItems = [
  { id: 'home',     label: '首页' },
  { id: 'events',   label: '赛事' },
  { id: 'features', label: '功能' },
  { id: 'stats',    label: '数据' },
]

// Map section id → ref
const sectionRefs = {
  home:     sectionHome,
  events:   sectionEvents,
  features: sectionFeatures,
  stats:    sectionStats,
}

// ── Scroll to section ────────────────────────────
const NAVBAR_H = 68

const scrollToSection = async (id) => {
  mobileMenuOpen.value = false
  activeSection.value = id

  await nextTick()

  const container = containerRef.value
  if (!container) return

  if (id === 'home') {
    container.scrollTo({ top: 0, behavior: 'smooth' })
    return
  }

  const el = sectionRefs[id]?.value
  if (!el) return

  // Need to account for the element's top position relative to the container's scroll content
  // Since container has position relative, offsetTop gives position relative to the container's first relatively positioned parent
  // A safer approach is to use getBoundingClientRect
  const containerRect = container.getBoundingClientRect()
  const elRect = el.getBoundingClientRect()
  const targetTop = elRect.top - containerRect.top + container.scrollTop - NAVBAR_H
  
  container.scrollTo({ top: targetTop, behavior: 'smooth' })
}

// ── Update active nav on scroll ─────────────────
const handleScroll = () => {
  const container = containerRef.value
  if (!container) return

  const scrollTop = container.scrollTop + NAVBAR_H + 80 // offset + lookahead

  const containerRect = container.getBoundingClientRect()

  const order = ['stats', 'features', 'events', 'home']
  for (const id of order) {
    const el = sectionRefs[id]?.value
    if (el) {
      const elRect = el.getBoundingClientRect()
      // Calculate top position relative to container's scrollable content
      const elTop = elRect.top - containerRect.top + container.scrollTop
      if (elTop <= scrollTop) {
        activeSection.value = id
        return
      }
    }
  }
  activeSection.value = 'home'
}

onMounted(() => {
  // containerRef is the scroll host — listener attached via template @scroll
})

// ── Static data ─────────────────────────────────
const avatarColor = (str) => {
  const colors = ['#FF6B35', '#6366f1', '#10b981', '#f59e0b', '#ec4899', '#06b6d4']
  let hash = 0
  for (let i = 0; i < str.length; i++) hash = str.charCodeAt(i) + ((hash << 5) - hash)
  return colors[Math.abs(hash) % colors.length]
}
const rankClass = (r) => ({ 1: 'gold', 2: 'silver', 3: 'bronze' }[r] || '')

const leaderboard = [
  { name: '李明远', event: '男子100米', score: '11.24s', pct: 96 },
  { name: '张晓雯', event: '女子跳高',  score: '1.72m',  pct: 87 },
  { name: '王志强', event: '男子铅球',  score: '14.6m',  pct: 79 },
  { name: '陈佳怡', event: '女子400米', score: '62.8s',  pct: 71 },
]

const statsData = [
  { value: '1,240', label: '注册学生',  desc: '全校参与体育平台人数' },
  { value: '36',    label: '历届赛事',  desc: '平台上线以来累计举办' },
  { value: '98%',   label: '成绩准时率', desc: '赛事结束当天发布率' },
  { value: '3min',  label: '报名用时',  desc: '平均每位同学报名耗时' },
]

const activeEvents = [
  {
    name: '2026年春季田径运动会',
    category: '田径', date: '4月15日 ~ 4月17日',
    deadline: '4月10日', count: 218, max: 300, pct: 73, status: 'OPEN'
  },
  {
    name: '校园3×3篮球争霸赛',
    category: '篮球', date: '4月22日 ~ 4月23日',
    deadline: '4月18日', count: 88, max: 96, pct: 92, status: 'OPEN'
  },
  {
    name: '高一年级趣味运动会',
    category: '趣味', date: '5月6日',
    deadline: '4月30日', count: 156, max: 240, pct: 65, status: 'REG'
  },
  {
    name: '校际游泳邀请赛选拔',
    category: '游泳', date: '5月12日 ~ 5月13日',
    deadline: '5月8日', count: 34, max: 60, pct: 57, status: 'REG'
  },
]

const features = [
  {
    title: '在线报名与审核',
    desc: '学生自助报名，班主任线上确认，体育组一键批量审核，全程无纸化，告别信息孤岛。',
    tag: '核心功能',
    icon: `<svg viewBox="0 0 24 24" fill="none"><path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2M9 5a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2M9 5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2" stroke="#FF6B35" stroke-width="2" stroke-linecap="round"/><path d="M9 12h6M9 16h4" stroke="#FF6B35" stroke-width="2" stroke-linecap="round"/></svg>`,
    iconBg: 'rgba(255,107,53,0.12)',
    featured: true,
  },
  {
    title: '运动员资格认证',
    desc: '学生提交年级班级信息，管理员审核通过后方可报名参赛，杜绝冒名顶替。',
    tag: '资格管理',
    icon: `<svg viewBox="0 0 24 24" fill="none"><circle cx="9" cy="7" r="4" stroke="#6366f1" stroke-width="2"/><path d="M3 21v-2a4 4 0 0 1 4-4h4a4 4 0 0 1 4 4v2" stroke="#6366f1" stroke-width="2" stroke-linecap="round"/><path d="M16 11l2 2 4-4" stroke="#6366f1" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
    iconBg: 'rgba(99,102,241,0.12)',
    featured: false,
  },
  {
    title: '成绩录入与发布',
    desc: '裁判现场录入，支持 Excel 批量导入，成绩实时上榜，同学扫码即可查看个人成绩单。',
    tag: '成绩管理',
    icon: `<svg viewBox="0 0 24 24" fill="none"><path d="M18 20V10M12 20V4M6 20v-6" stroke="#10b981" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
    iconBg: 'rgba(16,185,129,0.12)',
    featured: false,
  },
  {
    title: '比赛项目灵活配置',
    desc: '每场赛事可自定义项目，支持性别分组、人数上限、独立时间段，适配田径、球类、趣味等各类赛制。',
    tag: '项目配置',
    icon: `<svg viewBox="0 0 24 24" fill="none"><path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" stroke="#f59e0b" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
    iconBg: 'rgba(245,158,11,0.12)',
    featured: false,
  },
  {
    title: '通知与消息推送',
    desc: '报名成功、审核结果、赛程变更等关键节点自动通知，学生不再错过任何重要信息。',
    tag: '消息中心',
    icon: `<svg viewBox="0 0 24 24" fill="none"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" stroke="#ec4899" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M13.73 21a2 2 0 0 1-3.46 0" stroke="#ec4899" stroke-width="2" stroke-linecap="round"/></svg>`,
    iconBg: 'rgba(236,72,153,0.12)',
    featured: false,
  },
  {
    title: '数据统计与分析',
    desc: '赛事报名率、各年级参与度、成绩分布一览，帮助体育组优化每学期的赛事规划。',
    tag: '数据洞察',
    icon: `<svg viewBox="0 0 24 24" fill="none"><rect x="3" y="3" width="18" height="18" rx="2" stroke="#06b6d4" stroke-width="2"/><path d="M3 9h18M9 21V9" stroke="#06b6d4" stroke-width="2" stroke-linecap="round"/></svg>`,
    iconBg: 'rgba(6,182,212,0.12)',
    featured: false,
  },
]
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Noto+Sans+SC:wght@300;400;500;700&display=swap');

* {
  --accent:      #FF6B35;
  --accent-dark: #e0541e;
  --dark:        #0d0f14;
  --dark-2:      #111318;
  --dark-3:      #161921;
  --dark-4:      #1e2130;
  --dark-5:      #252a3a;
  --border:      rgba(255,255,255,0.07);
  --text-1:      #f0f2f8;
  --text-2:      #8892a4;
  --text-3:      #555e72;
}

.portal-container {
  position: relative;
  height: 100vh;
  background-color: var(--dark);
  font-family: 'Noto Sans SC', sans-serif;
  color: var(--text-1);
  overflow-x: hidden;
  overflow-y: auto;
  scroll-behavior: auto; /* let JS handle smooth */
}

/* ── Background ── */
.bg-layer { position: fixed; inset: 0; pointer-events: none; z-index: 0; }
.bg-grid {
  position: absolute; inset: 0;
  background-image:
    linear-gradient(rgba(255,107,53,0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,107,53,0.035) 1px, transparent 1px);
  background-size: 56px 56px;
}
.bg-circle { position: absolute; border-radius: 50%; filter: blur(90px); animation: drift 12s ease-in-out infinite alternate; }
.c1 { width: 600px; height: 600px; background: rgba(255,107,53,0.1); top: -200px; left: -200px; animation-duration: 11s; }
.c2 { width: 400px; height: 400px; background: rgba(99,102,241,0.07); bottom: -120px; right: -120px; animation-duration: 15s; animation-direction: alternate-reverse; }
.c3 { width: 300px; height: 300px; background: rgba(255,107,53,0.05); top: 55%; left: 55%; animation-duration: 20s; }
@keyframes drift { from { transform: translate(0,0) scale(1); } to { transform: translate(40px,25px) scale(1.06); } }

/* ── Navbar ── */
.navbar {
  position: sticky; top: 0; z-index: 100;
  background: rgba(13,15,20,0.82);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border);
}
.nav-inner {
  max-width: 1200px; margin: 0 auto; padding: 0 32px;
  height: 68px; display: flex; align-items: center; gap: 32px;
}
.nav-brand { display: flex; align-items: center; gap: 10px; flex-shrink: 0; }
.nav-logo { height: 32px; object-fit: contain; }
.nav-logo-icon svg { width: 28px; height: 28px; }
.nav-name { font-size: 16px; font-weight: 700; color: var(--text-1); letter-spacing: 0.02em; }
.nav-links { display: flex; gap: 2px; flex: 1; padding-left: 16px; }
.nav-link {
  font-size: 14px; color: var(--text-2); text-decoration: none;
  padding: 6px 14px; border-radius: 8px;
  transition: color 0.2s, background 0.2s;
  font-weight: 500; cursor: pointer;
  position: relative;
}
.nav-link::after {
  content: '';
  position: absolute;
  bottom: 2px; left: 50%; transform: translateX(-50%);
  width: 0; height: 2px;
  background: var(--accent);
  border-radius: 1px;
  transition: width 0.25s cubic-bezier(0.34,1.56,0.64,1);
}
.nav-link:hover { color: var(--text-1); background: rgba(255,255,255,0.05); }
.nav-link.active { color: var(--accent); background: rgba(255,107,53,0.06); }
.nav-link.active::after { width: 60%; }

.nav-actions { display: flex; gap: 10px; align-items: center; }
.nav-btn-outline {
  padding: 7px 18px; border-radius: 8px;
  border: 1px solid rgba(255,255,255,0.15);
  background: transparent; color: var(--text-1);
  font-size: 13px; font-weight: 600; font-family: inherit;
  cursor: pointer; transition: border-color 0.2s, background 0.2s;
}
.nav-btn-outline:hover { border-color: var(--accent); background: rgba(255,107,53,0.06); }
.nav-btn-solid {
  padding: 7px 18px; border-radius: 8px; border: none;
  background: var(--accent); color: #fff;
  font-size: 13px; font-weight: 600; font-family: inherit;
  cursor: pointer; transition: transform 0.15s, box-shadow 0.15s;
  box-shadow: 0 3px 12px rgba(255,107,53,0.32);
}
.nav-btn-solid:hover { transform: translateY(-1px); box-shadow: 0 5px 18px rgba(255,107,53,0.42); }
.nav-mobile-toggle { display: none; background: transparent; border: none; color: var(--text-1); cursor: pointer; padding: 4px; margin-left: auto; }
.nav-mobile-toggle svg { width: 22px; height: 22px; }
.mobile-menu { display: none; flex-direction: column; gap: 8px; padding: 16px 24px 24px; border-top: 1px solid var(--border); }
.mobile-menu.open { display: flex; }
.mobile-link { font-size: 15px; color: var(--text-2); text-decoration: none; padding: 10px 0; border-bottom: 1px solid var(--border); font-weight: 500; transition: color 0.2s; cursor: pointer; }
.mobile-link:hover, .mobile-link.active { color: var(--accent); }
.w-full { width: 100%; text-align: center; }

/* ── Hero ── */
.hero {
  position: relative; z-index: 1;
  max-width: 1200px; margin: 0 auto;
  padding: 100px 32px 80px;
  display: grid; grid-template-columns: 1fr 1fr; gap: 64px; align-items: center;
  scroll-margin-top: 68px;
}
.hero-content { display: flex; flex-direction: column; }
.hero-badge {
  display: inline-flex; align-items: center; gap: 8px;
  font-size: 10px; font-weight: 700; letter-spacing: 0.18em; color: var(--accent);
  border: 1px solid rgba(255,107,53,0.3); padding: 5px 14px; border-radius: 20px;
  margin-bottom: 28px; background: rgba(255,107,53,0.06); width: fit-content;
}
.badge-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--accent); animation: pulse-dot 1.5s ease-in-out infinite; }
@keyframes pulse-dot { 0%,100% { opacity:1;transform:scale(1); } 50% { opacity:0.5;transform:scale(0.8); } }
.hero-title { font-family: 'Bebas Neue','Noto Sans SC',sans-serif; font-size: 80px; line-height: 0.95; color: var(--text-1); margin: 0 0 8px; letter-spacing: 0.02em; animation: fadeUp 0.7s cubic-bezier(0.22,1,0.36,1) both; }
.hero-title .accent { color: var(--accent); }
.hero-title-sub { font-family: 'Noto Sans SC',sans-serif; font-size: 24px; font-weight: 300; letter-spacing: 0.1em; color: var(--text-2); display: block; margin-top: 8px; }
.hero-desc { font-size: 14px; color: var(--text-2); line-height: 2; margin: 24px 0 36px; animation: fadeUp 0.7s 0.1s cubic-bezier(0.22,1,0.36,1) both; }
.hero-cta { display: flex; gap: 14px; flex-wrap: wrap; margin-bottom: 32px; animation: fadeUp 0.7s 0.2s cubic-bezier(0.22,1,0.36,1) both; }
.cta-primary {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 13px 28px; border-radius: 10px; border: none;
  background: linear-gradient(135deg, var(--accent), var(--accent-dark));
  color: #fff; font-size: 14px; font-weight: 700; font-family: inherit;
  letter-spacing: 0.04em; cursor: pointer;
  box-shadow: 0 6px 24px rgba(255,107,53,0.38);
  transition: transform 0.18s, box-shadow 0.18s; position: relative; overflow: hidden;
}
.cta-primary::before { content: ''; position: absolute; inset: 0; background: linear-gradient(135deg,rgba(255,255,255,0.15) 0%,transparent 60%); pointer-events: none; }
.cta-primary:hover { transform: translateY(-2px); box-shadow: 0 10px 32px rgba(255,107,53,0.48); }
.cta-secondary {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 13px 22px; border-radius: 10px;
  border: 1px solid rgba(255,255,255,0.12); background: rgba(255,255,255,0.04);
  color: var(--text-1); font-size: 14px; font-weight: 600; font-family: inherit;
  cursor: pointer; transition: background 0.2s, border-color 0.2s, transform 0.18s;
}
.cta-secondary:hover { background: rgba(255,255,255,0.08); border-color: rgba(255,255,255,0.22); transform: translateY(-1px); }
.cta-icon { width: 16px; height: 16px; }
.hero-trust { display: flex; align-items: center; gap: 12px; animation: fadeUp 0.7s 0.3s cubic-bezier(0.22,1,0.36,1) both; }
.trust-avatars { display: flex; }
.trust-avatar { width: 30px; height: 30px; border-radius: 50%; border: 2px solid var(--dark); margin-left: -8px; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 700; color: #fff; }
.trust-avatar:first-child { margin-left: 0; }
.trust-text { font-size: 13px; color: var(--text-2); }
.trust-text strong { color: var(--text-1); }
@keyframes fadeUp { from { opacity:0;transform:translateY(20px); } to { opacity:1;transform:translateY(0); } }

/* ── Hero Visual ── */
.hero-visual { position: relative; height: 480px; animation: fadeUp 0.7s 0.15s cubic-bezier(0.22,1,0.36,1) both; }
.visual-card { position: absolute; background: rgba(22,25,33,0.9); border: 1px solid var(--border); border-radius: 16px; backdrop-filter: blur(12px); }
.main-card-vis { top: 0; left: 0; right: 60px; padding: 20px; }
.vis-header { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
.vis-dots { display: flex; gap: 5px; }
.vis-dots span { width: 8px; height: 8px; border-radius: 50%; background: var(--dark-5); }
.vis-dots span:first-child { background: #ef4444; }
.vis-dots span:nth-child(2) { background: #f59e0b; }
.vis-dots span:last-child { background: #10b981; }
.vis-title { font-size: 12px; font-weight: 600; color: var(--text-2); letter-spacing: 0.06em; }
.vis-rows { display: flex; flex-direction: column; gap: 10px; }
.vis-row { display: grid; grid-template-columns: 28px 1fr 60px 80px; align-items: center; gap: 10px; padding: 10px 12px; border-radius: 10px; transition: background 0.2s; }
.vis-row:hover { background: rgba(255,255,255,0.04); }
.vis-row.highlight { background: rgba(255,107,53,0.07); border: 1px solid rgba(255,107,53,0.15); }
.vis-rank { width: 22px; height: 22px; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 800; background: var(--dark-5); color: var(--text-2); }
.vis-rank.gold   { background: rgba(245,158,11,0.2); color: #f59e0b; }
.vis-rank.silver { background: rgba(148,163,184,0.2); color: #94a3b8; }
.vis-rank.bronze { background: rgba(180,83,9,0.2); color: #b45309; }
.vis-ath { display: flex; align-items: center; gap: 8px; min-width: 0; }
.vis-avatar { width: 26px; height: 26px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 700; color: #fff; flex-shrink: 0; }
.vis-name { font-size: 13px; font-weight: 600; color: var(--text-1); }
.vis-event { font-size: 11px; color: var(--text-3); }
.vis-score { font-size: 13px; font-weight: 700; color: var(--accent); text-align: right; font-variant-numeric: tabular-nums; }
.vis-bar-wrap { height: 4px; background: var(--dark-5); border-radius: 2px; overflow: hidden; }
.vis-bar { height: 100%; background: linear-gradient(90deg, var(--accent), #ff9a6c); border-radius: 2px; }
.side-card-vis { bottom: 40px; right: 0; width: 150px; padding: 18px; display: flex; flex-direction: column; gap: 4px; }
.side-label { font-size: 10px; font-weight: 700; letter-spacing: 0.12em; color: var(--text-3); text-transform: uppercase; }
.side-num { font-family: 'Bebas Neue', sans-serif; font-size: 42px; color: var(--accent); line-height: 1; margin: 4px 0 2px; }
.side-sub { font-size: 11px; color: var(--text-2); }
.side-progress { height: 4px; background: var(--dark-5); border-radius: 2px; overflow: hidden; margin-top: 10px; }
.side-prog-fill { height: 100%; background: linear-gradient(90deg, var(--accent), #ff9a6c); border-radius: 2px; }
.side-meta { font-size: 10px; color: var(--text-3); margin-top: 4px; }
.mini-card-vis { top: 200px; right: 10px; width: 130px; padding: 14px 16px; display: flex; flex-direction: column; gap: 3px; }
.mini-icon { width: 24px; height: 24px; margin-bottom: 4px; }
.mini-text { font-size: 11px; color: var(--text-2); }
.mini-num { font-family: 'Bebas Neue', sans-serif; font-size: 28px; color: #10b981; line-height: 1; }
.deco-ring { position: absolute; border-radius: 50%; border: 1px solid rgba(255,107,53,0.12); pointer-events: none; }
.r1 { width: 320px; height: 320px; top: 50%; left: 50%; transform: translate(-50%,-50%); animation: spin-slow 30s linear infinite; }
.r2 { width: 220px; height: 220px; top: 50%; left: 50%; transform: translate(-50%,-50%); border-color: rgba(99,102,241,0.1); animation: spin-slow 20s linear infinite reverse; }
@keyframes spin-slow { from { transform: translate(-50%,-50%) rotate(0deg); } to { transform: translate(-50%,-50%) rotate(360deg); } }

/* ── Stats Band ── */
.stats-band {
  position: relative; z-index: 1;
  background: rgba(22,25,33,0.6);
  border-top: 1px solid var(--border); border-bottom: 1px solid var(--border);
  backdrop-filter: blur(10px);
  scroll-margin-top: 68px;
  margin-bottom: 96px;
}
.stats-inner { max-width: 1200px; margin: 0 auto; padding: 48px 32px; display: grid; grid-template-columns: repeat(4,1fr); }
.stat-block { padding: 0 32px; border-right: 1px solid var(--border); display: flex; flex-direction: column; gap: 4px; }
.stat-block:first-child { padding-left: 0; }
.stat-block:last-child { border-right: none; }
.stat-num-big { font-family: 'Bebas Neue', sans-serif; font-size: 48px; color: var(--accent); line-height: 1; letter-spacing: 0.02em; }
.stat-label-big { font-size: 14px; font-weight: 700; color: var(--text-1); }
.stat-desc-big { font-size: 12px; color: var(--text-3); margin-top: 2px; }

/* ── Section Shared ── */
.section-header { text-align: center; margin-bottom: 56px; }
.section-eyebrow { font-size: 10px; font-weight: 700; letter-spacing: 0.2em; color: var(--accent); margin-bottom: 12px; }
.section-title { font-family: 'Bebas Neue','Noto Sans SC',sans-serif; font-size: 48px; color: var(--text-1); margin: 0 0 14px; letter-spacing: 0.04em; }
.section-sub { font-size: 14px; color: var(--text-2); max-width: 480px; margin: 0 auto; line-height: 1.8; }

/* ── Events Section ── */
.events-section { position: relative; z-index: 1; max-width: 1200px; margin: 0 auto; padding: 48px 32px 96px; scroll-margin-top: 68px; }
.events-grid { display: grid; grid-template-columns: repeat(4,1fr); gap: 20px; }
.event-card { background: rgba(22,25,33,0.8); border: 1px solid var(--border); border-radius: 16px; display: flex; flex-direction: column; overflow: hidden; transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s; }
.event-card:hover { transform: translateY(-4px); box-shadow: 0 16px 40px rgba(0,0,0,0.3); border-color: rgba(255,107,53,0.2); }
.event-card-top { display: flex; align-items: center; gap: 7px; padding: 12px 18px; border-bottom: 1px solid var(--border); }
.event-status-dot { width: 7px; height: 7px; border-radius: 50%; }
.event-status-dot.live { background: #10b981; animation: pulse-dot 1.5s ease-in-out infinite; }
.event-status-dot.reg  { background: var(--accent); }
.event-status-text { font-size: 11px; font-weight: 600; color: var(--text-2); letter-spacing: 0.06em; flex: 1; }
.event-deadline { font-size: 10px; color: var(--text-3); }
.event-card-body { padding: 18px; flex: 1; display: flex; flex-direction: column; gap: 8px; }
.event-category { display: inline-block; font-size: 10px; font-weight: 700; letter-spacing: 0.12em; color: var(--accent); background: rgba(255,107,53,0.1); border: 1px solid rgba(255,107,53,0.2); padding: 2px 8px; border-radius: 20px; width: fit-content; }
.event-name { font-size: 14px; font-weight: 700; color: var(--text-1); margin: 4px 0; line-height: 1.4; }
.event-meta { display: flex; flex-direction: column; gap: 5px; margin-top: 4px; }
.event-meta-item { display: flex; align-items: center; gap: 6px; font-size: 11px; color: var(--text-3); }
.meta-icon { width: 13px; height: 13px; flex-shrink: 0; }
.event-progress-wrap { display: flex; align-items: center; gap: 8px; margin-top: 8px; }
.event-progress-bar { flex: 1; height: 4px; background: var(--dark-5); border-radius: 2px; overflow: hidden; }
.event-progress-fill { height: 100%; background: linear-gradient(90deg, var(--accent), #ff9a6c); border-radius: 2px; }
.event-progress-fill.prog-hot { background: linear-gradient(90deg, #ef4444, #f87171); }
.event-pct { font-size: 11px; font-weight: 700; min-width: 30px; text-align: right; }
.event-hot-tip { font-size: 11px; color: #ef4444; font-weight: 600; }
.event-card-footer { padding: 12px 18px; border-top: 1px solid var(--border); }
.event-btn { font-size: 12px; font-weight: 600; color: var(--accent); background: transparent; border: none; cursor: pointer; font-family: inherit; transition: opacity 0.2s; padding: 0; }
.event-btn:hover { opacity: 0.7; }

/* ── Features Section ── */
.features-section { position: relative; z-index: 1; max-width: 1200px; margin: 0 auto; padding: 0 32px 96px; scroll-margin-top: 68px; }
.features-grid { display: grid; grid-template-columns: repeat(3,1fr); gap: 20px; }
.feature-card { background: rgba(22,25,33,0.7); border: 1px solid var(--border); border-radius: 16px; padding: 28px; display: flex; flex-direction: column; gap: 12px; transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s; }
.feature-card:hover { transform: translateY(-3px); box-shadow: 0 12px 32px rgba(0,0,0,0.25); border-color: rgba(255,255,255,0.12); }
.feature-card.featured { background: linear-gradient(145deg, rgba(255,107,53,0.08), rgba(22,25,33,0.8)); border-color: rgba(255,107,53,0.2); }
.feature-icon-wrap { width: 46px; height: 46px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.feature-icon-svg { width: 24px; height: 24px; }
.feature-title { font-size: 15px; font-weight: 700; color: var(--text-1); margin: 0; }
.feature-desc { font-size: 13px; color: var(--text-2); line-height: 1.7; margin: 0; flex: 1; }
.feature-tag { font-size: 10px; font-weight: 700; letter-spacing: 0.1em; color: var(--text-3); text-transform: uppercase; }

/* ── CTA Banner ── */
.cta-banner { position: relative; z-index: 1; margin: 0 32px 80px; border-radius: 20px; background: linear-gradient(135deg, rgba(255,107,53,0.12) 0%, rgba(22,25,33,0.9) 60%); border: 1px solid rgba(255,107,53,0.18); overflow: hidden; }
.cta-banner-inner { max-width: 1200px; margin: 0 auto; padding: 72px 64px; position: relative; }
.cta-deco-ring { position: absolute; right: -80px; top: 50%; transform: translateY(-50%); width: 400px; height: 400px; border-radius: 50%; border: 1px solid rgba(255,107,53,0.1); pointer-events: none; }
.cta-deco-ring::before { content: ''; position: absolute; inset: 40px; border-radius: 50%; border: 1px solid rgba(255,107,53,0.07); }
.cta-content { position: relative; z-index: 1; max-width: 560px; }
.cta-title { font-family: 'Bebas Neue','Noto Sans SC',sans-serif; font-size: 52px; color: var(--text-1); margin: 12px 0 16px; letter-spacing: 0.04em; }
.cta-sub { font-size: 14px; color: var(--text-2); line-height: 1.8; margin-bottom: 36px; }
.cta-btns { display: flex; gap: 14px; flex-wrap: wrap; }

/* ── Footer ── */
.site-footer { position: relative; z-index: 1; border-top: 1px solid var(--border); background: rgba(13,15,20,0.9); }
.footer-inner { max-width: 1200px; margin: 0 auto; padding: 32px 32px; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 16px; }
.footer-brand { display: flex; align-items: center; gap: 10px; }
.footer-logo svg { width: 26px; height: 26px; }
.footer-name { font-size: 14px; font-weight: 700; color: var(--text-1); }
.footer-tagline { font-size: 12px; color: var(--text-3); margin-left: 8px; }
.footer-copy { font-size: 12px; color: var(--text-3); }

/* ── Responsive ── */
@media (max-width: 1024px) {
  .hero { grid-template-columns: 1fr; gap: 48px; padding: 72px 24px 60px; }
  .hero-visual { height: 360px; }
  .hero-title { font-size: 60px; }
  .events-grid { grid-template-columns: repeat(2,1fr); }
  .features-grid { grid-template-columns: repeat(2,1fr); }
  .stats-inner { grid-template-columns: repeat(2,1fr); gap: 24px; }
  .stat-block { border-right: none; padding: 16px 0; border-bottom: 1px solid var(--border); }
  .stat-block:last-child { border-bottom: none; }
}
@media (max-width: 700px) {
  .nav-links, .nav-actions { display: none; }
  .nav-mobile-toggle { display: block; }
  .hero-title { font-size: 48px; }
  .hero-visual { height: 280px; }
  .main-card-vis { right: 0; }
  .side-card-vis, .mini-card-vis { display: none; }
  .events-grid, .features-grid { grid-template-columns: 1fr; }
  .stats-inner { grid-template-columns: 1fr 1fr; }
  .cta-banner { margin: 0 16px 60px; }
  .cta-banner-inner { padding: 40px 28px; }
  .cta-title { font-size: 36px; }
  .footer-inner { flex-direction: column; gap: 12px; }
}
</style>