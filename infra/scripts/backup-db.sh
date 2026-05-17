#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   bash infra/scripts/backup-db.sh
# Optional:
#   ENV_FILE=/opt/austral-estoque/.env
#   DB_CONTAINER=austral-db
#   BACKUP_DIR=/opt/backups
#   RETENTION_DAYS=7

ENV_FILE="${ENV_FILE:-/opt/austral-estoque/.env}"
DB_CONTAINER="${DB_CONTAINER:-austral-db}"
BACKUP_DIR="${BACKUP_DIR:-/opt/backups}"
RETENTION_DAYS="${RETENTION_DAYS:-7}"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Env file not found: $ENV_FILE"
  exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "docker command not found"
  exit 1
fi

if ! [[ "$RETENTION_DAYS" =~ ^[0-9]+$ ]]; then
  echo "RETENTION_DAYS must be a non-negative integer"
  exit 1
fi

# shellcheck disable=SC1090
source "$ENV_FILE"

if [[ -z "${POSTGRES_USER:-}" || -z "${POSTGRES_DB:-}" ]]; then
  echo "POSTGRES_USER and POSTGRES_DB must be defined in $ENV_FILE"
  exit 1
fi

if ! docker ps --format '{{.Names}}' | grep -qx "$DB_CONTAINER"; then
  echo "Database container is not running: $DB_CONTAINER"
  exit 1
fi

TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
BACKUP_FILE="${BACKUP_DIR}/austral_estoque_${TIMESTAMP}.sql.gz"

mkdir -p "$BACKUP_DIR"

echo "Starting backup: $BACKUP_FILE"
docker exec "$DB_CONTAINER" pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB" | gzip > "$BACKUP_FILE"

if [[ ! -s "$BACKUP_FILE" ]]; then
  echo "Backup file was created but is empty: $BACKUP_FILE"
  exit 1
fi

find "$BACKUP_DIR" -name "*.sql.gz" -mtime +"$RETENTION_DAYS" -delete

echo "Backup completed: $BACKUP_FILE"
