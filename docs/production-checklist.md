# Production Checklist

## 1. Pre-deploy
- Confirm backend runs with `SPRING_PROFILES_ACTIVE=prod`.
- Confirm `.env.production` defines `APP_SEED_ADMIN_PASSWORD` (no fallback in compose).
- Confirm `spring.jpa.open-in-view=false` in active profile.
- Fill `.env.production` from `.env.production.example` with strong secrets.
- Confirm `JWT_SECRET` has at least 64 random characters.
- Confirm admin seed password is not default.
- Place TLS files in `infra/ssl/cert.pem` and `infra/ssl/key.pem`.
- Confirm DNS points to the VPS IP.
- Run local readiness check:
```bash
ENV_FILE=.env.production bash infra/scripts/go-no-go.sh
```

## 2. Deploy
- Confirm CI pipeline (`.github/workflows/ci.yml`) green on `main` before deploy.
- Recommended staging rehearsal:
```bash
DOMAIN=<staging-domain> SMOKE_PASSWORD='<admin-password>' ENV_FILE=.env.staging bash infra/scripts/staging-rehearsal.sh
```
- Note: `staging-rehearsal.sh` defaults `SKIP_LOCAL_BUILDS=true` for server environments.
- Run (recommended):
```bash
DOMAIN=<your-domain> SMOKE_PASSWORD='<admin-password>' bash infra/scripts/deploy.sh
```
- Manual fallback:
```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.production up -d --build
```
- Verify containers are healthy:
```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml ps
```
- Verify API health:
```bash
curl -k https://<your-domain>/actuator/health
```

## 3. Smoke test
- Login with seeded admin.
- Create supplier and item.
- Create requisition and approve/reject.
- Create order and register receive.
- Verify stock and dashboard update.
- Optional automated smoke:
```bash
BASE_URL=https://<your-domain> SMOKE_LOGIN=admin SMOKE_PASSWORD='<admin-password>' bash infra/scripts/smoke-test.sh
```
- Reference CI gates: `docs/ci-gates.md`.
- Re-run readiness with remote health:
```bash
ENV_FILE=.env.production DOMAIN=<your-domain> bash infra/scripts/go-no-go.sh
```

## 4. Backup and restore
- Run backup script once and check output file:
```bash
bash infra/scripts/backup-db.sh
```
- Keep backup retention policy active (7 days minimum).
- Execute one restore rehearsal in non-production.
- Follow procedure: `docs/db-rollback-and-restore.md`.

## 5. Rollback
- Keep previous image tags available.
- Automatic rollback is executed by `infra/scripts/deploy.sh` if healthcheck or smoke test fails.
- Manual rollback command:
```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.production up -d
```
- If schema changed, rollback only with tested migration rollback strategy.
- Final release decision must follow: `docs/go-no-go.md`.
