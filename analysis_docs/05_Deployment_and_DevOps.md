# LZ Sports 部署与运维架构分析

## 1. 部署架构概览
LZ Sports 支持多种维度的部署方式，从本地单机开发到企业级容器化集群部署，提供了完整的配置与脚本支持。
- **本地环境**：支持直接运行 Spring Boot 和 Vite，依赖本地的 MySQL 和 Redis。
- **容器化编排 (Docker Compose)**：提供了一键拉起整个技术栈（包括 MySQL, Redis, Adminer, Backend, Frontend）的 `docker-compose.yml` 方案。
- **集群化编排 (Kubernetes)**：提供了基于 K8s 的 Deployment、Service、ConfigMap 和 Secret 配置文件。

## 2. Docker Compose 部署实现

### 2.1 服务划分
在 `docker-compose.yml` 中，定义了 5 个核心 Service，均运行在自建的 `appnet` Bridge 网络中：
- `mysql`：基于 MySQL 8.0 镜像，挂载环境变量设置 root 密码和初始化库。配置了 healthcheck。
- `redis`：基于 Redis 7 Alpine 镜像，开启密码认证。
- `adminer`：轻量级的数据库管理工具，方便直接在浏览器管理数据。
- `backend`：后端应用，通过 `depends_on` (结合 `condition: service_healthy`) 确保在 MySQL 和 Redis 启动并健康后再启动。挂载了极度丰富的环境变量（OSS, Mail, JWT, DB 等）。
- `frontend`：前端应用，基于 Nginx 镜像打包，依赖 backend。

### 2.2 环境变量管理
系统将敏感信息和可变配置抽取到了 `.env` 文件中，通过 `${VARIABLE_NAME}` 语法注入到 Docker 容器内部。这使得同一份 Compose 文件可以轻松复用于 dev/test/prod 环境。

## 3. Kubernetes (K8s) 部署实现

在 `k8s/` 目录下提供了 Kubernetes 的部署清单，主要分为以下部分：

### 3.1 资源配置分离 (ConfigMap & Secret)
在 `backend-deployment.yaml` 中：
- **ConfigMap (`lz-sports-backend-config`)**：管理非敏感的明文配置，如 `SPRING_PROFILES_ACTIVE`, `DB_URL`, `OSS_ENDPOINT` 等。
- **Secret (`lz-sports-backend-secret`)**：管理敏感信息，如数据库密码、Redis密码、JWT 密钥、OSS AccessKey 等。

### 3.2 部署控制器 (Deployment)
- **Backend Deployment**：
  - 通过 `envFrom` 将 ConfigMap 和 Secret 批量注入为环境变量。
  - 配置了 `volumeMounts` 和 `hostPath` 数据卷，用于持久化挂载本地上传的附件到 `/app/uploads`。
  - `imagePullPolicy: Never` 暗示该配置适合于本地开发集群（如 Minikube / Docker Desktop K8s），若是生产环境需改为 `Always` 或 `IfNotPresent`。
- **Frontend Deployment**：
  - 一个轻量级的 Nginx 静态文件代理，对外暴露 80 端口。

## 4. 自动化构建与脚本支持
根目录与 `scripts/` 目录提供了大量批处理与 Shell 脚本：
- `build.sh` / `build.bat`：用于前端的 `npm run build` 和后端的 `mvn clean package` 自动化。
- `deploy.sh` / `publish.sh`：用于自动构建 Docker 镜像，打标签（Tag），并推送到私有镜像仓库或直接通过 Compose 重启服务。
- 这些脚本的存在极大地简化了 CI/CD 的对接工作（如接入 Jenkins、GitLab CI 或 GitHub Actions）。
