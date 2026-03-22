# LZ Sports 运动管理平台

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-green.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue-3.5-4FC08D.svg)](https://vuejs.org/)
[![Vite](https://img.shields.io/badge/Vite-7.3-646CFF.svg)](https://vitejs.dev/)
[![MySQL](https://img.shields.io/badge/MySQL-5.7%2F8.0-blue.svg)](https://www.mysql.com/)

LZ Sports 是一个面向校园运动会场景的全流程管理平台，包含前后端两个独立工程：  
- 后端：Spring Boot + Spring Security + MyBatis-Plus，提供认证、赛事、报名、成绩、通知等 API。  
- 前端：Vue 3 + Vite + Element Plus，提供运动员端与管理员端统一界面。

---

## 核心能力

### 用户与运动员
- 用户注册、登录、首次登录改密、个人信息维护。
- 运动员认证申请与审核状态跟踪。

### 赛事与项目
- 赛事发布、报名时间管理、赛事详情展示。
- 项目配置（人数、性别、年级限制）与赛事绑定。

### 报名与成绩
- 在线报名、报名审核、个人报名记录查询。
- 成绩录入、查询与导入导出。

### 管理后台
- 用户审核与用户管理。
- 赛事管理、项目管理、报名审核、成绩管理。
- 统计看板与站内通知。

---

## 技术栈

### 后端 `lz_sports_backend`
- Java 17、Spring Boot 3.2.3、Spring Security 6
- MyBatis-Plus 3.5.5、MySQL、Redis、Redisson
- JWT（java-jwt）、EasyExcel、Fastjson2
- SpringDoc OpenAPI + Knife4j（接口文档）

### 前端 `lz_sports_frontend`
- Vue 3.5、Vite 7.3、Vue Router、Pinia
- Element Plus、Axios、ECharts、Sass
- Vite 开发代理：`/api -> http://localhost:8080`

---

## 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 18+
- MySQL 5.7 或 8.0
- Redis（任意稳定版本）

---

## 快速启动

启动入口已统一到分治文档，请先阅读：

- `docs/启动方式/总览.md`

按场景选择：

- Windows 本地：`docs/启动方式/本地开发/Windows本地.md`
- Linux 本地：`docs/启动方式/本地开发/Linux本地.md`
- Docker Desktop：`docs/启动方式/容器部署/DockerDesktop.md`
- Linux Docker：`docs/启动方式/容器部署/LinuxDocker.md`

---

## 常用脚本

### 前端
```bash
npm run dev
npm run build
npm run preview
```

### 后端
```bash
mvn spring-boot:run
mvn test
```

---

## 主要接口分组（后端）

- `/api/auth`：登录注册、用户认证相关
- `/api/system`：系统初始化
- `/api/athlete`：运动员申请与资料
- `/api/event`：赛事相关
- `/api/project`：项目相关
- `/api/registration`：报名相关
- `/api/score`：成绩相关
- `/api/notification`、`/api/notifications`：站内信
- `/api/admin/users`、`/api/admin/stats`、`/api/admin/school-config`：管理端功能

---

## 项目结构

```text
lz_sports/
├─ lz_sports_backend/
│  ├─ src/main/java/com/lz
│  │  ├─ controller
│  │  ├─ service
│  │  ├─ mapper
│  │  ├─ entity
│  │  ├─ dto
│  │  └─ vo
│  ├─ src/main/resources
│  │  ├─ application*.yml
│  │  ├─ mapper
│  │  └─ sql
│  └─ src/test
├─ lz_sports_frontend/
│  ├─ src
│  │  ├─ api
│  │  ├─ views
│  │  ├─ stores
│  │  ├─ router
│  │  └─ layout
│  └─ vite.config.js
└─ README.md
```

---

## 说明

- 本项目当前前端 `package.json` 未配置 lint/typecheck 脚本。
- 后端与前端通过 `/api` 代理联调，默认同机开发即可直接启动。
- 项目仅供学习与交流使用。
