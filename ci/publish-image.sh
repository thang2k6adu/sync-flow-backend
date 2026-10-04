#!/usr/bin/env bash
set -euo pipefail

TARGET_DIR="${1:-.}"
SERVICE_DIR="$(cd "$TARGET_DIR" && pwd)"
SERVICE_NAME="$(basename "$SERVICE_DIR")"
IMAGE_NAME="${2:-$SERVICE_NAME}"

: "${IMAGE_REGISTRY:?IMAGE_REGISTRY not set}"
: "${REGISTRY_USER:?REGISTRY_USER not set}"
: "${REGISTRY_TOKEN:?REGISTRY_TOKEN not set}"

IMAGE_TAG="${IMAGE_TAG:-git-$(git -C "$SERVICE_DIR" rev-parse --short HEAD 2>/dev/null || date +%s)}"
IMAGE_REF="${IMAGE_REGISTRY%/}/${IMAGE_NAME}:${IMAGE_TAG}"
REGISTRY_HOST="${IMAGE_REGISTRY%%/*}"

echo "==> Publishing Docker image for $SERVICE_NAME..."
echo "    Target: $IMAGE_REF"

echo "$REGISTRY_TOKEN" | docker login "$REGISTRY_HOST" -u "$REGISTRY_USER" --password-stdin
trap 'docker logout "$REGISTRY_HOST" >/dev/null 2>&1 || true' EXIT

docker push "$IMAGE_REF"
echo "pushed: $IMAGE_REF"
