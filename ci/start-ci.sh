#!/usr/bin/env bash
# Local CI Pipeline for Sync Flow Backend Microservices
# Usage:
#   ./ci/start-ci.sh [auth|task|vocab|gateway|all]
#
# Environment variables:
#   IMAGE_REGISTRY : Registry URL (e.g. ghcr.io/thang2k6adu/sync-flow-backend)
#   IMAGE_TAG      : Tag for Docker image (default: git short commit or timestamp)
#   REGISTRY_USER  : Username for registry login
#   REGISTRY_TOKEN : Access token / password for registry login

set -uo pipefail

DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$(cd "$DIR/.." && pwd)"

TARGET="${1:-all}"

SERVICES=()
case "$TARGET" in
  auth|kruzetech-auth)
    SERVICES=("kruzetech-auth")
    ;;
  task|kruzetech-task)
    SERVICES=("kruzetech-task")
    ;;
  vocab|kruzetech-vocab)
    SERVICES=("kruzetech-vocab")
    ;;
  gateway|kruzetech-gateway)
    SERVICES=("kruzetech-gateway")
    ;;
  all)
    SERVICES=("kruzetech-auth" "kruzetech-task" "kruzetech-vocab" "kruzetech-gateway")
    ;;
  *)
    echo "Unknown service: $TARGET"
    echo "Usage: $0 [auth|task|vocab|gateway|all]"
    exit 1
    ;;
esac

GLOBAL_FAIL=0

for SVC in "${SERVICES[@]}"; do
  SVC_DIR="$BACKEND_DIR/services/$SVC"
  if [[ ! -d "$SVC_DIR" ]]; then
    echo "Directory not found: $SVC_DIR"
    exit 1
  fi

  echo
  echo "============================================================"
  echo "         RUNNING CI PIPELINE FOR: $SVC"
  echo "============================================================"

  PASS=(); FAIL=(); SKIP=(); WARN=()
  step() { local l="$1"; shift; echo; echo "==> [$SVC] $l"; if "$@"; then PASS+=("$l"); else FAIL+=("$l"); fi; }
  skip() { echo; echo "==> [$SVC] $1 — SKIPPED ($2)"; SKIP+=("$1 ($2)"); }
  soft_step() { local l="$1"; shift; echo; echo "==> [$SVC] $l (non-blocking)"; if "$@"; then PASS+=("$l"); else WARN+=("$l"); fi; }

  # 1. Build & Test
  step "build-test" bash "$DIR/build-test.sh" "$SVC_DIR"

  # 2. Linting (soft step)
  if [[ -f "$DIR/lint.sh" ]]; then
    soft_step "lint" bash "$DIR/lint.sh" "$SVC_DIR"
  fi

  # 3. Scan dependencies CVE (soft step)
  if [[ -f "$DIR/scan-deps.sh" ]]; then
    soft_step "scan-deps" bash "$DIR/scan-deps.sh" "$SVC_DIR"
  fi

  # 4. Scan secrets in history (soft step)
  if [[ -f "$DIR/scan-secrets.sh" ]]; then
    soft_step "scan-secrets" bash "$DIR/scan-secrets.sh" "$SVC_DIR"
  fi

  # 5. Build Docker Image (Luôn build để đảm bảo Dockerfile hợp lệ)
  step "build-image" bash "$DIR/build-image.sh" "$SVC_DIR" "$SVC"

  # 6. Publish Docker Image
  if [[ -n "${IMAGE_REGISTRY:-}" ]]; then
    step "publish-image" bash "$DIR/publish-image.sh" "$SVC_DIR" "$SVC"
  else
    skip "publish-image" "IMAGE_REGISTRY chưa đặt (đặt IMAGE_REGISTRY=ghcr.io/test để kiểm thử bước push gate)"
  fi

  echo
  echo "---------------- SUMMARY FOR $SVC ----------------"
  echo "PASS (${#PASS[@]})"
  ((${#PASS[@]})) && printf '  \xe2\x9c\x93 %s\n' "${PASS[@]}"
  echo "SKIP (${#SKIP[@]})"
  ((${#SKIP[@]})) && printf '  - %s\n' "${SKIP[@]}"
  echo "WARN (${#WARN[@]})"
  ((${#WARN[@]})) && printf '  ! %s\n' "${WARN[@]}"
  echo "FAIL (${#FAIL[@]})"
  ((${#FAIL[@]})) && printf '  \xe2\x9c\x97 %s\n' "${FAIL[@]}"

  if [[ ${#FAIL[@]} -gt 0 ]]; then
    GLOBAL_FAIL=1
  fi
done

echo
echo "============================================================"
if [[ $GLOBAL_FAIL -eq 0 ]]; then
  echo " [SUCCESS] All selected services passed CI pipeline!"
  exit 0
else
  echo " [FAILURE] One or more steps failed during CI execution."
  exit 1
fi
