# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

LZ Sports is a campus sports event management platform — a monorepo with two independent projects:

- `lz_sports_backend/` — Spring Boot 3.2.3, Java 17, MyBatis-Plus 3.5.5, MySQL, Redis/Redisson, JWT auth
- `lz_sports_frontend/` — Vue 3.5, Vite 7.3, Element Plus, Pinia, Vue Router, Axios

The frontend proxies `/api` to `http://localhost:8080` via Vite dev server (see `vite.config.js`). Both projects run on the same machine during development.

## Essential Commands

### Backend (run from `lz_sports_backend/`)
```bash
mvn spring-boot:run                  # Start dev server (port 8080 by default)
mvn test                             # Run all tests (some require Docker/Testcontainers)
mvn -Pnon-container-baseline test    # Run only tests that don't need Docker (CI baseline)
```

### Frontend (run from `lz_sports_frontend/`)
```bash
npm run dev          # Start Vite dev server (port 5173 by default, override with FRONTEND_PORT env)
npm run build        # Production build to dist/
npm run lint         # ESLint on key files (main.js, api/**, router, vite config)
npm test             # Vitest (--passWithNoTests, so it won't fail with no test files)
```

### Dev workflow scripts (run from repo root)
```bash
powershell -ExecutionPolicy Bypass -File scripts/dev-start.ps1   # Auto-load .env, kill port conflicts, start both services
powershell -ExecutionPolicy Bypass -File scripts/dev-stop.ps1    # Stop both services by port
powershell -ExecutionPolicy Bypass -File scripts/smoke-oneclick.ps1  # Full regression: branch check + ports + full-smoke + rolepaths
```

Unified entry points: `scripts/test.bat <subcommand>` (Windows) or `./scripts/test.sh <subcommand>` (Linux).

### Environment variables (config/.env.dev for local)
Key env vars: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PORT`, `JWT_SECRET`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `SERVER_PORT`, `FRONTEND_PORT`.

## Architecture

### Backend (`com.lz`)

**Auth flow**: `JwtAuthenticationFilter` (once-per-request) extracts token from `token` header or `Authorization: Bearer`, validates via `JwtUtil` + Redis (`auth:token:<userId>`), sets Spring Security context with role-based authorities. Public paths are listed in both `SecurityConfig.PUBLIC_PATHS` and the filter itself. First-login users are forced to reset password (only `/api/auth/update`, `/api/auth/info`, `/api/auth/logout` allowed).

**Role model**: `SUPER_ADMIN` > `SCHOOL_ADMIN` > `EVENT_ADMIN` / `ATHLETE` / `USER`. Defined in `UserRole` enum. SUPER_ADMIN inherits SCHOOL_ADMIN permissions. Custom annotations `@RequireRole` and `@RequireEventAdmin` are enforced by `PermissionAspect`.

**Layered architecture**:
- `controller/` — REST controllers (one per domain area)
- `service/` — interfaces, with `service/impl/` holding implementations
- `mapper/` — MyBatis-Plus mapper interfaces (XML in `resources/mapper/`)
- `entity/` — database entities (MyBatis-Plus with logic delete via `deleted` column)
- `dto/` — request bodies, `vo/` — response objects
- `common/` — enums, annotations (`RequireRole`, `RequireEventAdmin`), AOP aspects, exception handling, result wrappers (`Result`, `PageResult`, `ResultCode`)
- `config/` — Spring Boot config classes (Security, MyBatis-Plus, Redis, WebMVC, Swagger)
- `filter/` — `JwtAuthenticationFilter`
- `util/` — `JwtUtil`, `RedisUtil`, `ImageUtils`, `MailUtils`, `OssUtil`, etc.

**API groups**: `/api/auth`, `/api/system`, `/api/athlete`, `/api/event`, `/api/project`, `/api/registration`, `/api/score`, `/api/notification(s)`, `/api/admin/*`, `/api/public/*`. Swagger docs at `/doc.html`.

**ThreadLocal context**: `BaseContext.setCurrentId(userId)` is set in the JWT filter and MUST be cleared in its `finally` block. Service code can call `BaseContext.getCurrentId()`.

**MyBatis-Plus conventions**: Auto id generation, underscore-to-camelCase mapping, logic delete on `deleted` field (0=active, 1=deleted), enums scanned from `com.lz.common.enums`.

### Frontend

**State management**: Pinia stores in `src/stores/`. `useUserStore` manages token + userInfo in localStorage, with `normalizeUserInfo` handling inconsistent role field names.

**Router** (`src/router/index.js`): `beforeEach` guard checks system init status, auth requirement, first-login redirect, and role-based access. Routes with `meta.roles` are restricted to specified roles. Lazy-loaded views.

**API layer** (`src/api/`): One module per domain (auth, event, score, etc.). All use the shared Axios instance from `src/utils/request.js`, which auto-attaches token + `X-Client-Request-Id`, handles response unwrapping, and maps HTTP status codes to error messages/redirects via `src/config/errorStrategy.js`.

**UI**: Element Plus with auto-import (unplugin-vue-components + unplugin-auto-import). Dark theme with orange accents — see `.cursor/rules/frontend-dark-theme-tokens.mdc` for token and contrast requirements.

**Testing**: Vitest + @vue/test-utils, tests in `src/views/__tests__/`.

## Git Branch Strategy

- **`master`** — product code only (frontend + backend source, core docs). NO automation artifacts.
- **`git-ai/automation-route`** — AI iteration artifacts: `git-ai/**`, smoke scripts, run logs, snapshots.

Never merge `git-ai/automation-route` wholesale into `master`. Cherry-pick only product fixes.

## Commit Conventions

Format: `type(scope): subject` — e.g., `fix(backend): ...`, `test(backend): ...`, `feat: ...`.

Pre-commit gates:
- Backend changes: `mvn -Pnon-container-baseline test` must pass
- Frontend changes: `npm run lint` && `npm test` must pass
- Both changed: frontend first, then backend

Auto-fix failures and retry up to 2 times before marking as blocked.

## Test Baseline

The non-container test baseline (`mvn -Pnon-container-baseline test`) is the CI quality gate. New tests added to this baseline must not depend on Docker/Testcontainers — use mocks/stubs to isolate DB, cache, and external services. See `lz_sports_backend/src/test/README_NON_CONTAINER_BASELINE.md` for the current test inventory and extension rules.

CI workflows: `.github/workflows/ci.yml` (frontend lint+test + backend baseline) and `.github/workflows/backend-smoke-ci.yml` (backend baseline only).
