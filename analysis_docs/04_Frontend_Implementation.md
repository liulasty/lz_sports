# LZ Sports 前端实现细节分析

## 1. 前端架构与基础框架
LZ Sports 前端基于最新的 **Vue 3.5** 和 **Vite 7.3** 构建，采用了现代前端化标准架构。
- **UI 框架**：全局深度集成 Element Plus 提供丰富的桌面端基础组件。
- **状态管理**：使用轻量级的 Pinia 进行状态管理，替代了 Vuex。
- **样式方案**：使用 Sass 预处理器，配合 `src/assets/styles/theme` 实现了系统级的主题色动态切换。

## 2. 路由与权限控制 (Vue Router)
系统通过 `vue-router` 实现单页面应用（SPA）导航，并且在全局前置守卫（`router.beforeEach`）中做了极度严格的权限拦截：

### 2.1 系统初始化检测
- 路由首先会判断系统是否已初始化（通过检查 `localStorage` 中的 `isInitialized` 以及调用后端的 `checkInit` API）。
- 如果系统尚未初始化，所有访问都会被强制重定向到 `/init` 页面，引导超级管理员配置学校基础信息。

### 2.2 首次登录强制改密
- 守卫会读取 `userStore.userInfo.isFirstLogin` 字段。
- 对于管理员后台导入的用户，如果是首次登录，会被强制拦截并重定向到 `/reset-password` 页面，直到完成密码重置才能进入控制台（Dashboard）。

### 2.3 细粒度 RBAC 鉴权
- 路由表中利用 `meta.roles` 定义了不同页面的访问权限（例如 `['SUPER_ADMIN', 'EVENT_ADMIN', 'SCHOOL_ADMIN']`）。
- 路由守卫会对当前用户的 `userStore.userInfo.type` 进行匹配，如果不满足权限，则自动重定向到 `/403` 无权限页面。

## 3. 请求封装与异常拦截 (Axios)
系统的网络请求模块 (`src/utils/request.js`) 进行了高度定制化封装：

### 3.1 统一认证头注入
在 `interceptors.request` 中，系统会自动从 Pinia 状态树或 `localStorage` 提取 JWT Token，并双向注入到请求头的 `token` 和 `Authorization: Bearer {token}` 字段中，兼容不同后端的获取方式。

### 3.2 策略模式处理全局异常
- 区别于传统在 Axios 响应拦截器中写一堆 `if-else` 或 `switch` 语句，系统引入了**策略模式**（配置在 `src/config/errorStrategy.js` 中）。
- 当请求出错时，拦截器会根据 HTTP 状态码去策略字典中获取对应的处理策略（例如 401 策略包含 `clearAuth: true` 和 `redirect: '/login'`）。
- 拦截器通过策略驱动完成清理本地存储、调用 `router.push` 重定向以及触发 `ElMessage` 全局通知，极大提高了代码的可维护性和扩展性。

## 4. 核心页面结构
系统采用了典型的管理后台布局结构：
- **通用/门户页**：登录 (`/login`)、注册 (`/register`)、找回密码 (`/forgot-password`)、门户主页 (`/`)。
- **框架内部页**：挂载在 `/_layout` 之下，包含侧边栏 (Sidebar) 和顶部导航 (Navbar)。
  - **公共中心**：控制面板 (Dashboard)、赛事列表与详情、个人资料维护、消息通知。
  - **运动员工作台**：我的报名 (`/my-registrations`)、我的成绩 (`/my-score`)。
  - **管理控制台**：赛事创建与管理、报名与运动员资质审核、成绩管理、人员管理、全校系统配置等。

## 5. 组件化与数据可视化
- **模块化组件**：针对复用性高的业务功能（如赛事状态流转、成绩录入弹窗），系统抽取了独立的 Vue 组件。
- **数据大屏与看板**：在 Dashboard 中，集成了 **ECharts** 进行报名人数统计、各学院成绩占比等数据的可视化渲染。
