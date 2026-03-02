# Docker 与 Kubernetes 配置指南

本指南介绍如何使用 Docker Compose 和 Kubernetes 运行 `lz_sports` 应用程序，并支持本地数据库和 Redis。

## 前提条件
- 已安装 Docker 和 Docker Compose
- Kubernetes 集群（例如，已启用 Kubernetes 的 Docker Desktop）
- Java 21 和 Maven（用于本地开发）
- Node.js 20 或更高版本（用于本地开发）

## 1. Docker Compose

使用 Docker Compose 运行应用程序：

```bash
docker-compose up --build
```

- **后端**：可通过 `http://localhost:8080` 访问
- **前端**：可通过 `http://localhost:80` 访问
- **数据库**：连接到本地 MySQL（地址为 `localhost:3306`）
- **Redis**：连接到本地 Redis（地址为 `localhost:6379`）

## 2. Kubernetes

将应用程序部署到 Kubernetes：

1.  **构建镜像**：
    确保镜像在本地可用（如果使用 Docker Desktop）或将其推送到镜像仓库。
    ```bash
    docker-compose build
    ```

2.  **应用配置文件**：
    ```bash
    kubectl apply -f k8s/
    ```

3.  **访问应用程序**：
    - **前端**：可通过 NodePort `30080` 访问（例如 `http://localhost:30080`）
    - **后端**：内部服务名称为 `backend`

### 配置详情

- **数据库和 Redis**：
    - 配置为使用 `host.docker.internal` 访问主机上运行的服务。
    - 请确保本地 MySQL 和 Redis 正在运行且可访问。

- **前端代理**：
    - Nginx 配置为将 `/api/` 请求代理到 `http://backend:8080/`。

## 创建的文件

- `lz_sports_backend/Dockerfile`：后端构建和运行环境镜像。
- `lz_sports_frontend/Dockerfile`：前端构建和运行环境镜像。
- `lz_sports_frontend/nginx.conf`：前端的 Nginx 配置文件。
- `docker-compose.yml`：本地开发的编排文件。
- `k8s/`：Kubernetes 部署和服务配置文件。