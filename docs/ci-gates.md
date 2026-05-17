# CI Gates

Este repositorio possui pipeline em `.github/workflows/ci.yml` com 3 gates obrigatorios:

1. **Backend Tests**
   - Executa `mvn test` em `backend/`.
   - Inclui testes de integracao de fluxo operacional e testes do `ProductionGuard`.

2. **Frontend Build**
   - Executa `npm ci` e `npm run build` em `frontend/`.
   - Garante integridade de dependencias e build de producao.

3. **Script Sanity**
   - Valida sintaxe de scripts:
     - `infra/scripts/deploy.sh`
     - `infra/scripts/smoke-test.sh`
     - `infra/scripts/backup-db.sh`
     - `infra/scripts/restore-db.sh`
     - `infra/scripts/go-no-go.sh`
   - Valida composicao Docker:
     - `docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.production.example config`

## Regra de promocao para Producao

So promover para producao quando:
- Todos os jobs da CI estiverem verdes no `main`.
- Smoke pos-deploy tiver passado (`infra/scripts/smoke-test.sh`).
- `docs/production-checklist.md` estiver 100% cumprido.
- `infra/scripts/go-no-go.sh` tiver passado com o arquivo de ambiente de producao.
