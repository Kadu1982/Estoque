#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   DOMAIN=estoque.example.com ./infra/scripts/deploy.sh
# Optional:
#   APP_DIR=/opt/austral-estoque
#   ENV_FILE=.env.production
#   WAIT_SECONDS=120
#   SKIP_SMOKE=true
#   SSL_CERT_PATH=infra/ssl/cert.pem
#   SSL_KEY_PATH=infra/ssl/key.pem

APP_DIR="${APP_DIR:-/opt/austral-estoque}"
ENV_FILE="${ENV_FILE:-.env.production}"
WAIT_SECONDS="${WAIT_SECONDS:-120}"
DOMAIN="${DOMAIN:-}"
SKIP_SMOKE="${SKIP_SMOKE:-false}"
SSL_CERT_PATH="${SSL_CERT_PATH:-infra/ssl/cert.pem}"
SSL_KEY_PATH="${SSL_KEY_PATH:-infra/ssl/key.pem}"

if [[ -z "$DOMAIN" ]]; then
  echo "DOMAIN is required. Example: DOMAIN=estoque.example.com ./infra/scripts/deploy.sh"
  exit 1
fi

cd "$APP_DIR"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Missing env file: $APP_DIR/$ENV_FILE"
  exit 1
fi

if [[ ! -s "$SSL_CERT_PATH" ]]; then
  echo "Missing TLS certificate file: $APP_DIR/$SSL_CERT_PATH"
  exit 1
fi

if [[ ! -s "$SSL_KEY_PATH" ]]; then
  echo "Missing TLS key file: $APP_DIR/$SSL_KEY_PATH"
  exit 1
fi

require_env() {
  local key="$1"
  local value
  value="$(grep -E "^${key}=" "$ENV_FILE" | tail -n 1 | cut -d'=' -f2- || true)"
  if [[ -z "${value// }" ]]; then
    echo "Missing required env var in $ENV_FILE: $key"
    exit 1
  fi
  if [[ "$value" == CHANGE_ME* ]]; then
    echo "Invalid placeholder value for $key in $ENV_FILE"
    exit 1
  fi
}

require_env POSTGRES_PASSWORD
require_env REDIS_PASSWORD
require_env JWT_SECRET
require_env APP_SEED_ADMIN_PASSWORD

JWT_SECRET_VALUE="$(grep -E "^JWT_SECRET=" "$ENV_FILE" | tail -n 1 | cut -d'=' -f2- || true)"
if [[ ${#JWT_SECRET_VALUE} -lt 64 ]]; then
  echo "Invalid JWT_SECRET in $ENV_FILE: must have at least 64 characters"
  exit 1
fi

ADMIN_PASSWORD_VALUE="$(grep -E "^APP_SEED_ADMIN_PASSWORD=" "$ENV_FILE" | tail -n 1 | cut -d'=' -f2- || true)"
if [[ "$ADMIN_PASSWORD_VALUE" == "Admin@123456" ]]; then
  echo "Invalid APP_SEED_ADMIN_PASSWORD in $ENV_FILE: default password is not allowed"
  exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "docker command not found"
  exit 1
fi

if ! command -v curl >/dev/null 2>&1; then
  echo "curl command not found"
  exit 1
fi

echo "Validating docker compose configuration"
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file "$ENV_FILE" config >/dev/null

PREV_REV="$(git rev-parse HEAD)"
echo "Previous revision: $PREV_REV"

rollback() {
  echo "Deploy failed. Rolling back to $PREV_REV"
  git reset --hard "$PREV_REV"
  docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file "$ENV_FILE" up -d --build
  echo "Rollback finished"
}

trap rollback ERR

echo "Pulling latest code"
git fetch origin
git pull --ff-only origin main

echo "Building and starting production stack"
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file "$ENV_FILE" up -d --build

echo "Waiting for API health (max ${WAIT_SECONDS}s)"
END_TIME=$((SECONDS + WAIT_SECONDS))
until curl -kfsS "https://${DOMAIN}/actuator/health" >/dev/null 2>&1; do
  if (( SECONDS >= END_TIME )); then
    echo "Healthcheck timeout"
    exit 1
  fi
  sleep 5
done

if [[ "$SKIP_SMOKE" != "true" ]]; then
  echo "Running smoke test"
  SMOKE_LOGIN="${SMOKE_LOGIN:-admin}"
  SMOKE_PASSWORD="${SMOKE_PASSWORD:-}"
  if [[ -z "$SMOKE_PASSWORD" ]]; then
    echo "SMOKE_PASSWORD is required when SKIP_SMOKE is not true"
    exit 1
  fi
  BASE_URL="https://${DOMAIN}" SMOKE_LOGIN="$SMOKE_LOGIN" SMOKE_PASSWORD="$SMOKE_PASSWORD" \
    bash infra/scripts/smoke-test.sh
fi

trap - ERR
echo "Deploy completed successfully"
