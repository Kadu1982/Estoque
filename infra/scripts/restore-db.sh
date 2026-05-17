#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   BACKUP_FILE=/opt/backups/austral_estoque_YYYYMMDD_HHMMSS.sql.gz bash infra/scripts/restore-db.sh
# Optional:
#   ENV_FILE=/opt/austral-estoque/.env
#   DB_CONTAINER=austral-db
#   FORCE_RESTORE=true

ENV_FILE="${ENV_FILE:-/opt/austral-estoque/.env}"
DB_CONTAINER="${DB_CONTAINER:-austral-db}"
BACKUP_FILE="${BACKUP_FILE:-}"
FORCE_RESTORE="${FORCE_RESTORE:-false}"

if [[ -z "$BACKUP_FILE" ]]; then
  echo "BACKUP_FILE is required"
  exit 1
fi

if [[ ! -f "$BACKUP_FILE" ]]; then
  echo "Backup file not found: $BACKUP_FILE"
  exit 1
fi

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Env file not found: $ENV_FILE"
  exit 1
fi

if [[ "$FORCE_RESTORE" != "true" ]]; then
  echo "Refusing to restore without FORCE_RESTORE=true"
  exit 1
fi

# shellcheck disable=SC1090
source "$ENV_FILE"

if [[ -z "${POSTGRES_USER:-}" || -z "${POSTGRES_DB:-}" ]]; then
  echo "POSTGRES_USER and POSTGRES_DB must be defined in $ENV_FILE"
  exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "docker command not found"
  exit 1
fi

if ! docker ps --format '{{.Names}}' | grep -qx "$DB_CONTAINER"; then
  echo "Database container is not running: $DB_CONTAINER"
  exit 1
fi

if ! gzip -t "$BACKUP_FILE" >/dev/null 2>&1; then
  echo "Backup file is not a valid gzip archive: $BACKUP_FILE"
  exit 1
fi

echo "Restoring backup into container '$DB_CONTAINER' database '$POSTGRES_DB'..."
gunzip -c "$BACKUP_FILE" | docker exec -i "$DB_CONTAINER" psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"
echo "Restore completed successfully."
