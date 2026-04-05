# Linux / macOS 混合开发指南

本指南与 Windows 混合开发指南的思路类似，适用于在 Linux (如 Ubuntu 桌面版) 或 macOS 下，结合 Docker 与 IDE 进行高效开发。

## 🎯 准备工作
1. 确保已安装 [Docker Desktop for Mac](https://www.docker.com/products/docker-desktop/) 或是 Linux 上的 Docker Engine。
2. 确保安装了 IntelliJ IDEA 或 VS Code。

## 💡 后端开发者模式

如果你是后端开发者，你希望：**使用 Docker 运行数据库、缓存和前端**，而在 IDEA 中**运行后端代码进行断点调试**。

### 1. 启动除后端以外的基础设施

我们只需启动 `mysql`、`redis` 和 `frontend` 容器，不需要启动 `backend`。打开终端执行：
```bash
# 使用原生 docker compose 命令，只启动所需服务
docker compose --env-file config/.env.dev -f docker-compose.yml -f docker-compose.dev.yml up -d mysql redis frontend adminer
```
*此时，MySQL (3306) 和 Redis (6379) 已经映射到你电脑的本地端口，前端 (5173) 也会启动（不过前端此时请求接口会失败，因为后端还没起）。*

### 2. 在 IDEA 中启动后端

由于我们已经做好了配置解耦，你可以直接在 IDEA 中找到 `com.lz.BackendApplication` 类：
1. 点击类左侧的绿色箭头 `Run` 或 `Debug`。
2. Spring Boot 会默认加载 `application-dev.yml` 中的配置，连接到 `localhost:3306` 的 MySQL 和 `localhost:6379` 的 Redis。
3. 启动成功后，前端 (http://localhost:5173) 的请求会被自动转发到你 IDEA 里启动的后端 (8080) 端口上。

---

## 💻 前端开发者模式

如果你是前端开发者，你希望：**使用 Docker 运行后端接口、数据库和缓存**，而在 VS Code 中**运行 Vue 代码进行热更新**。

### 1. 启动全套后端基础设施

如果你本地没有数据库环境，你可以直接使用一键部署脚本拉起包括后端在内的所有依赖：
```bash
# 添加执行权限（首次使用时）
chmod +x scripts/*.sh

# 编译最新后端并启动
./scripts/build.sh
./scripts/deploy.sh dev
```
*这会启动所有服务，包括端口为 8080 的 `backend`。*

### 2. 在本地启动 Vue

打开终端，进入前端目录：
```bash
cd lz_sports_frontend
npm install
npm run dev
```

> **注意**：如果本地 Vue 启动的端口也是 `5173`，可能会和 Docker 里的 `frontend` 容器冲突。
> 此时，建议你在 `lz_sports_frontend/vite.config.js` 或运行命令中换一个端口启动（例如 `5174`），或者在启动 Docker 时停止掉前端容器：
> ```bash
> docker compose stop frontend
> ```

---

## ⚡ 纯本地开发（不使用 Docker 容器）

如果你希望彻底脱离 Docker 进行开发：

1. 确保你本地（或你的局域网服务器）的 MySQL 和 Redis 正在运行。
2. 配置 `lz_sports_backend/src/main/resources/application-dev.yml` 中的数据库账号密码。
3. 分别在 IDEA 和 VS Code 中启动后端和前端。

或者，你可以使用 `--local-db` 标志来部署容器，强制容器直连你宿主机上已有的数据库：
```bash
./scripts/deploy.sh dev --local-db
```