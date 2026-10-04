#!/usr/bin/env bash
set -euo pipefail

TARGET_DIR="${1:-.}"
SERVICE_DIR="$(cd "$TARGET_DIR" && pwd)"

cd "$SERVICE_DIR"
# Kiểm tra nếu service có task spotlessCheck trong gradle
if ./gradlew tasks --all | grep -q "spotlessCheck"; then
  echo "==> Running spotlessCheck in $SERVICE_DIR..."
  ./gradlew spotlessCheck --no-daemon
else
  echo "==> No linting task (spotlessCheck) defined for this service. Skipping."
fi
