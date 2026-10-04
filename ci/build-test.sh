#!/usr/bin/env bash
set -euo pipefail

# Nhận đường dẫn service directory từ đối số đầu tiên
TARGET_DIR="${1:-.}"
SERVICE_DIR="$(cd "$TARGET_DIR" && pwd)"

echo "==> Running tests in $SERVICE_DIR"
cd "$SERVICE_DIR"
./gradlew clean test --no-daemon
