# Windows 桌面版 Docker 部署指南 (Docker Desktop)

本指南适用于在 Windows 环境下（无论是开发机还是测试机），通过 Docker Desktop 快速拉起完整的 LZ Sports 系统。

## 🎯 准备工作

1. 确保已安装 **[Docker Desktop for Windows](https://www.docker.com/products/docker-desktop/)**。
2. 确保 Docker Desktop 正在运行，并且 WSL 2 (Windows Subsystem for Linux) 已启用。
3. 确保你的系统中已安装 Java 21+ 和 Maven 3.6+（构建后端时需要）。
4. 确保端口未被占用（默认占用：8080，5173，3306，6379，8081）。如需修改，请参考 [端口切换指南](../端口切换配置指南.md)。

## 🚀 一键部署全套系统

在项目根目录下，打开 PowerShell 或 CMD，执行以下命令：

### 1. 编译并构建镜像
```bat
.\scripts\build.bat
```
*此步骤会先调用 Maven 打包 `lz_sports_backend`，然后读取项目根目录的 `docker-compose.yml` 及相关 Dockerfile，构建前端和后端的最新 Docker 镜像。*

### 2. 启动开发环境（推荐测试使用）
```bat
.\scripts\deploy.bat dev
```
*此命令将加载 `config/.env.dev` 的配置。MySQL 和 Redis 容器将会启动，并对外暴露 `3306` 和 `6379` 端口，方便你使用 Navicat 或 Redis Desktop Manager 连接查看数据。*

> **启动后访问：**
> * 前端应用：http://localhost:5173
> * 后端接口：http://localhost:8080/api/system/school-config
> * Adminer（数据库管理面板）：http://localhost:8081

### 3. 启动生产环境模拟
```bat
.\scripts\deploy.bat prod
```
*生产模式会隐藏 MySQL 和 Redis 的对外端口，限制容器内存占用，并将前端端口切换至 80（需使用管理员权限运行或确保 80 端口未被占用）。*

## 💡 进阶：使用本地数据库运行

如果你的电脑上已经安装了 MySQL 8 和 Redis，并且不希望 Docker 额外占用内存启动这两个容器，你可以加上 `--local-db` 标志。

> ⚠️ **注意**：使用此模式前，请确保本地 MySQL 已创建了 `lz_sports` 数据库，且账号密码与 `config/.env.dev` 中的 `DB_USER` 和 `DB_PASSWORD` 一致。

```bat
.\scripts\deploy.bat dev --local-db
```
*此命令会让 Docker 容器内部的后端应用，通过 `host.docker.internal` 桥接，直接连回你 Windows 宿主机上运行的数据库和 Redis。同时，脚本会自动跳过拉取和启动 MySQL/Redis 容器的步骤。*

## 🛑 停止与清理

当你想停止系统或清理数据时，可以使用原生的 `docker compose` 命令。

**只停止服务，保留数据：**
```bat
docker compose stop
```

**停止服务并删除容器（不删除挂载的数据卷）：**
```bat
docker compose down
```

**停止服务、删除容器，并清除所有数据库和 Redis 数据卷（危险！）：**
```bat
docker compose down -v
```