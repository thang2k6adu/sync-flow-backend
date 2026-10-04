#!/usr/bin/env bash
set -euo pipefail

TARGET_DIR="${1:-.}"
SERVICE_DIR="$(cd "$TARGET_DIR" && pwd)"
GITLEAKS_IMAGE="zricethezav/gitleaks:latest"

if ! docker info &>/dev/null; then
  echo "scan-secrets: Docker is not available or not running. Skipping secrets scan."
  exit 0
fi

TOPLEVEL="$(git -C "$SERVICE_DIR" rev-parse --show-toplevel 2>/dev/null || pwd)"
PREFIX="$(git -C "$SERVICE_DIR" rev-parse --show-prefix 2>/dev/null || true)"
PREFIX="${PREFIX%/}"

ARGS=(detect --source /repo --no-git=false --redact --verbose --exit-code 1)
if [[ -n "$PREFIX" ]]; then
  ARGS+=("--log-opts=-- ${PREFIX}")
fi

echo "==> Scanning secrets with Gitleaks..."
docker run --rm \
  -v "$TOPLEVEL":/repo \
  -w /repo \
  "$GITLEAKS_IMAGE" "${ARGS[@]}"
