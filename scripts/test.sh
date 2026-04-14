#!/bin/bash
set -e

cd "$(dirname "$0")/.."

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
