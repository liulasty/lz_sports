#!/bin/bash
set -e

cd "$(dirname "$0")/.."

CUR_BRANCH=$(git branch --show-current 2>/dev/null || true)
if [ "$CUR_BRANCH" != "git-ai/automation-route" ]; then
  echo "[ERROR] This test entrypoint is restricted to branch git-ai/automation-route."
  echo "        Current branch: ${CUR_BRANCH:-unknown}"
  echo "        Please switch (or use the automation worktree) before iterative dev/test."
  exit 1
fi

if [ "${1:-}" = "smoke" ]; then
  BACKEND_URL=${2:-http://localhost:8080}
  ACCESS_TOKEN=${3:-}
  if [ -n "$ACCESS_TOKEN" ]; then
    powershell -ExecutionPolicy Bypass -File "scripts/smoke-linkup.ps1" -BackendUrl "$BACKEND_URL" -AccessToken "$ACCESS_TOKEN"
  else
    powershell -ExecutionPolicy Bypass -File "scripts/smoke-linkup.ps1" -BackendUrl "$BACKEND_URL"
  fi
  exit $?
fi

if [ "${1:-}" = "notify-smoke" ]; then
  BACKEND_URL=${2:-http://localhost:8080}
  ACCESS_TOKEN=${3:-}
  if [ -n "$ACCESS_TOKEN" ]; then
    powershell -ExecutionPolicy Bypass -File "scripts/smoke-notification.ps1" -BackendUrl "$BACKEND_URL" -AccessToken "$ACCESS_TOKEN"
  else
    powershell -ExecutionPolicy Bypass -File "scripts/smoke-notification.ps1" -BackendUrl "$BACKEND_URL"
  fi
  exit $?
fi

if [ "${1:-}" = "full-smoke" ]; then
  BACKEND_URL=${2:-http://localhost:8080}
  ACCESS_TOKEN=${3:-}
  if [ -n "$ACCESS_TOKEN" ]; then
    powershell -ExecutionPolicy Bypass -File "scripts/smoke-full.ps1" -BackendUrl "$BACKEND_URL" -AccessToken "$ACCESS_TOKEN"
  else
    powershell -ExecutionPolicy Bypass -File "scripts/smoke-full.ps1" -BackendUrl "$BACKEND_URL"
  fi
  exit $?
fi

if [ "${1:-}" = "oneclick" ]; then
  BACKEND_URL=${2:-http://localhost:8080}
  FRONTEND_URL=${3:-http://localhost:5173}
  EVENT_ID=${4:-1}
  powershell -ExecutionPolicy Bypass -File "scripts/smoke-oneclick.ps1" -BackendUrl "$BACKEND_URL" -FrontendUrl "$FRONTEND_URL" -EventId "$EVENT_ID"
  exit $?
fi

if [ "${1:-}" = "dev-start" ]; then
  powershell -ExecutionPolicy Bypass -File "scripts/dev-start.ps1"
  exit $?
fi

if [ "${1:-}" = "dev-stop" ]; then
  powershell -ExecutionPolicy Bypass -File "scripts/dev-stop.ps1"
  exit $?
fi

ENV=${1:-dev}

if [ "$ENV" = "prod" ]; then
  PORT=80
else
  PORT=5173
fi

echo "Waiting for services to start..."
sleep 5

echo "Testing Frontend on port $PORT..."
if curl -sSf -m 5 http://localhost:$PORT > /dev/null; then
  echo "[OK] Frontend is up."
else
  echo "[ERROR] Frontend is not reachable!"
  exit 1
fi

echo "Testing Backend through proxy..."
if curl -sSf -m 5 http://localhost:$PORT/api/system/init-status > /dev/null; then
  echo "[OK] Backend is healthy."
else
  echo "[ERROR] Backend is not reachable or healthy!"
  exit 1
fi

echo "All tests passed."
