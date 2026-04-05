# Linux 服务器生产环境部署指南 (Docker Compose)

本指南适用于在 Ubuntu / CentOS / Debian 等 Linux 服务器上，通过 Docker Engine 和 Compose 插件，以**生产标准**运行 LZ Sports 系统。

## 🎯 环境准备

1. 安装 [Docker Engine](https://docs.docker.com/engine/install/) 和 Docker Compose 插件。
   ```bash
   # 验证安装
   docker --version
   docker compose version
   ```
2. 安装 Maven 和 JDK 21+。
   ```bash
   sudo apt install maven openjdk-21-jdk
   ```
3. 根据服务器实际情况，修改 `config/.env.prod` 文件。
   - 配置正确的生产数据库密码 (`DB_PASSWORD`)
   - 配置生产级 JWT 密钥 (`JWT_SECRET`)
   - 配置阿里云 OSS 等其他依赖

## 🚀 部署操作

登录到服务器，进入项目根目录。

### 1. 构建应用与镜像

在正式启动前，我们需要编译 Java 代码并生成最新的镜像：

```bash
chmod +x scripts/build.sh
./scripts/build.sh v1.0.0
```
*(这里我们传递了参数 `v1.0.0`，脚本会将生成的镜像标记为 `lz-sports-backend:v1.0.0`，方便进行版本控制。如果不传则默认为 `latest`)*

### 2. 启动生产环境

运行带有 `prod` 标志的部署脚本，以生产模式拉起所有容器：

```bash
chmod +x scripts/deploy.sh
./scripts/deploy.sh prod v1.0.0
```

> **生产模式的特点 (`prod` 标志的作用)：**
> 1. **资源限制**：启用了 `docker-compose.prod.yml` 中的 `deploy.resources.limits`，防止后端内存泄漏导致整个宿主机宕机（默认限制 Backend: 2G, MySQL: 1G, Redis: 512M）。
> 2. **自动重启**：为所有核心服务添加了 `restart: unless-stopped`，宿主机重启或容器异常退出时将自动恢复。
> 3. **端口安全**：关闭了 MySQL (3306) 和 Redis (6379) 的对外映射，仅允许在 Docker 内部网络 (`appnet`) 相互通信。
> 4. **前端端口**：前端将直接暴露在服务器的 `80` 端口。

## 💡 进阶：使用云数据库 (RDS/本地安装)

在真实的生产环境中，为了数据安全和高可用，你可能更倾向于使用云数据库（如阿里云 RDS）或由 DBA 单独部署的 MySQL 实例。

你只需在 `deploy.sh` 后面追加 `--local-db` 标志，即可剥离内置的 MySQL/Redis 容器：

1. **修改配置**：打开 `config/.env.prod`，将 `DB_URL` 修改为你的云数据库地址，修改 `REDIS_HOST` 为云 Redis 地址。
2. **部署时添加标志**：
   ```bash
   ./scripts/deploy.sh prod v1.0.0 --local-db
   ```

*加上 `--local-db` 后，Docker Compose 将加载 `docker-compose.local-db.yml`，这会使得 `mysql` 和 `redis` 容器不再启动，且 `backend` 不再等待它们 `service_healthy`。后端将直接使用 `.env.prod` 中配置的外部地址连接数据库。*

## 🔍 查看日志与维护

**实时查看后端生产日志（最后 100 行）：**
```bash
docker logs -f --tail 100 backend
```

**更新版本：**
当你更新了代码需要发布新版本时：
```bash
# 1. 拉取最新代码
git pull

# 2. 构建新版本 (例如 v1.0.1)
./scripts/build.sh v1.0.1

# 3. 部署新版本，Docker 会自动停止旧容器，启动新容器（秒级切换）
./scripts/deploy.sh prod v1.0.1
```