# 后端非容器测试基线（BE-002）

## 目标

在不依赖 Docker / Testcontainers 的前提下，提供一组可稳定执行的后端测试基线，作为本地调试与 CI 的最小质量门禁。

## 基线测试范围

当前纳入以下测试类：

- `com.lz.service.impl.RegistrationServiceImplTest`
  - 覆盖空批量审核入参提示
  - 覆盖越权取消报名（403）边界
- `com.lz.service.impl.ScoreServiceImplTest`
  - 覆盖“无可发布成绩”业务边界

## 运行命令

### 推荐命令（统一 profile）

```bash
cd lz_sports_backend
mvn -Pnon-container-baseline test
```

> 说明：`non-container-baseline` profile 已在 `pom.xml` 中固定包含上述测试类。

### 兼容命令（显式指定测试类）

```bash
cd lz_sports_backend
mvn "-Dtest=ScoreServiceImplTest,RegistrationServiceImplTest" test
```

## 暂未纳入基线的测试

以下测试当前依赖 Docker/Testcontainers 或完整集成环境，不纳入“非容器基线”：

- `UserRegistrationTest`
- `RegistrationConcurrencyTest`
- `UserAuditNotificationTest`

## 与 CI 的关系

- GitHub Actions 工作流：`.github/workflows/backend-smoke-ci.yml`
- 当前 workflow 直接执行：
  - `mvn -Pnon-container-baseline test`

后续当容器依赖问题解除后，可逐步将测试范围并入 `CI-001` 的统一前后端流水线。
