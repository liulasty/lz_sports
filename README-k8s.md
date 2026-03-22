# LZ Sports Kubernetes 入口

Kubernetes 说明已迁移至 `docs` 目录进行分治维护。

## 请查看

- 启动方式总览：`docs/启动方式/总览.md`
- Linux Docker 启动：`docs/启动方式/容器部署/LinuxDocker.md`

## 常用命令

```bash
kubectl apply -f k8s/backend-deployment.yaml
kubectl apply -f k8s/backend-service.yaml
kubectl apply -f k8s/frontend-deployment.yaml
kubectl apply -f k8s/frontend-service.yaml
```
