

2.1 后端技术栈

| 依赖                       | 版本                | 用途             |
| ------------------------ | ----------------- | -------------- |
| Spring Boot              | Spring Boot 3.2.3 | 核心框架           |
| Spring Security          | 6.x（随Boot）        | 认证与权限          |
| Spring Mail              | 随Boot             | 邮件发送（验证码）      |
| Spring Validation        | 随Boot             | 入参校验           |
| Spring Async             | 随Boot             | 异步任务（发邮件/通知）   |
| MyBatis-Plus             | 3.5.x             | ORM框架          |
| MySQL Driver             | 8.x               | 数据库驱动          |
| Redis（spring-data-redis） | 随Boot             | 验证码存储、会话缓存     |
| JJWT                     | 0.12.x            | JWT生成与解析       |
| EasyExcel                | 3.3.x             | Excel导入导出      |
| Lombok                   | 1.18.x            | 简化代码           |
| Hutool                   | 5.8.x             | 工具库（时间/加密/字符串） |
| MinIO Client             | 8.5.x             | 文件存储（预留OSS切换）  |

**JDK 版本**：21（LTS）**打包方式**：Spring Boot Fat Jar**构建工具**：Maven 3.8+

------

### 2.2 前端技术栈

| 依赖           | 版本           | 用途          |
| ------------ | ------------ | ----------- |
| Vue          | 3.4.x        | 核心框架        |
| Vue Router   | 4.x          | 路由管理        |
| Pinia        | 2.x          | 全局状态管理      |
| Element Plus | 2.6.x        | UI组件库       |
| Axios        | 1.6.x        | HTTP请求      |
| ECharts      | 5.x          | 数据统计图表（仪表盘） |
| VueUse       | 10.x         | 组合式工具函数     |
| Day.js       | 1.11.x       | 时间处理        |
| Inter字体      | Google Fonts | 全局字体        |

**构建工具**：Vite 5.x**Node版本**：20+（LTS）**打包产物**：dist静态文件，由Spring Boot托管

------

### 2.3 基础设施

| 组件   | 方案             | 备注                |
| ---- | -------------- | ----------------- |
| 数据库  | MySQL 8.x      | 推荐阿里云RDS          |
| 缓存   | Redis 7.x      | 验证码、JWT黑名单、未读消息计数 |
| 文件存储 | 本地磁盘           | 预留MinIO/OSS切换接口   |
| 反向代理 | Nginx          | 配置参考文档一并交付        |
| 容器化  | Docker Compose | 备选部署方案            |

------

### 2.4 前后端集成方式

```
Vue打包后的dist静态文件
    ↓
放入 Spring Boot resources/static 目录
    ↓
一个Jar包同时提供前端页面和后端API
    ↓
用户访问任意路径 → Nginx → Jar包
```

**这样做的好处**：学校只需要部署一个 Jar 包，不需要分别部署前端和后端，降低运维复杂度。

------

### 2.5 开发环境约定

| 项目     | 约定                                   |
| ------ | ------------------------------------ |
| 后端端口   | 8080（开发）/ 80或443（生产）                 |
| 前端开发端口 | 5173（Vite默认）                         |
| API前缀  | /api/                                |
| 接口文档   | Knife4j（Swagger增强版，开发环境开启）           |
| 代码规范   | 后端：阿里巴巴Java规范 / 前端：ESLint + Prettier |

部署时 Nginx 同时托管前端静态文件，并把 `/api` 请求转发到后端 Jar 包。配套交付一份 Nginx 配置参考文档。