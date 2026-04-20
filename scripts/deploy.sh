#!/bin/bash
set -e

cd "$(dirname "$0")/.."

ENV="dev"
VERSION="latest"
USE_LOCAL_DB=false

# 解析参数
for arg in "$@"; do
    case $arg in
        prod)
            ENV="prod"
            shift
            ;;
        dev)
            ENV="dev"
            shift
            ;;
        --local-db)
            USE_LOCAL_DB=true
            shift
            ;;
        *)
            # 如果不是标志，则认为是版本号
            VERSION=$arg
            shift
            ;;
    esac
done

export IMAGE_VERSION=$VERSION

echo "========================================================"
echo "Deploying environment: $ENV"
echo "Version: $VERSION"
if [ "$USE_LOCAL_DB" = true ]; then
    echo "Using LOCAL Database and Redis (host.docker.internal)"
else
    echo "Using CONTAINERIZED Database and Redis"
fi
echo "========================================================"

COMPOSE_FILES="-f docker-compose.yml"
if [ "$ENV" = "prod" ]; then
    COMPOSE_FILES="$COMPOSE_FILES -f docker-compose.prod.yml"
    ENV_FILE="config/.env.prod"
else
    COMPOSE_FILES="$COMPOSE_FILES -f docker-compose.dev.yml"
    ENV_FILE="config/.env.dev"
fi

if [ "$USE_LOCAL_DB" = true ]; then
    COMPOSE_FILES="$COMPOSE_FILES -f docker-compose.local-db.yml"
fi

echo "Running: docker compose --env-file $ENV_FILE $COMPOSE_FILES up -d"
docker compose --env-file $ENV_FILE $COMPOSE_FILES up -d

echo "Deployment complete."
