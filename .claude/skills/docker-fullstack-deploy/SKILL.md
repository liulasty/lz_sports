---
name: docker-fullstack-deploy
description: Design and generate complete Docker containerization and deployment systems for Spring Boot + Vue full-stack applications. Use this skill whenever the user mentions Docker, containerization, deployment scripts, docker-compose, multi-environment config, .env files, image versioning, multi-arch builds, or wants to "deploy", "dockerize", "ship", or "run" a Spring Boot or Vue project. Also trigger when the user asks about cross-platform startup scripts (.sh/.bat/.ps1), semantic versioning for images, or unified configuration management across dev/prod environments.
---

# Docker Full-Stack Deployment System

本 Skill 提供了一套针对 Spring Boot + Vue 全栈应用的 Docker 容器化和部署架构规范。支持多环境（开发/生产）、自动版本控制、健康检查与容错回滚，并提供一键式的跨平台部署脚本。

## 核心架构原则

1. **分层 Compose 架构**：使用 `docker-compose.yml` 作为基础骨架（定义网络、镜像、健康检查），通过 `docker-compose.dev.yml` 与 `docker-compose.prod.yml` 进行环境覆盖。
2. **统一环境变量**：通过 `config/.env.dev` 和 `config/.env.prod` 集中管理配置，后端通过 `SPRING_PROFILES_ACTIVE` 读取。
3. **自动化测试验证**：提供本地、CI 和生产三个阶段的自动化测试脚本。
4. **跨平台脚手架**：所有操作通过 `scripts/` 目录下的 `.sh`、`.bat`、`.ps1` 脚本完成。

## 使用指南

### 1. 配置文件与环境定义

参考 `references/architecture.md` 了解环境变量的统一命名规范与开发/生产环境的区别。

### 2. Docker Compose 文件组装

- 基础依赖和健康检查配置在 `docker-compose.yml` 中
- 开发环境的实时日志和端口暴露在 `docker-compose.dev.yml` 中
- 生产环境的资源限制与自动重启配置在 `docker-compose.prod.yml` 中

### 3. 部署与管理脚本

所有操作通过 `scripts/` 目录下的跨平台脚本完成：
- `deploy.sh` / `deploy.bat` - 一键部署
- `build.sh` / `build.bat` - 构建镜像
- `test.sh` / `test.ps1` / `test.bat` - 自动化验证

## 高级特性

- **健康检查与容错回滚**：参阅 `references/architecture.md` 了解容器健康检查配置和部署失败时的回滚机制。
- **CI/CD 集成**：参阅 `references/testing.md` 了解自动化测试集成方式。

## Resources

- [架构详解](references/architecture.md)
- [脚本说明](references/scripts.md)
- [测试规范](references/testing.md)
