# BACKLOG (Machine Readable)

> version: 1
> status_enum: TODO | DOING | DONE | BLOCKED
> priority_enum: P0 | P1 | P2
> selection_rule: 按 priority 升序 + id 升序，选择第一个 status=TODO 且 dependencies 全部 DONE 且 block_reason 为空的任务

## tasks

```yaml
tasks:
  - id: FE-000
    title: 补齐前端质量基线（lint/test 脚本）
    priority: P0
    status: DONE
    owner: agent
    area: frontend
    dependencies: []
    block_reason: ""
    acceptance:
      - "lz_sports_frontend/package.json 存在 lint 与 test 脚本"
      - "npm run lint 可通过"
      - "npm test 可通过"

  - id: FE-001
    title: 扩大 lint 覆盖到 src/api 并清理存量问题
    priority: P0
    status: DONE
    owner: agent
    area: frontend
    dependencies:
      - FE-000
    block_reason: ""
    acceptance:
      - "lint 范围至少覆盖 src/api"
      - "npm run lint 可通过"
      - "不引入新错误"

  - id: FE-002
    title: 前端关键页面补充最小单测样例
    priority: P1
    status: DONE
    owner: agent
    area: frontend
    dependencies:
      - FE-001
    block_reason: ""
    acceptance:
      - "至少覆盖 登录/报名/成绩查询 中的关键渲染与交互断言"
      - "npm test 可通过"

  - id: FE-003
    title: 统一前端 API 异常处理与提示策略
    priority: P1
    status: DONE
    owner: agent
    area: frontend
    dependencies:
      - FE-001
    block_reason: ""
    acceptance:
      - "统一错误提示入口"
      - "关键请求具备可追踪错误信息"

  - id: BE-001
    title: 后端报名/成绩服务补充边界测试
    priority: P2
    status: BLOCKED
    owner: agent
    area: backend
    dependencies: []
    block_reason: "本地环境缺少 Docker，且现有集成测试（如 UserRegistrationTest）依赖 Testcontainers，连续 2 次执行 mvn test 均失败，无法完成全量通过验收。"
    acceptance:
      - "新增关键边界用例"
      - "mvn test 可通过"

  - id: BE-002
    title: 后端非容器全量可执行测试基线
    priority: P2
    status: DONE
    owner: agent
    area: backend
    dependencies:
      - CI-003
    block_reason: ""
    acceptance:
      - "形成无 Docker/Testcontainers 依赖的后端可执行测试基线清单"
      - "提供对应 Maven 命令并在本地可通过"
      - "为 CI-001 提供可依赖的后端测试基线"

  - id: BE-004
    title: 扩展后端非容器测试基线覆盖
    priority: P2
    status: DONE
    owner: agent
    area: backend
    dependencies:
      - BE-002
    block_reason: ""
    acceptance:
      - "新增至少 1 个服务层边界测试用例（无容器依赖）"
      - "mvn -Pnon-container-baseline test 可通过"

  - id: BE-005
    title: 扩展成绩服务非容器边界测试
    priority: P2
    status: DONE
    owner: agent
    area: backend
    dependencies:
      - BE-004
    block_reason: ""
    acceptance:
      - "ScoreServiceImplTest 新增至少 1 个边界用例"
      - "mvn -Pnon-container-baseline test 可通过"

  - id: DOC-001
    title: 统一前后端本地联调文档
    priority: P2
    status: DONE
    owner: agent
    area: docs
    dependencies: []
    block_reason: ""
    acceptance:
      - "前后端启动与联调步骤一致"
      - "常见故障排查可复现"

  - id: DOC-002
    title: 同步 README 的非容器测试与 CI 基线说明
    priority: P2
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - BE-002
      - CI-001
    block_reason: ""
    acceptance:
      - "README 明确非容器后端测试基线命令"
      - "README 明确统一 CI 工作流入口"

  - id: DOC-003
    title: 补充后端非容器测试基线执行与扩展规范
    priority: P2
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - BE-004
    block_reason: ""
    acceptance:
      - "提供基线执行步骤与故障排查清单"
      - "提供新增测试纳入基线的规范"

  - id: CI-001
    title: 增加基础 CI 流水线（前端+后端）
    priority: P2
    status: DONE
    owner: agent
    area: ci
    dependencies:
      - FE-001
      - BE-002
      - CI-002
      - CI-003
    block_reason: ""
    acceptance:
      - "前端 lint/test 自动执行"
      - "后端 mvn test 自动执行"

  - id: CI-002
    title: 增加前端基础 CI 流水线（lint+test）
    priority: P2
    status: DONE
    owner: agent
    area: ci
    dependencies:
      - FE-001
    block_reason: ""
    acceptance:
      - "前端 lint 自动执行"
      - "前端 npm test 自动执行"

  - id: CI-003
    title: 增加后端定向测试 CI（无 Docker 依赖）
    priority: P2
    status: DONE
    owner: agent
    area: ci
    dependencies: []
    block_reason: ""
    acceptance:
      - "后端定向测试在 CI 自动执行"
      - "不依赖 Docker/Testcontainers"
```

## update_rules

- 更新状态时仅修改对应任务字段：`status`、`block_reason`、`dependencies`。
- 当任务进入 `BLOCKED`，必须填写 `block_reason`。
- 当任务从 `BLOCKED` 恢复，必须清空 `block_reason`。
- 新增任务时，`id` 必须全局唯一，推荐前缀：`FE/BE/DOC/CI`。
