#!/bin/bash
set -e

cd "$(dirname "$0")/.."

VERSION=${1:-latest}
export IMAGE_VERSION=$VERSION

echo "========================================================"
echo "  [Build] Building project version: $VERSION"
echo "========================================================"

echo "1. Compiling backend..."
cd lz_sports_backend
mvn clean package -DskipTests
cd ..

echo "2. Building Docker images..."
docker compose build

echo "Build complete for version $VERSION."
