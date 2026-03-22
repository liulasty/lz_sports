# Linux Docker 启动说明

适用场景：Linux 服务器/开发机，通过 `docker compose` 运行前后端。

## 前置条件

- Docker Engine + Docker Compose Plugin
- MySQL、Redis 对容器可达
- 后端 `.env` 文件存在：`lz_sports_backend/.env`

## 启动步骤

1. 后端先打包

```bash
cd /path/to/lz_sports/lz_sports_backend
mvn -DskipTests package
```

2. 回到根目录，设置统一前端端口并启动

```bash
cd /path/to/lz_sports
export FRONTEND_PORT=5173
docker compose up -d --build
```

## 访问地址

- 前端：`http://<服务器IP>:5173`
- 后端：`http://<服务器IP>:8080`

## 常用命令

```bash
docker compose ps
docker compose logs -f frontend
docker compose logs -f backend
docker compose down
```

## 改端口方式

只改这一行即可：

```bash
export FRONTEND_PORT=5200
```
