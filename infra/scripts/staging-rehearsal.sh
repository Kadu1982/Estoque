#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   DOMAIN=staging.example.com SMOKE_PASSWORD='***' bash infra/scripts/staging-rehearsal.sh
# Optional:
#   ENV_FILE=.env.staging
#   SKIP_BACKUP=true
#   SKIP_LOCAL_BUILDS=true

ENV_FILE="${ENV_FILE:-.env.staging}"
DOMAIN="${DOMAIN:-}"
SMOKE_PASSWORD="${SMOKE_PASSWORD:-}"
SKIP_BACKUP="${SKIP_BACKUP:-false}"
SKIP_LOCAL_BUILDS="${SKIP_LOCAL_BUILDS:-true}"

if [[ -z "$DOMAIN" ]]; then
  echo "DOMAIN is required"
  exit 1
fi

if [[ -z "$SMOKE_PASSWORD" ]]; then
  echo "SMOKE_PASSWORD is required"
  exit 1
fi

echo "[1/4] Pre-checks (go/no-go)"
ENV_FILE="$ENV_FILE" SKIP_LOCAL_BUILDS="$SKIP_LOCAL_BUILDS" bash infra/scripts/go-no-go.sh

if [[ "$SKIP_BACKUP" != "true" ]]; then
  echo "[2/4] Backup before deploy"
  ENV_FILE="$ENV_FILE" bash infra/scripts/backup-db.sh
else
  echo "[2/4] Backup skipped"
fi

echo "[3/4] Deploy"
ENV_FILE="$ENV_FILE" DOMAIN="$DOMAIN" SMOKE_PASSWORD="$SMOKE_PASSWORD" bash infra/scripts/deploy.sh

echo "[4/4] Post-deploy readiness with remote health"
ENV_FILE="$ENV_FILE" DOMAIN="$DOMAIN" SKIP_LOCAL_BUILDS="$SKIP_LOCAL_BUILDS" bash infra/scripts/go-no-go.sh

echo "Staging rehearsal completed"
