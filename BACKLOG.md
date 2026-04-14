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
    status: DONE
    owner: agent
    area: backend
    dependencies: []
    block_reason: ""
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

  - id: BE-006
    title: 补齐赛事状态流转非容器边界测试
    priority: P0
    status: DONE
    owner: agent
    area: backend
    dependencies: []
    block_reason: ""
    acceptance:
      - "新增 EventServiceImplTest 覆盖发布前置校验边界"
      - "mvn -Pnon-container-baseline test 可通过"

  - id: BE-007
    title: 补齐系统初始化业务完整性与组织模式适配测试
    priority: P0
    status: DONE
    owner: agent
    area: backend
    dependencies: []
    block_reason: ""
    acceptance:
      - "新增 SchoolConfigServiceImplTest 覆盖重复初始化与默认模式"
      - "覆盖 UNIVERSITY/HIGH_SCHOOL 下部门字段映射适配"
      - "mvn -Pnon-container-baseline test 可通过"

  - id: BE-008
    title: 补齐系统重置业务完整性测试
    priority: P0
    status: DONE
    owner: agent
    area: backend
    dependencies: []
    block_reason: ""
    acceptance:
      - "SchoolConfigServiceImplTest 覆盖 resetSystem 的 initialized 重置"
      - "覆盖 resetSystem 对部门与非管理员用户清理行为"
      - "mvn -Pnon-container-baseline test 可通过"

  - id: BE-009
    title: 补齐初始化控制器校验与组织模式异常分支
    priority: P0
    status: DONE
    owner: agent
    area: backend
    dependencies: []
    block_reason: ""
    acceptance:
      - "新增 SystemInitControllerTest 覆盖 init-status/init 参数校验"
      - "SchoolConfigServiceImplTest 覆盖非法 orgMode 失败分支"
      - "mvn -Pnon-container-baseline test 可通过"

  - id: BE-010
    title: 补齐初始化失败路径与事务关键行为测试
    priority: P0
    status: DONE
    owner: agent
    area: backend
    dependencies: []
    block_reason: ""
    acceptance:
      - "SchoolConfigServiceImplTest 覆盖管理员创建失败分支"
      - "验证失败后不会继续写入部门数据"
      - "mvn -Pnon-container-baseline test 可通过"

  - id: BE-011
    title: 补齐初始化参数校验与系统重置控制器测试
    priority: P0
    status: DONE
    owner: agent
    area: backend
    dependencies: []
    block_reason: ""
    acceptance:
      - "SchoolInitDTO 增加 orgMode 约束并由服务层统一归一化"
      - "新增 SystemInitControllerTest 与 AdminSchoolConfigControllerTest 覆盖关键分支"
      - "mvn -Pnon-container-baseline test 可通过"

  - id: BE-012
    title: 补齐上传校徽 uploadLogo 非容器边界测试
    priority: P0
    status: DONE
    owner: agent
    area: backend
    dependencies:
      - BE-011
      - AUTO-003
    block_reason: ""
    acceptance:
      - "SchoolConfigServiceImplTest 覆盖空文件、超大小、无文件名、非法扩展名分支"
      - "边界分支返回 BusinessException 且 code=400"
      - "mvn -Pnon-container-baseline test 可通过"

  - id: BE-013
    title: 补齐上传校徽 uploadLogo 成功与异常捕获测试
    priority: P0
    status: DONE
    owner: agent
    area: backend
    dependencies:
      - BE-012
      - AUTO-003
    block_reason: ""
    acceptance:
      - "SchoolConfigServiceImplTest 覆盖合法上传成功路径并断言配置更新"
      - "覆盖底层更新异常被转换为业务异常上传失败"
      - "mvn -Pnon-container-baseline test 可通过"

  - id: BE-014
    title: 补齐上传校徽占位接口契约测试
    priority: P0
    status: DONE
    owner: agent
    area: backend
    dependencies:
      - BE-013
      - AUTO-003
    block_reason: ""
    acceptance:
      - "新增 uploadLogo 占位契约测试，覆盖返回值约束与调用时机"
      - "保留 imageUtils.upload 接口占位，不依赖真实图床"
      - "mvn test 可通过"

  - id: BE-015
    title: 补齐系统初始化到配置更新的业务流集成测试
    priority: P1
    status: DONE
    owner: agent
    area: backend
    dependencies:
      - BE-014
      - AUTO-003
    block_reason: ""
    acceptance:
      - "新增至少 2 条业务流集成测试，覆盖 初始化→更新配置→上传校徽→查询"
      - "外部依赖保持 Mock/占位，不引入真实图床依赖"
      - "mvn test 可通过"

  - id: BE-016
    title: 修复本地联调数据中赛事与项目关联断裂
    priority: P0
    status: DONE
    owner: agent
    area: backend
    dependencies: []
    block_reason: ""
    acceptance:
      - "报名可见项目的 eventId 在 event 表中可查询到有效赛事"
      - "运动员调用 /api/registration/apply/{projectId} 不再因赛事不存在失败"
      - "形成一条可复跑的 报名申请->取消报名 业务记录"

  - id: BE-017
    title: 修复报名重提与赛事管理员审核权限解析缺陷
    priority: P0
    status: DONE
    owner: agent
    area: backend
    dependencies:
      - BE-016
    block_reason: ""
    acceptance:
      - "取消后再次报名不再触发 registration.uk_user_item 唯一键冲突"
      - "RequireEventAdmin 优先按 eventId 参数解析，避免误用报名ID做赛事权限校验"
      - "mvn -Pnon-container-baseline test 可通过"

  - id: AUTO-001
    title: 自动化执行限定到 git-ai/automation-route 分治路径
    priority: P0
    status: DONE
    owner: agent
    area: docs
    dependencies: []
    block_reason: ""
    acceptance:
      - "仓库存在 git-ai/automation-route 路径并提供边界说明"
      - "明确自动化产物与路由约束，避免跨目录扩散"
      - "后续迭代优先在该路径内落地自动化文档与编排"

  - id: AUTO-002
    title: 初始化 automation-route 分治模板目录
    priority: P0
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-001
    block_reason: ""
    acceptance:
      - "git-ai/automation-route 下存在 plans/runs/policies 模板"
      - "模板包含最小字段规范，可直接用于后续迭代"
      - "不新增目录外自动化编排文件"

  - id: AUTO-003
    title: 产出首个 automation-route 真实计划与运行记录
    priority: P0
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-002
    block_reason: ""
    acceptance:
      - "plans 下存在首个真实任务计划文件"
      - "runs 下存在对应执行记录文件"
      - "计划明确验证命令与边界范围"

  - id: AUTO-004
    title: 建立发现不足输入源并标准化运行记录字段
    priority: P1
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-003
    block_reason: ""
    acceptance:
      - "git-ai/automation-route/runs 模板包含缺陷来源与修复归因字段"
      - "策略文件要求每轮运行记录沉淀缺陷输入字段"
      - "至少 1 条运行记录按新字段规范落地"

  - id: AUTO-005
    title: 前后端联调关键场景 smoke 脚本化
    priority: P2
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-004
    block_reason: ""
    acceptance:
      - "提供可复跑 smoke 脚本覆盖 登录/报名/成绩 关键路径"
      - "脚本支持 DryRun 与真实执行两种模式"
      - "提供最小使用说明与运行记录"

  - id: AUTO-006
    title: 完善 smoke token 使用说明与运行记录模板
    priority: P2
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-005
    block_reason: ""
    acceptance:
      - "README 明确 smoke 的 DryRun/真实执行/AccessToken 三种用法"
      - "runs 模板包含 token 来源记录字段"
      - "新增 1 条运行记录示例覆盖 token 模式"

  - id: AUTO-007
    title: 将 smoke 脚本接入统一测试脚本入口
    priority: P2
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-006
    block_reason: ""
    acceptance:
      - "scripts/test.bat 支持 smoke 子命令"
      - "scripts/test.sh 支持 smoke 子命令"
      - "提供最小执行示例并在运行记录中留痕"

  - id: AUTO-008
    title: 提升 smoke 登录与 token 兼容性
    priority: P2
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-007
    block_reason: ""
    acceptance:
      - "smoke 脚本兼容 data.token 结构返回"
      - "登录 409 时给出 AccessToken 明确引导"
      - "新增运行记录留痕脚本兼容性修复"

  - id: AUTO-009
    title: 固化注册实跑与验证码日志提取回放记录
    priority: P1
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-008
    block_reason: ""
    acceptance:
      - "形成一条可复盘运行记录，包含 send-verify-code -> verify-code -> register 真实链路"
      - "记录验证码来源与 token 来源，便于后续联调回放"
      - "复跑 smoke（登录/报名/成绩）通过"

  - id: AUTO-010
    title: 记录报名申请链路阻塞并定位根因
    priority: P1
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-009
    block_reason: ""
    acceptance:
      - "记录本地环境中报名申请失败的接口回放结果"
      - "明确根因与可执行修复任务"
      - "形成下一步可落地 backlog 项（BE-016）"

  - id: AUTO-011
    title: 回放管理员审核链路并沉淀修复结果
    priority: P1
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-010
      - BE-017
    block_reason: ""
    acceptance:
      - "event-admin 对报名 approve/refuse 链路回放通过"
      - "修复过程中出现的代码逻辑问题并补充对应测试"
      - "运行记录包含请求回放结果、根因和修复措施"

  - id: AUTO-012
    title: 回放批量审核与越权拦截负向场景
    priority: P1
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-011
    block_reason: ""
    acceptance:
      - "event-admin 批量审核（batch-audit approve=true）回放通过"
      - "非管理员调用 batch-audit 被正确拒绝（code=403）"
      - "运行记录沉淀批量审核前后状态变化"

  - id: AUTO-013
    title: 回放批量拒绝审核分支并确认状态落库
    priority: P1
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-012
    block_reason: ""
    acceptance:
      - "event-admin 调用 batch-audit approve=false 回放通过"
      - "目标记录状态由 PENDING 转为 REJECTED"
      - "运行记录沉淀拒绝分支回放结果"

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
