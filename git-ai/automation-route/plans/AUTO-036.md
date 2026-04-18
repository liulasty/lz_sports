task_id: AUTO-036
goal: 盘点当前项目的主要业务与接口现状，并将 heartbeat 自动化聚焦到业务测试与缺陷修复。
scope_in_repo:
  - lz_sports_backend/src/main/java/com/lz/controller/**
  - README.md
  - docs/InterfaceDesign.md
  - git-ai/automation-route/BUSINESS_STATUS.md
  - BACKLOG.md
out_of_scope:
  - 直接重构业务代码
  - 前端页面逐页视觉巡检
validation_commands:
  - 人工核对控制器接口与 BUSINESS_STATUS 文档一致
  - 更新 heartbeat 自动化 prompt 以 BUSINESS_STATUS 为输入继续回归
rollback_notes: 如果业务现状文档与当前代码不符，回退文档与 backlog 记录，重新以控制器源码为准生成。
