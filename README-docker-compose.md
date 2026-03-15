# LZ Sports Docker Compose 使用说明

## 1. 前提条件

- 已安装 Docker Desktop（含 Docker Compose）
- 本机已启动 MySQL（默认 `localhost:3306`）
- 本机已启动 Redis（默认 `localhost:6379`）
- 已在后端目录准备好 `.env` 文件：`lz_sports_backend/.env`

## 2. 后端本地打包

后端镜像不再在 Dockerfile 内执行 Maven 构建，必须先本地打包：

```bash
cd lz_sports_backend
mvn -DskipTests package
```

打包产物默认应为：

`target/lz_sports_backend-0.0.1-SNAPSHOT.jar`

## 3. 启动 Docker Compose

在项目根目录执行：

```bash
docker compose up -d --build
```

## 4. 访问地址

- 前端：`http://localhost`
- 后端：`http://localhost:8080`

## 5. 关键配置说明

- 编排文件：`docker-compose.yml`
- 后端镜像构建参数：
  - `JAR_FILE=target/lz_sports_backend-0.0.1-SNAPSHOT.jar`
- 后端环境变量来源：
  - `env_file: ./lz_sports_backend/.env`
  - 并覆盖 `DB_URL` 为 `.env` 里的 `DB_URL_DOCKER`
  - 并覆盖 `REDIS_HOST` 为 `.env` 里的 `REDIS_HOST_DOCKER`
- 上传目录映射：
  - 宿主机 `./data/uploads` -> 容器 `/app/uploads`

## 6. 常用运维命令

查看服务状态：

```bash
docker compose ps
```

查看后端日志：

```bash
docker compose logs -f backend
```

停止并删除容器：

```bash
docker compose down
```
