# Phase 4 自检清单 · 管理与收尾

> 全部为 [x] 才允许进入全项目自检

---

## 👥 用户管理

- [x] 用户列表支持按 role、status、keyword 筛选
- [x] 修改 SUPER_ADMIN 角色返回 403
- [x] 禁用 SUPER_ADMIN 账号返回 403
- [x] 禁用后该用户 token 立即失效（下次请求返回 401）
- [x] 可设置的角色仅限：USER / EVENT_ADMIN

---

## 📈 数据统计

- [x] 总览数据包含：总用户数、总赛事数、各状态赛事数、总报名数、本月新增用户数
- [x] 赛事维度统计包含：每个赛事的报名总数、审核通过数、成绩已发布项目数
- [x] 统计数据与数据库实际数据一致（手动抽查验证）

---

## 🎨 学校个性化配置

- [x] 修改校名后 GET /public/school-config 返回新校名
- [x] 修改校名后前端顶部导航栏实时更新
- [x] Logo 上传限制：jpg/png/gif，≤ 5MB，超出返回 400
- [x] Logo 上传成功后登录页和导航栏同步更新
- [x] 主题色切换后全局 CSS 变量更新，页面颜色实时变化
- [x] 6套主题色全部可正常切换

---

## 🖥️ 前端页面完整性

确认以下所有页面已实现且可正常访问：

- [x] /setup — 初始化向导
- [x] /login — 登录页
- [x] /register — 注册页
- [x] /reset-password — 重置密码页
- [x] /force-change-password — 强制改密码页
- [x] / — 首页（赛事列表）
- [x] /events/:id — 赛事详情页
- [x] /events/:id/apply — 申请运动员页
- [x] /my/registrations — 我的报名记录
- [x] /my/applications — 我的运动员申请
- [x] /my/results — 我的成绩
- [x] /public/events/:id/results — 公开成绩榜
- [x] /admin — 管理后台首页（仪表盘）
- [x] /admin/events — 赛事管理列表
- [x] /admin/events/create — 创建赛事
- [x] /admin/events/:id/edit — 编辑赛事
- [x] /admin/events/:id/items — 项目管理
- [x] /admin/events/:id/admins — 管理员分配
- [x] /admin/events/:id/athletes — 运动员审核
- [x] /admin/events/:id/results — 成绩管理
- [x] /admin/projects — 项目库管理
- [x] /admin/users — 用户管理
- [x] /admin/settings — 学校配置

---

## 📱 响应式适配

- [x] 所有用户侧页面在 375px 宽度下无横向滚动
- [x] 所有用户侧页面在 375px 宽度下核心功能可正常操作
- [x] 所有页面在 1280px 宽度下布局正常无错位
- [x] 导航栏在移动端正确折叠

---

## 🔒 全局安全检查

- [x] 全局搜索项目中无硬编码密码或密钥
- [x] 全局搜索无硬编码颜色值（#fff、#333等应使用CSS变量）
- [x] 文件上传接口校验文件类型（白名单校验，非后缀名校验）
- [x] 所有 SQL 通过 MyBatis 参数绑定，无字符串拼接 SQL
- [x] 前端不直接展示后端原始错误堆栈给用户

---

## 📦 交付物检查

- [x] 后端 mvn package 打包成功，无编译错误
- [x] 前端 npm run build 打包成功，无编译错误
- [x] docs/deploy.md 部署文档存在，包含：
  - [x] 环境要求说明（JDK21、MySQL8、Redis7）
  - [x] 数据库初始化步骤
  - [x] application-prod.yml 配置说明
  - [x] Nginx 配置参考
  - [x] Docker Compose 启动方式
- [x] sql/init.sql 可独立执行，无依赖错误
- [x] sql/data.sql 内置项目数据完整

---

## 代码质量终检

- [x] 无遗留 System.out.println
- [x] 无遗留 console.log
- [x] 无遗留 TODO / FIXME 注释
- [x] 无空 catch 块
- [x] 无未使用的 import