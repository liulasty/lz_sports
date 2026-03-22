# Docker Desktop 启动说明

适用场景：Windows + Docker Desktop，通过 `docker compose` 运行前后端。

## 前置条件

- Docker Desktop（已启用 Docker Compose）
- MySQL、Redis 在宿主机可访问
- 后端 `.env` 文件存在：`lz_sports_backend/.env`

## 启动步骤

1. 后端先打包

```powershell
cd d:\soft\lz_sports\lz_sports_backend
mvn -DskipTests package
```

2. 回到根目录，设置统一前端端口并启动

```powershell
cd d:\soft\lz_sports
$env:FRONTEND_PORT=5173
docker compose up -d --build
```

## 访问地址

- 前端：`http://localhost:5173`
- 后端：`http://localhost:8080`

## 常用命令

```powershell
docker compose ps
docker compose logs -f frontend
docker compose logs -f backend
docker compose down
```

## 改端口方式

只改这一行即可：

```powershell
$env:FRONTEND_PORT=5200
```
