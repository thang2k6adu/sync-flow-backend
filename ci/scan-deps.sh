#!/usr/bin/env bash
set -euo pipefail

TARGET_DIR="${1:-.}"
SERVICE_DIR="$(cd "$TARGET_DIR" && pwd)"

if ! command -v trivy &>/dev/null; then
  echo "scan-deps: trivy CLI not found on host. Skipping vulnerability scan."
  exit 0
fi

cd "$SERVICE_DIR"
./gradlew bootJar -x test --no-daemon
JAR_PATH="$(find build/libs -maxdepth 1 -name '*.jar' | head -1)"
if [[ -z "$JAR_PATH" ]]; then
  echo "scan-deps: no jar found under build/libs, bootJar did not produce an artifact" >&2
  exit 1
fi

echo "==> Scanning dependencies with Trivy for $JAR_PATH..."
trivy rootfs --scanners vuln --exit-code 1 --severity CRITICAL,HIGH "$JAR_PATH"
