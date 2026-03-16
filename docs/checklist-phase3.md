# Phase 3 自检清单 · 成绩与通知

> 全部为 [x] 才允许进入 Phase 4

---

## 🏅 成绩录入

- [x] 只能为 CONFIRMED 状态的报名录入成绩，其他状态返回 409
- [x] 重复录入同一报名的成绩时执行更新（upsert，不报错）
- [x] 已发布的成绩不可修改，返回 409
- [x] 草稿成绩通过用户侧接口查询时不返回
- [x] result 表的 item_registration_id 有唯一索引（一条报名一条成绩）

---

## 📢 成绩发布

- [x] 发布后 is_published 置为 1，published_at 记录发布时间
- [x] 发布后成绩不可修改（Service 层拦截）
- [x] 发布后异步向该赛事所有运动员发站内通知
- [x] 发布操作不可撤回（无撤回接口）
- [x] 前端发布前弹出二次确认弹窗，用户点确认才执行

---

## 📊 Excel 导入

- [x] 下载模板接口正确生成含已通过报名数据的 Excel
- [ ] 导入时逐行校验：
  - [x] 报名ID是否存在且属于该赛事
  - [x] 报名状态是否为 CONFIRMED
  - [x] 成绩字段是否为空
  - [x] 该成绩是否已发布（已发布不可覆盖）
- [x] 部分行失败时其他行正常导入（不因一行失败回滚全部）
- [x] 导入结果返回：成功N条、失败N条、失败详情（行号+原因）
- [x] 上传文件大小限制 ≤ 10MB，超出返回 400

---

## 📤 Excel 导出

- [x] 导出成绩表按项目分 Sheet
- [x] 导出报名名单包含：序号、姓名、院系、年级、项目、报名时间、状态
- [x] 导出成绩表包含：排名、姓名、院系、年级、成绩、备注
- [x] 导出文件名包含赛事名称和导出时间
- [x] 导出响应头正确设置（Content-Disposition、Content-Type）

---

## 🔔 站内信

- [x] 发送通知时 sys_user.unread_count + 1（原子 UPDATE）
- [x] 标记单条已读时 unread_count - 1
- [x] 全部标记已读时 unread_count 直接置为 0
- [x] 只能标记自己的通知，标记他人通知返回 403
- [x] 未读消息数量接口直接读取 unread_count 字段（不实时统计）
- [x] 批量发送通知（赛事发布/成绩发布）使用异步 @Async
- [ ] 以下场景全部触发通知：
  - [x] 赛事发布 → 所有注册用户
  - [x] 运动员申请通过 → 申请人
  - [x] 运动员申请拒绝 → 申请人（含原因）
  - [x] 成绩发布 → 该赛事所有运动员

---

## 🎨 前端

- [x] 导航栏通知铃铛显示未读数量角标
- [x] 角标数量为 0 时不显示角标
- [x] 未读消息数量每 30 秒轮询一次
- [x] 点击通知铃铛展开通知列表抽屉
- [x] 通知列表已读/未读样式区分明显
- [x] 点击通知标记为已读，角标数量同步减少
- [x] 「全部已读」按钮正常工作，角标清零
- [x] 我的成绩页只显示已发布的成绩
- [x] 成绩管理页草稿成绩和已发布成绩有视觉区分

---

## 代码质量

- [x] 无遗留 System.out.println / console.log
- [x] Excel 导入的行校验异常被捕获，不影响其他行处理
- [x] 批量发通知的方法有 @Async 注解
- [x] 文件流在 finally 块中关闭（无资源泄漏）

---

## ✅ 执行记录

- ✅ 已完成：[lz_sports_backend/src/main/resources/sql/20260316_phase3_result_notification.sql] - 新增 phase3 迁移，补齐 result 发布字段、sys_user.unread_count、索引
- ✅ 已完成：[lz_sports_backend/src/main/java/com/lz/controller/ScoreController.java] - 新增成绩录入/修改/发布/导入导出/用户侧成绩与公开榜接口
- ✅ 已完成：[lz_sports_backend/src/main/java/com/lz/service/impl/ScoreServiceImpl.java] - 实现 CONFIRMED 校验、upsert、已发布拦截、导入逐行校验与失败明细、发布后异步通知
- ✅ 已完成：[lz_sports_backend/src/main/java/com/lz/mapper/ScoreMapper.java,resources/mapper/ScoreMapper.xml] - 增加 upsert SQL、管理员查询、公开榜查询、按项目导出数据能力
- ✅ 已完成：[lz_sports_backend/src/main/java/com/lz/controller/NotificationController.java] - 新增通知分页、单条已读、全部已读、未读数接口
- ✅ 已完成：[lz_sports_backend/src/main/java/com/lz/service/impl/NotificationServiceImpl.java] - 实现 sendNotification/batchSendNotification/markRead/markAllRead/unreadCount 与 unread_count 原子更新
- ✅ 已完成：[lz_sports_frontend/src/layout/components/Navbar.vue] - 通知铃铛角标、30秒轮询、通知抽屉、单条/全部已读联动
- ✅ 已完成：[lz_sports_frontend/src/views/admin/ScoreManage.vue] - 新增成绩管理页，支持项目切换、草稿/已发布区分、导入结果明细、发布二次确认
- ✅ 已完成：[lz_sports_frontend/src/views/score/index.vue] - 我的成绩改为仅查询已发布成绩
- ✅ 已完成：[lz_sports_frontend/src/api/notification.js,src/api/score.js,src/router/index.js,src/layout/components/Sidebar/index.vue] - 完成前端接口与路由接入
