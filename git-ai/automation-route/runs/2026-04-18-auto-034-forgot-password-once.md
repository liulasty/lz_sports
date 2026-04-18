run_id: 2026-04-18-auto-034-forgot-password-once
related_task_id: AUTO-033
changed_files:
  - lz_sports_backend/src/main/java/com/lz/service/impl/UserServiceImpl.java
  - lz_sports_frontend/src/views/login/ForgotPassword.vue
key_changes:
  - Fixed ForgotPassword verifyToken passing chain: send-code token is persisted on frontend and passed to verify-code, then reset-password uses verify response token only.
  - Enforced reset-password token one-time usage on backend via blacklist key (blacklist:reset-token:<token>).
  - Unified send-verify-code behavior for anti-enumeration: no registered-email existence leak in response.
test_results:
  - send_verify_code_existing_email: code=200
  - send_verify_code_non_existing_email: code=200
  - reset_password_first_use: code=200
  - reset_password_second_use_same_token: code=400
  - login_old_password_after_reset: code=409
  - login_new_password_after_reset: code=200
  - password_restore_to_original: code=200
risks:
  - Reset token blacklist TTL is 10 minutes; adjust if product requires longer replay protection windows.
  - Existing register flow still shares verify-code endpoint; ensure API docs clarify token purpose in different scenes.
next_task: "Add dedicated reset-password token endpoint (scene-specific claim) and E2E test script for ForgotPassword page."
