# Publish to GitHub

Use this script when `gh` CLI is not available:

```bash
GITHUB_TOKEN=<token> \
GITHUB_OWNER=Kadu1982 \
REPO_NAME=estoque \
REPO_VISIBILITY=private \
bash infra/scripts/publish-github.sh
```

Notes:
- Token must have repository creation/push permissions.
- Script creates repo if needed and pushes `main`.
