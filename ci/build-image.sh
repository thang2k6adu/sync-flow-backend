#!/usr/bin/env bash
set -euo pipefail

# Nhận đường dẫn service directory và tên image
TARGET_DIR="${1:-.}"
SERVICE_DIR="$(cd "$TARGET_DIR" && pwd)"
SERVICE_NAME="$(basename "$SERVICE_DIR")"
IMAGE_NAME="${2:-$SERVICE_NAME}"

IMAGE_TAG="${IMAGE_TAG:-git-$(git -C "$SERVICE_DIR" rev-parse --short HEAD 2>/dev/null || date +%s)}"

if [[ -n "${IMAGE_REGISTRY:-}" ]]; then
  IMAGE_REF="${IMAGE_REGISTRY%/}/${IMAGE_NAME}:${IMAGE_TAG}"
else
  IMAGE_REF="${IMAGE_NAME}:${IMAGE_TAG}"
fi

GIT_COMMIT="$(git -C "$SERVICE_DIR" rev-parse --short HEAD 2>/dev/null || echo unknown)"

echo "==> Building Docker image for $SERVICE_NAME..."
echo "    Image ref : $IMAGE_REF"
echo "    Git commit: $GIT_COMMIT"

docker build --build-arg GIT_COMMIT="$GIT_COMMIT" -t "$IMAGE_REF" "$SERVICE_DIR"
echo "built: $IMAGE_REF"
