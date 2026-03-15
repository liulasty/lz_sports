# LZ Sports 容器化部署总览

本文档作为部署入口导航。  
已将使用说明拆分为两份独立文档：

- Docker Compose 使用说明：`README-docker-compose.md`
- Kubernetes 使用说明：`README-k8s.md`

## 适用范围

- 后端采用“本地打包 Jar，再构建镜像”模式
- 前端使用 Nginx 静态资源 + `/api/` 反向代理到 `backend:8080`
- 数据库与 Redis 默认连接宿主机（`host.docker.internal`）

## 快速入口

### Docker Compose

查看并执行：`README-docker-compose.md`

### Kubernetes

查看并执行：`README-k8s.md`
