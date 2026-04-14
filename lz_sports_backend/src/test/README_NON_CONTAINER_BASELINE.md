# 后端非容器测试基线（git-ai 分支）

## 目标

在不依赖 Docker / Testcontainers 的前提下，提供可稳定执行的后端测试基线，作为本地调试与 CI 的最小质量门禁。

## 统一执行命令

```bash
cd lz_sports_backend
mvn -Pnon-container-baseline test
```

## 当前纳入测试

### `RegistrationServiceImplTest`
- 空审核列表返回提示：`batchAuditShouldReturnHintWhenIdsEmpty`
- 审核结果“处理/跳过”统计：`batchAuditShouldCountSuccessAndSkip`
- 取消他人报名返回 403：`cancelShouldRejectOtherUsersRegistration`
- 取消不存在报名返回异常：`cancelShouldThrowWhenRegistrationNotFound`

### `ScoreServiceImplTest`
- 无可发布成绩时报错：`publishScoresShouldThrowWhenNoUnpublishedScores`
- 录入成绩时报名不存在：`upsertScoreShouldThrowWhenRegistrationNotFound`
- 更新成绩时成绩不存在：`updateScoreShouldThrowWhenScoreNotFound`
- 已发布成绩不可修改：`updateScoreShouldThrowWhenScorePublished`

## CI 对应关系

- 统一流水线：`.github/workflows/ci.yml`
- 后端定向流水线：`.github/workflows/backend-smoke-ci.yml`
- 两者均使用 `mvn -Pnon-container-baseline test`

## 扩展规则

新增测试纳入基线时需满足：

1. 不依赖 Docker/Testcontainers
2. 可通过 mock/stub 隔离数据库、缓存、外部服务
3. 优先补业务边界与异常分支
4. 纳入后必须本地执行基线命令通过
