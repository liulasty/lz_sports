# LZ Sports 系统架构与整体概览

## 1. 系统概述
LZ Sports 是一个面向校园运动会场景的全流程管理平台。它采用前后端分离的架构设计，旨在提供稳定、高效的赛事报名、审核、成绩管理及通知等功能。平台包含两个独立的工程：
- **后端 (`lz_sports_backend`)**：负责业务逻辑处理、数据存储与权限校验。
- **前端 (`lz_sports_frontend`)**：负责用户界面交互与数据可视化。

## 2. 技术栈选型

### 2.1 后端技术栈
- **核心框架**：Java 17 + Spring Boot 3.2.3
- **安全认证**：Spring Security 6 + java-jwt (4.4.0)
- **数据访问层**：MyBatis-Plus 3.5.5 + MySQL (5.7/8.0)
- **缓存与分布式工具**：Redis + Redisson 3.27.0
- **其它工具**：EasyExcel（Excel导入导出）、Fastjson2（JSON解析）、Aliyun OSS（文件存储）、JavaMail（邮件发送）
- **接口文档**：SpringDoc OpenAPI + Knife4j (4.4.0)

### 2.2 前端技术栈
- **核心框架**：Vue 3.5 + Vite 7.3
- **路由与状态管理**：Vue Router 5 + Pinia 3
- **UI组件库**：Element Plus 2.13.2
- **数据可视化**：ECharts 6
- **网络请求**：Axios 1.13.5
- **样式预处理器**：Sass

## 3. 系统架构图

```mermaid
graph TD
    %% 客户端层
    Client[Web 浏览器客户端\nVue3 + ElementPlus] 

    %% 负载均衡/反向代理层
    Nginx[Nginx / 代理服务器]

    %% 后端应用层
    subgraph 后端服务层 [Spring Boot 后端应用 (lz_sports_backend)]
        Controller[Controller 层\n(RESTful API)]
        Security[Spring Security + JWT 认证]
        Service[Service 层\n(核心业务逻辑)]
        Mapper[Mapper 层\n(MyBatis-Plus)]
        
        %% 业务模块
        subgraph 业务模块
            Auth[用户与认证]
            Event[赛事与项目管理]
            Reg[报名审核]
            Score[成绩管理]
            Notify[站内信与通知]
            Stats[统计分析]
        end
    end

    %% 数据持久层与中间件
    subgraph 数据与中间件层
        MySQL[(MySQL 数据库\n主数据存储)]
        Redis[(Redis\n缓存与分布式锁)]
        OSS[阿里云 OSS\n图片/附件存储]
    end

    %% 数据流转
    Client <-->|HTTP/REST| Nginx
    Nginx <-->|反向代理| Controller
    Controller --> Security
    Security --> Service
    Service --> Auth & Event & Reg & Score & Notify & Stats
    Service <--> Mapper
    Mapper <-->|SQL| MySQL
    Service <-->|Redisson/Cache| Redis
    Service <-->|SDK| OSS
```

## 4. 目录结构与模块说明
整个工程遵循标准的 Maven 及前端项目结构：
- `lz_sports_backend/src/main/java/com/lz/`
  - `controller/`：暴露 RESTful API 接口。
  - `service/`：定义业务接口及其实现类。
  - `mapper/`：MyBatis-Plus 的数据访问层。
  - `entity/`：与数据库表对应的实体类。
  - `dto/` & `vo/`：数据传输对象与视图对象，隔离内外数据。
  - `config/`：存放跨域、Redis、Security、MybatisPlus 等配置类。
  - `util/`：JWT、OSS、Redis、String 等工具类。
- `lz_sports_frontend/src/`
  - `api/`：按业务模块封装的 Axios 请求。
  - `views/`：页面级 Vue 组件。
  - `components/`：复用性高的业务/基础组件。
  - `stores/`：Pinia 状态管理。
  - `router/`：Vue 路由配置。
  - `layout/`：页面布局框架（如侧边栏、导航栏）。

## 5. 核心业务流概览
1. **身份认证与鉴权**：用户登录获取 JWT Token，前端将 Token 携带在 HTTP 请求头中；后端经 `JwtAuthenticationFilter` 解析后交由 Spring Security 校验权限。
2. **赛事报名流程**：管理员发布赛事与项目 -> 运动员提交报名 -> 辅导员/院系管理员审核 -> 报名成功。
3. **成绩管理流程**：赛事进行 -> 裁判/管理员录入或 Excel 导入成绩 -> 自动计算积分及排名 -> 成绩公示并更新统计大屏。
