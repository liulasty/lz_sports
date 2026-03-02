# LZ Sports 运动管理平台 (Refactored)

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-green.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.5-4FC08D.svg)](https://vuejs.org/)
[![Vite](https://img.shields.io/badge/Vite-7.3-646CFF.svg)](https://vitejs.dev/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)

**LZ Sports** 是一个基于 Spring Boot 3 和 Vue 3 全新重构的校园运动会与赛事管理平台。它提供了从用户注册、运动员认证、赛事发布到在线报名、成绩查询的全流程解决方案。

---

## 📖 目录

- [核心功能](#-核心功能)
- [技术栈](#-技术栈)
- [环境要求](#-环境要求)
- [快速开始](#-快速开始)
- [项目结构](#-项目结构)
- [配置说明](#-配置说明)
- [版权说明](#-版权说明)

---

## 🌟 核心功能

### 🏃‍♂️ 运动员端
- **个人中心**: 维护个人档案，申请成为注册运动员。
- **赛事浏览**: 查看当前开启报名的运动会及具体项目（如田径、球类）。
- **在线报名**: 根据年级、性别限制进行项目报名。
- **状态追踪**: 实时查看报名审核状态及比赛成绩。

### 🛡️ 管理员端
- **用户管理**: 管理普通用户与运动员账户，审核运动员资质。
- **赛事管理**: 发布新赛事，配置报名起止时间、规程文档。
- **项目管理**: 设置具体比赛项目（限制人数、性别、年级）。
- **报名审核**: 审核运动员的参赛申请。
- **数据仪表盘**: 基于 ECharts 的赛事数据统计与可视化。

---

## 🛠 技术栈

### Backend (后端)
- **Framework**: Spring Boot 3.2.3 (Spring Security 6)
- **Language**: Java 21 (LTS)
- **Database**: MySQL 5.7/8.0
- **ORM**: MyBatis-Plus 3.5.5
- **Cache**: Redis
- **Storage**: Aliyun OSS (Object Storage Service)
- **Tooling**: Lombok, Maven, FastJSON2

### Frontend (前端)
- **Framework**: Vue 3.5 (Composition API)
- **Build Tool**: Vite 7.3
- **State Management**: Pinia
- **UI Library**: Element Plus
- **HTTP Client**: Axios
- **Style**: Sass (SCSS)

---

## ✅ 环境要求

在运行项目之前，请确保本地环境满足以下最低要求：

- **JDK**: 21+
- **Node.js**: 18.0+
- **MySQL**: 5.7 或 8.0
- **Redis**: 任意稳定版本
- **Maven**: 3.6+

---

## 🚀 快速开始

### 1. 数据库准备
1. 创建数据库 `lz_sports`。
2. 导入 SQL 脚本: `lz_sports_backend/src/main/resources/sql/lz_sports.sql`。

```sql
CREATE DATABASE lz_sports DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
-- 随后执行 SQL 文件中的建表语句
```

### 2. 后端启动
1. 进入后端目录: `cd lz_sports_backend`
2. 修改配置文件 `src/main/resources/application.yml`:
   - 更新 `spring.datasource` 下的数据库账号密码。
   - 更新 `aliyun.oss` 配置（如需使用图片上传功能）。
3. 启动应用:
   ```bash
   mvn spring-boot:run
   ```
   后端服务将运行在 `http://localhost:8080`。

### 3. 前端启动
1. 进入前端目录: `cd lz_sports_frontend`
2. 安装依赖:
   ```bash
   npm install
   ```
3. 启动开发服务器:
   ```bash
   npm run dev
   ```
   前端页面将运行在 `http://localhost:5173`。

---

## 📂 项目结构

```
lz_sports/
├── lz_sports_backend/       # Spring Boot 3 后端
│   ├── src/main/java/com/lz # Java 源码
│   │   ├── controller       # 控制层
│   │   ├── service          # 业务逻辑层
│   │   ├── entity           # 数据库实体
│   │   └── mapper           # MyBatis 接口
│   └── src/main/resources   # 配置文件与 Mapper XML
├── lz_sports_frontend/      # Vue 3 前端
│   ├── src/
│   │   ├── api              # Axios 请求封装
│   │   ├── views            # 页面组件
│   │   ├── stores           # Pinia 状态管理
│   │   └── utils            # 工具类
│   └── vite.config.js       # Vite 配置
└── README.md                # 项目文档
```

---

## ⚙️ 配置说明

### 数据库配置
在 `lz_sports_backend/src/main/resources/application.yml` 中修改:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lz_sports?...
    username: root
    password: your_password
```

### 阿里云 OSS 配置
若需测试图片上传，请配置:
```yaml
aliyun:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    accessKeyId: your_key_id
    accessKeySecret: your_key_secret
    bucketName: your_bucket
```

---

## 📄 版权说明

本项目仅供学习与交流使用。
