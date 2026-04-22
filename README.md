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
mvn -Pnon-container-baseline test
```

### 联调 smoke（登录/报名/成绩）
```bash
# DryRun：仅校验步骤结构，不依赖服务
powershell -ExecutionPolicy Bypass -File scripts/smoke-linkup.ps1 -DryRun

# 真实执行：依赖本地前后端服务与可用账号
powershell -ExecutionPolicy Bypass -File scripts/smoke-linkup.ps1 -BackendUrl http://localhost:8081

# Token 模式：跳过登录，直接验证报名/成绩链路
powershell -ExecutionPolicy Bypass -File scripts/smoke-linkup.ps1 -BackendUrl http://localhost:8081 -AccessToken "<BearerToken>"
```

### 通知 smoke（分页一致性/鉴权/隔离）
```bash
# 默认账号（smoke_user_447613714）回放通知链路
powershell -ExecutionPolicy Bypass -File scripts/smoke-notification.ps1 -BackendUrl http://localhost:8081

# Token 模式：跳过登录，直接验证通知分页一致性
powershell -ExecutionPolicy Bypass -File scripts/smoke-notification.ps1 -BackendUrl http://localhost:8081 -AccessToken "<BearerToken>"

# 通过统一测试入口执行
scripts/test.bat notify-smoke http://localhost:8081
./scripts/test.sh notify-smoke http://localhost:8081
```

### 全链路 smoke（报名/成绩 + 通知）
```bash
# 一次性串行执行 linkup + notification 两段 smoke
powershell -ExecutionPolicy Bypass -File scripts/smoke-full.ps1 -BackendUrl http://localhost:8081

# 通过统一测试入口执行
scripts/test.bat full-smoke http://localhost:8081
./scripts/test.sh full-smoke http://localhost:8081
```

### 开工/收工闭环（本地开发推荐）
```bash
# 开工：自动加载 config/.env.dev、清理 8080/5173 冲突并启动前后端
powershell -ExecutionPolicy Bypass -File scripts/dev-start.ps1
# 或
scripts/test.bat dev-start
./scripts/test.sh dev-start

# 收工：按端口停止前后端
powershell -ExecutionPolicy Bypass -File scripts/dev-stop.ps1
# 或
scripts/test.bat dev-stop
./scripts/test.sh dev-stop
```

---

## 非容器测试基线

在不使用 Docker/Testcontainers 的前提下，后端可执行测试基线如下：

```bash
cd lz_sports_backend
mvn -Pnon-container-baseline test
```

基线说明文档：`lz_sports_backend/src/test/README_NON_CONTAINER_BASELINE.md`

---

## CI 工作流

- 统一前后端基础流水线：`.github/workflows/ci.yml`
  - 前端：`npm run lint` + `npm test`
  - 后端：`mvn -Pnon-container-baseline test`
- 后端定向流水线：`.github/workflows/backend-smoke-ci.yml`

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

- 本项目前端已配置 `lint` 与 `test` 脚本，`typecheck` 仍未单独配置。
- 后端与前端通过 `/api` 代理联调，默认同机开发即可直接启动。
- 项目仅供学习与交流使用。
