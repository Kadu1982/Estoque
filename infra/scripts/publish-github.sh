#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   GITHUB_TOKEN=... GITHUB_OWNER=Kadu1982 REPO_NAME=estoque bash infra/scripts/publish-github.sh
# Optional:
#   REPO_VISIBILITY=private   # private|public
#   REMOTE_NAME=origin

GITHUB_TOKEN="${GITHUB_TOKEN:-}"
GITHUB_OWNER="${GITHUB_OWNER:-}"
REPO_NAME="${REPO_NAME:-}"
REPO_VISIBILITY="${REPO_VISIBILITY:-private}"
REMOTE_NAME="${REMOTE_NAME:-origin}"

if [[ -z "$GITHUB_TOKEN" || -z "$GITHUB_OWNER" || -z "$REPO_NAME" ]]; then
  echo "GITHUB_TOKEN, GITHUB_OWNER and REPO_NAME are required"
  exit 1
fi

if [[ "$REPO_VISIBILITY" != "private" && "$REPO_VISIBILITY" != "public" ]]; then
  echo "REPO_VISIBILITY must be private or public"
  exit 1
fi

if ! command -v git >/dev/null 2>&1; then
  echo "git command not found"
  exit 1
fi

if ! command -v curl >/dev/null 2>&1; then
  echo "curl command not found"
  exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
  echo "jq command not found"
  exit 1
fi

if [[ ! -d .git ]]; then
  echo "Current directory is not a git repository"
  exit 1
fi

API_URL="https://api.github.com/user/repos"
IS_PRIVATE=true
if [[ "$REPO_VISIBILITY" == "public" ]]; then
  IS_PRIVATE=false
fi

echo "Creating GitHub repository ${GITHUB_OWNER}/${REPO_NAME} (${REPO_VISIBILITY})"
HTTP_CODE="$(
  curl -sS -o /tmp/github-create-repo.json -w "%{http_code}" \
    -X POST "$API_URL" \
    -H "Authorization: Bearer $GITHUB_TOKEN" \
    -H "Accept: application/vnd.github+json" \
    -d "{\"name\":\"$REPO_NAME\",\"private\":$IS_PRIVATE}"
)"

if [[ "$HTTP_CODE" != "201" && "$HTTP_CODE" != "422" ]]; then
  echo "GitHub API error (HTTP $HTTP_CODE):"
  cat /tmp/github-create-repo.json
  exit 1
fi

if [[ "$HTTP_CODE" == "422" ]]; then
  echo "Repository may already exist. Continuing..."
fi

REMOTE_URL="https://github.com/${GITHUB_OWNER}/${REPO_NAME}.git"
AUTH_REMOTE_URL="https://${GITHUB_TOKEN}@github.com/${GITHUB_OWNER}/${REPO_NAME}.git"

if git remote get-url "$REMOTE_NAME" >/dev/null 2>&1; then
  git remote set-url "$REMOTE_NAME" "$REMOTE_URL"
else
  git remote add "$REMOTE_NAME" "$REMOTE_URL"
fi

echo "Pushing branch main"
git push "$AUTH_REMOTE_URL" main:main

echo "Repository published: $REMOTE_URL"
