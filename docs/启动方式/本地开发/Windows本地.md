# Windows 混合开发指南 (Docker + IDEA/VSCode)

本指南针对前后端开发者，教你如何利用 Docker 部署基础设施，而在本地机器上使用 IDE 进行代码开发和调试。这种方式既避免了在本地安装 MySQL/Redis 的繁琐，又保留了 IDE 强大的代码补全和热更新能力。

## 🎯 准备工作
1. 确保已安装 [Docker Desktop](https://www.docker.com/products/docker-desktop/) 并在后台运行。
2. 确保安装了 IntelliJ IDEA（后端）或 VS Code（前端）。

## 💡 后端开发者模式

如果你是后端开发者，你希望：**使用 Docker 运行数据库、缓存和前端**，而在 IDEA 中**运行后端代码进行断点调试**。

### 1. 启动除后端以外的基础设施

我们只需启动 `mysql`、`redis` 和 `frontend` 容器，不需要启动 `backend`。打开 PowerShell 执行：
```bat
# 使用原生 docker compose 命令，只启动所需服务
docker compose --env-file config/.env.dev -f docker-compose.yml -f docker-compose.dev.yml up -d mysql redis frontend adminer
```
*此时，MySQL (3306) 和 Redis (6379) 已经映射到你电脑的本地端口，前端 (5173) 也会启动（不过前端此时请求接口会失败，因为后端还没起）。*

### 2. 在 IDEA 中启动后端

由于我们已经做好了配置解耦，你可以直接在 IDEA 中找到 `com.lz.BackendApplication` 类：
1. 点击类左侧的绿色箭头 `Run` 或 `Debug`。
2. Spring Boot 会默认加载 `application-dev.yml` 中的配置，连接到 `localhost:3306` 的 MySQL 和 `localhost:6379` 的 Redis。
3. 启动成功后，前端 (http://localhost:5173) 的请求会被自动转发到你 IDEA 里启动的后端 (8080) 端口上。你可以随时打断点调试前端发来的请求。

---

## 💻 前端开发者模式

如果你是前端开发者，你希望：**使用 Docker 运行后端接口、数据库和缓存**，而在 VS Code 中**运行 Vue 代码进行热更新**。

### 1. 启动全套后端基础设施

如果你本地没有数据库环境，你可以直接使用一键部署脚本拉起包括后端在内的所有依赖：
```bat
# 编译最新后端并启动
.\scripts\build.bat
.\scripts\deploy.bat dev
```
*这会启动所有服务，包括端口为 8080 的 `backend`。*

### 2. 在本地启动 Vue

打开终端，进入前端目录：
```bat
cd lz_sports_frontend
npm install
npm run dev
```

> **注意**：如果本地 Vue 启动的端口也是 `5173`，可能会和 Docker 里的 `frontend` 容器冲突。
> 此时，建议你在 `lz_sports_frontend/vite.config.js` 或运行命令中换一个端口启动（例如 `5174`），或者在启动 Docker 时停止掉前端容器：
> ```bat
> docker compose stop frontend
> ```

---

## ⚡ 纯本地开发（不使用 Docker 容器）

如果你本身就已经在电脑上安装了 MySQL 和 Redis 客户端，并希望彻底脱离 Docker 进行开发。

### 1. 启动本地数据库
确保你本地的 MySQL 正在运行，且已经创建了 `lz_sports` 数据库；确保本地 Redis 正在运行。

### 2. 配置后端 (可选)
如果你的本地 MySQL 密码不是 `1234`，或者 Redis 密码不是 `!Theworld1949`，你需要修改 `lz_sports_backend/src/main/resources/application-dev.yml` 文件中对应的值。

### 3. 启动项目
分别在 IDEA 和 VS Code 中启动后端和前端即可。

### 4. 推荐脚本化开工/收工
如果你希望下次直接开工，建议使用仓库脚本统一处理环境加载与端口冲突：

```bat
:: 开工：加载 config/.env.dev，自动适配本地 mysql/redis 主机名并启动前后端
powershell -ExecutionPolicy Bypass -File scripts/dev-start.ps1

:: 收工：按端口停止前后端（默认 8080/5173）
powershell -ExecutionPolicy Bypass -File scripts/dev-stop.ps1
```

也可走统一入口：

```bat
scripts\test.bat dev-start
scripts\test.bat dev-stop
```

或者，你也可以使用一键部署脚本的 `--local-db` 模式，将前后端打包进容器运行，但直连你的本地数据库：
```bat
.\scripts\build.bat
.\scripts\deploy.bat dev --local-db
```