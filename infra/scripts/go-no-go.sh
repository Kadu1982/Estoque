#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   bash infra/scripts/go-no-go.sh
# Optional:
#   ENV_FILE=.env.production
#   DOMAIN=estoque.example.com
#   SKIP_LOCAL_BUILDS=true

ENV_FILE="${ENV_FILE:-.env.production}"
DOMAIN="${DOMAIN:-}"
SKIP_LOCAL_BUILDS="${SKIP_LOCAL_BUILDS:-false}"

pass() { echo "[OK] $1"; }
fail() { echo "[FAIL] $1"; exit 1; }

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || fail "Missing required command: $1"
}

require_env_file() {
  [[ -f "$ENV_FILE" ]] || fail "Env file not found: $ENV_FILE"
}

read_env_var() {
  local key="$1"
  grep -E "^${key}=" "$ENV_FILE" | tail -n 1 | cut -d'=' -f2- || true
}

check_secret() {
  local key="$1"
  local value
  value="$(read_env_var "$key")"
  [[ -n "${value// }" ]] || fail "Missing env var in $ENV_FILE: $key"
  [[ "$value" != CHANGE_ME* ]] || fail "Placeholder value is not allowed for $key"
  pass "$key is set"
}

require_cmd bash
require_cmd docker
require_cmd curl
require_env_file

check_secret POSTGRES_PASSWORD
check_secret REDIS_PASSWORD
check_secret JWT_SECRET
check_secret APP_SEED_ADMIN_PASSWORD

JWT_SECRET_VALUE="$(read_env_var JWT_SECRET)"
[[ ${#JWT_SECRET_VALUE} -ge 64 ]] || fail "JWT_SECRET must have at least 64 characters"
pass "JWT_SECRET length >= 64"

ADMIN_PASSWORD_VALUE="$(read_env_var APP_SEED_ADMIN_PASSWORD)"
[[ "$ADMIN_PASSWORD_VALUE" != "Admin@123456" ]] || fail "APP_SEED_ADMIN_PASSWORD default value is not allowed"
pass "APP_SEED_ADMIN_PASSWORD is not default"

SPRING_PROFILE="$(read_env_var SPRING_PROFILES_ACTIVE)"
[[ "$SPRING_PROFILE" == "prod" ]] || fail "SPRING_PROFILES_ACTIVE must be prod in $ENV_FILE"
pass "SPRING_PROFILES_ACTIVE=prod"

echo "Validating docker compose configuration"
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file "$ENV_FILE" config >/dev/null
pass "docker compose config"

if [[ "$SKIP_LOCAL_BUILDS" != "true" ]]; then
  echo "Running backend tests"
  (cd backend && mvn test -q)
  pass "backend tests"

  echo "Running frontend build"
  (cd frontend && npm run build >/dev/null)
  pass "frontend build"
fi

if [[ -n "$DOMAIN" ]]; then
  require_cmd jq
  HEALTH_URL="https://${DOMAIN}/actuator/health"
  echo "Checking health: $HEALTH_URL"
  STATUS="$(curl -kfsS "$HEALTH_URL" | jq -r '.status' || true)"
  [[ "$STATUS" == "UP" ]] || fail "Healthcheck is not UP"
  pass "healthcheck UP"
fi

echo "GO/NO-GO checks passed"
