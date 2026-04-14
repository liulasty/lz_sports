run_id: 2026-04-15-business-onboarding
scope:
  - boot frontend on 5173
  - boot backend on 8080
  - system reset + initialization
  - register multiple users via verify-code flow
  - assign business roles by super admin

service_status:
  frontend: "http://localhost:5173 => 200"
  backend: "http://localhost:8080/api/system/init-status => code=200,data=true"

initialization:
  reset_api: "POST /api/admin/school-config/reset => code=200"
  init_api: "POST /api/system/init => code=200"
  init_payload:
    schoolName: "LZ Sports QA School"
    adminUsername: "init_school_admin_01"
    orgMode: "UNIVERSITY"

verification_log_extracts:
  - "2026-04-15 01:28:57 验证码: 416043"
  - "2026-04-15 01:29:19 验证码: 377079"
  - "2026-04-15 01:29:35 验证码: 221175"

accounts:
  - username: "ops_super_seed"
    password: "Pass12345"
    role: "SUPER_ADMIN"
    status: "ACTIVE"
    source: "registered via /api/auth/register then promoted to SUPER_ADMIN for bootstrap"
  - username: "init_school_admin_01"
    password: "Admin12345"
    role: "SCHOOL_ADMIN"
    status: "ACTIVE"
    source: "created by /api/system/init"
  - username: "ops_user_a"
    password: "Pass12345"
    role: "USER"
    status: "ACTIVE"
    source: "registered from verification flow, role assigned by super admin"
  - username: "ops_event_admin_a"
    password: "Pass12345"
    role: "EVENT_ADMIN"
    status: "ACTIVE"
    source: "registered from verification flow, role assigned by super admin"
  - username: "ops_athlete_b"
    password: "Pass12345"
    role: "ATHLETE"
    status: "ACTIVE"
    source: "registered from verification flow"

notes:
  - "为了复用同一个可用 QQ 邮箱进行多次注册，历史账号邮箱被改写到 placeholder.local。"
  - "角色分配使用接口 /api/admin/users/{id}/role。"
