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

  - id: BE-018
    title: 修复驳回记录误计入有效报名导致无法重提
    priority: P0
    status: DONE
    owner: agent
    area: backend
    dependencies:
      - BE-017
    block_reason: ""
    acceptance:
      - "REJECTED/CANCELLED 记录均可被重新报名复用，不阻塞重提"
      - "报名上限与冲突校验只统计 PENDING/APPROVED/CONFIRMED"
      - "mvn -Pnon-container-baseline test 可通过"

  - id: BE-019
    title: 修复通知分页参数越界导致结果不一致
    priority: P0
    status: DONE
    owner: agent
    area: backend
    dependencies:
      - BE-018
    block_reason: ""
    acceptance:
      - "notification/page 对 currentPage<1 自动归一为 1"
      - "notification/page 对 pageSize<1 使用默认值并限制上限"
      - "分页 total 与 records 不再出现越界参数下的不一致"

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

  - id: AUTO-014
    title: 验证审核通知链路并沉淀重提修复结果
    priority: P1
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-013
      - BE-018
    block_reason: ""
    acceptance:
      - "审核通过/拒绝后通知可查询且未读数按预期增长"
      - "修复 REJECTED 误计入有效报名逻辑并通过基线测试"
      - "运行记录沉淀通知验证与修复细节"

  - id: AUTO-015
    title: 回放通知已读链路并确认未读数回落
    priority: P1
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-014
    block_reason: ""
    acceptance:
      - "单条已读接口调用后未读数减少 1"
      - "全部已读接口调用后未读数归零"
      - "通知列表中已验证记录 isRead=true"

  - id: AUTO-016
    title: 回放通知接口鉴权负向场景
    priority: P1
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-015
    block_reason: ""
    acceptance:
      - "无 token 访问通知接口被拒绝（code=401）"
      - "无效 token 访问通知接口被拒绝（code=401）"
      - "运行记录沉淀负向鉴权验证结果"

  - id: AUTO-017
    title: 验证跨角色通知隔离与越权拦截
    priority: P1
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-016
    block_reason: ""
    acceptance:
      - "管理员通知列表不包含运动员通知"
      - "管理员标记他人通知已读被拒绝（code=403）"
      - "运行记录沉淀跨角色隔离验证结果"

  - id: AUTO-018
    title: 验证通知分页与已读筛选一致性
    priority: P1
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-017
    block_reason: ""
    acceptance:
      - "notification/page 全量 total 与 read/unread total 可加和"
      - "notification/unread-count 与 unread 筛选 total 一致"
      - "运行记录沉淀分页筛选一致性验证结果"

  - id: AUTO-019
    title: 回放通知分页边界并验证参数归一化
    priority: P1
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-018
      - BE-019
    block_reason: ""
    acceptance:
      - "currentPage=0/-1 与 currentPage=1 行为一致"
      - "pageSize=0/-5 返回稳定 total 与 records（按默认分页策略）"
      - "运行记录沉淀边界回放与修复结果"

  - id: AUTO-020
    title: 验证通知分页 pageSize 上限归一化
    priority: P1
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-019
    block_reason: ""
    acceptance:
      - "pageSize=101/999 与 pageSize=100 在结果规模上保持一致"
      - "分页 total 在不同超限 pageSize 参数下保持一致"
      - "运行记录沉淀上限归一化验证结果"

  - id: AUTO-021
    title: 脚本化通知链路 smoke 并接入统一入口
    priority: P1
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-020
    block_reason: ""
    acceptance:
      - "新增 scripts/smoke-notification.ps1 覆盖通知分页一致性与鉴权负向"
      - "scripts/test.bat 与 scripts/test.sh 支持 notify-smoke 子命令"
      - "README 提供通知 smoke 的最小执行示例"

  - id: AUTO-022
    title: 提供一键全链路 smoke 串行入口
    priority: P1
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-021
    block_reason: ""
    acceptance:
      - "新增 scripts/smoke-full.ps1 串行执行 linkup 与 notification smoke"
      - "scripts/test.bat 与 scripts/test.sh 支持 full-smoke 子命令"
      - "README 补充全链路 smoke 执行示例"

  - id: AUTO-023
    title: 固化自动化分支与路径约束策略
    priority: P2
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-022
    block_reason: ""
    acceptance:
      - "新增分支范围策略文档，明确自动化默认在 git-ai/automation-route 执行"
      - "策略覆盖 worktree 占用时的安全迁移流程"
      - "新增对应运行记录，沉淀问题来源与修复措施"

  - id: AUTO-024
    title: 本地全链路回归（full-smoke + 多角色接口验证）
    priority: P0
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-023
    block_reason: ""
    acceptance:
      - "前端在 5173、后端在 8080 可访问"
      - "full-smoke（linkup+notification）以现有账号实跑通过"
      - "分别以 SUPER_ADMIN / SCHOOL_ADMIN / EVENT_ADMIN / USER / ATHLETE 登录，验证至少 1 条正向权限与 1 条越权拦截（403/401）"
      - "新增一条 runs 记录包含 token_source 与回归结论"

  - id: AUTO-025
    title: 固化 EVENT_ADMIN 与 eventId 绑定，使 RequireEventAdmin 正向路径可稳定回归
    priority: P0
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-024
    block_reason: ""
    acceptance:
      - "role smoke 运行时自动将 EVENT_ADMIN 绑定到指定 eventId（默认 1），然后 batch-audit 正向断言可通过"
      - "新增对应 runs 记录沉淀绑定与断言结果"

  - id: AUTO-026
    title: 新增一键回归脚本（分支检查+端口检查+full-smoke+rolepaths+自动runs）
    priority: P0
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-025
    block_reason: ""
    acceptance:
      - "新增 scripts/smoke-oneclick.ps1，可一键执行 full-smoke + rolepaths 并自动生成 runs 记录"
      - "scripts/test.bat 与 scripts/test.sh 支持 oneclick 子命令"
      - "一键脚本在 git-ai/automation-route 分支上实跑通过"

  - id: AUTO-027
    title: 修复本地启动预检与环境加载，降低下次开工成本
    priority: P0
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-026
    block_reason: ""
    acceptance:
      - "新增 dev-start 脚本，自动加载 config/.env.dev 并做本地化变量适配"
      - "启动前自动清理 8080/5173 监听冲突（可关闭）"
      - "支持 scripts/test.bat 与 scripts/test.sh 子命令直达"

  - id: AUTO-028
    title: 补齐 dev-stop 与本地文档闭环，并重启业务回归
    priority: P0
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-027
    block_reason: ""
    acceptance:
      - "新增 scripts/dev-stop.ps1，按端口停止前后端"
      - "README 与本地开发文档包含 dev-start/dev-stop 指引"
      - "完成一次 oneclick 业务回归，形成运行记录"

  - id: AUTO-029
    title: 执行 oneclick 开工回归并沉淀运行记录
    priority: P2
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-028
    block_reason: ""
    acceptance:
      - "在 git-ai/automation-route 分支（或对应 worktree）执行 oneclick 回归"
      - "full-smoke 与 rolepaths 均通过"
      - "新增对应 runs 记录并包含回归结论"

  - id: AUTO-030
    title: 复跑 oneclick 回归并沉淀最新运行记录
    priority: P2
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-029
    block_reason: ""
    acceptance:
      - "再次执行 oneclick 并通过分支/端口/full-smoke/rolepaths 全链路检查"
      - "新增当日 runs 记录用于本轮统一执行循环留痕"

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

  - id: CI-004
    title: 统一仓库根目录构建产物忽略规则
    priority: P0
    status: DONE
    owner: agent
    area: ci
    dependencies: []
    block_reason: ""
    acceptance:
      - "根 .gitignore 显式忽略 lz_sports_backend/target 与 lz_sports_frontend/dist"
      - "从仓库根执行 git status 不再暴露构建产物未跟踪噪音"

  - id: CI-005
    title: 统一仓库换行与文本属性策略
    priority: P0
    status: DONE
    owner: agent
    area: ci
    dependencies: []
    block_reason: ""
    acceptance:
      - "仓库根新增 .gitattributes 并定义默认文本行尾策略"
      - "显式约束 Windows 脚本文件保持 CRLF"
      - "二进制资源采用 binary 属性，避免误判文本"
  - id: AUTO-031
    title: 统一执行循环复跑 oneclick 并沉淀最新运行记录
    priority: P2
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-030
    block_reason: ""
    acceptance:
      - "在 git-ai/automation-route 分支（或对应 worktree）执行 oneclick 回归"
      - "full-smoke 与 rolepaths 均通过，失败先自修复后重试"
      - "新增当日 runs 记录并按统一执行循环字段汇报"
  - id: AUTO-032
    title: 通过真实注册链重建多角色业务测试账号资产
    priority: P2
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-031
    block_reason: ""
    acceptance:
      - "使用 send-verify-code -> verify-code -> register 真实注册三类账号"
      - "学校管理员审核注册账号，超级管理员设置赛事管理员，赛事管理员审核运动员申请"
      - "产出可复用的明文账号说明与 token 资产文件"
  - id: AUTO-033
    title: 统一执行循环下复跑 oneclick 并校验最新角色资产不影响主回归
    priority: P2
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-032
    block_reason: ""
    acceptance:
      - "在 git-ai/automation-route 分支执行 oneclick 回归"
      - "full-smoke 与 rolepaths 均通过"
      - "新增一条 runs 记录沉淀本轮结论"
  - id: AUTO-034
    title: 验证当前赛事的完整业务链路
    priority: P2
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-033
    block_reason: ""
    acceptance:
      - "oneclick 回归通过"
      - "基于最新 EVENT_ADMIN / USER / ATHLETE 账号回放当前赛事完整链路"
      - "新增对应 runs 记录沉淀结论"
  - id: AUTO-035
    title: 建立业务完整性循环执行闭环并复跑整体验证
    priority: P0
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-034
    block_reason: ""
    acceptance:
      - "补齐 git-ai/automation-route/WORKFLOW_OPEN_CLOSE_SMOKE.md，明确 dev-start -> suite -> fix -> rerun -> dev-stop 的统一循环"
      - "在 git-ai/automation-route 分支或对应 worktree 复跑 suite，覆盖基线 oneclick + 当前赛事完整业务链路"
      - "若 suite 失败，先修复并重跑；若 suite 通过，新增 runs 记录沉淀当前业务完整性结论"
  - id: AUTO-036
    title: 盘点主要业务接口现状并将自动化聚焦到业务测试与缺陷修复
    priority: P0
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-035
    block_reason: ""
    acceptance:
      - "输出一份基于当前控制器实际接口的业务现状文档，覆盖系统初始化、认证、运动员、赛事、项目、报名、成绩、通知、管理端统计等主要业务"
      - "文档明确当前自动化已覆盖链路、未覆盖链路和优先缺陷入口"
      - "将 heartbeat 自动化提示词更新为以业务测试和 bug 修复为主，而不只是泛化回归"
  - id: AUTO-037
    title: 统一执行循环复跑 suite 并沉淀本轮业务完整性记录
    priority: P2
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-036
    block_reason: ""
    acceptance:
      - "在 git-ai/automation-route 分支或对应 worktree 执行 dev-start -> suite -> dev-stop"
      - "若 suite 失败，先修复并重跑；若 suite 通过，新增一条 runs 记录沉淀本轮业务完整性结论"
      - "回写 backlog 状态，并补下一条最小可执行任务"
  - id: AUTO-038
    title: 扩展业务完整性回归到报名拒绝分支
    priority: P2
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-037
    block_reason: ""
    acceptance:
      - "在现有 suite 或定向 smoke 中覆盖 EVENT_ADMIN 拒绝报名分支"
      - "验证 REJECTED 后通知与报名状态查询符合预期"
      - "新增对应 runs 记录并保持主回归可复跑"
  - id: AUTO-039
    title: 扩展业务回归到用户禁用启用与登录失败校验
    priority: P2
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-038
    block_reason: ""
    acceptance:
      - "覆盖 SCHOOL_ADMIN 禁用普通用户后登录失败的定向验证"
      - "覆盖重新启用后恢复登录成功"
      - "新增对应 runs 记录并保持账号资产生成流程不被破坏"
  - id: AUTO-040
    title: 扩展业务回归到赛事管理最小闭环
    priority: P2
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-039
    block_reason: ""
    acceptance:
      - "覆盖创建 DRAFT 赛事、绑定管理员、发布 OPEN 的最小管理端闭环"
      - "验证赛事状态变化对后续报名链路的影响符合当前实现"
      - "新增对应 runs 记录并形成可复跑脚本或命令"
  - id: AUTO-041
    title: 扩展业务回归到成绩录入与发布最小闭环
    priority: P2
    status: DONE
    owner: agent
    area: docs
    dependencies:
      - AUTO-040
    block_reason: ""
    acceptance:
      - "覆盖 score upsert 到 publish 的最小正向链路"
      - "验证 my/public 成绩查询在发布前后符合预期"
      - "新增对应 runs 记录并保持主回归可复跑"
  - id: AUTO-042
    title: 检查项目与部门接口的权限边界和数据污染风险
    priority: P2
    status: TODO
    owner: agent
    area: docs
    dependencies:
      - AUTO-041
    block_reason: ""
    acceptance:
      - "验证 department 增删改接口的权限与污染风险"
      - "验证 project 管理接口对非管理员角色的访问边界"
      - "若发现问题，形成运行记录并补最小修复或明确缺陷项"
```

## update_rules

- 更新状态时仅修改对应任务字段：`status`、`block_reason`、`dependencies`。
- 当任务进入 `BLOCKED`，必须填写 `block_reason`。
- 当任务从 `BLOCKED` 恢复，必须清空 `block_reason`。
- 新增任务时，`id` 必须全局唯一，推荐前缀：`FE/BE/DOC/CI`。

