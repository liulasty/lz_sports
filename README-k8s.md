# LZ Sports Kubernetes 使用说明

## 1. 前提条件

- 已安装并启用 Kubernetes（例如 Docker Desktop Kubernetes）
- `kubectl` 可正常连接当前集群
- 宿主机已启动 MySQL 和 Redis（默认按 `host.docker.internal` 访问）
- 本地已构建两张镜像：
  - `lz-sports-backend:latest`
  - `lz-sports-frontend:latest`

## 2. 本地构建镜像

### 2.1 后端先本地打包再构建

```bash
cd lz_sports_backend
mvn -DskipTests package
cd ..
docker build -t lz-sports-backend:latest ./lz_sports_backend
```

### 2.2 前端构建

```bash
docker build -t lz-sports-frontend:latest ./lz_sports_frontend
```

## 3. 部署到 Kubernetes

在项目根目录执行：

```bash
kubectl apply -f k8s/backend-deployment.yaml
kubectl apply -f k8s/backend-service.yaml
kubectl apply -f k8s/frontend-deployment.yaml
kubectl apply -f k8s/frontend-service.yaml
```

## 4. 访问地址

- 前端 NodePort：`http://localhost:30080`
- 后端 Service（集群内）：`backend:8080`

## 5. 当前 K8s 配置说明

- 后端配置已内置在 `k8s/backend-deployment.yaml`：
  - `ConfigMap`：非敏感配置（`DB_URL`、`REDIS_HOST`、`APP_UPLOAD_DIR` 等）
  - `Secret`：敏感配置（数据库密码、JWT、OSS 密钥等）
- 后端上传目录挂载：
  - 容器 `/app/uploads`
  - `hostPath: /data/lz-sports/uploads`
- 前后端镜像策略：
  - `imagePullPolicy: Never`
  - 适用于本地集群直接使用本地镜像

## 6. 上线前必改项

- 修改 `k8s/backend-deployment.yaml` 中 Secret 的默认值
- 若不是 Docker Desktop，请替换 `host.docker.internal` 为实际可达地址
- 生产环境不建议使用 `hostPath`，建议改为 PV/PVC

## 7. 常用排障命令

查看 Pod：

```bash
kubectl get pods
```

查看 Service：

```bash
kubectl get svc
```

查看后端日志：

```bash
kubectl logs -f deploy/lz-sports-backend
```

删除部署：

```bash
kubectl delete -f k8s/backend-deployment.yaml -f k8s/backend-service.yaml -f k8s/frontend-deployment.yaml -f k8s/frontend-service.yaml
```
