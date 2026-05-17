# Go/No-Go de Producao

## Criterios de GO

Todos os itens abaixo devem estar OK:

1. CI verde no `main`:
   - `Backend Tests`
   - `Frontend Build`
   - `Script Sanity`
2. Build local validado:
   - `backend`: `mvn test`
   - `frontend`: `npm run build`
3. Deploy staging validado:
   - `infra/scripts/deploy.sh` concluido sem rollback
   - `infra/scripts/smoke-test.sh` aprovado
4. Saude operacional:
   - `GET /actuator/health` = `UP`
   - logs sem erro critico no boot
5. Seguranca/configuracao:
   - `JWT_SECRET` >= 64 chars
   - `APP_SEED_ADMIN_PASSWORD` nao default
   - `SPRING_PROFILES_ACTIVE=prod`
6. Backup/restore:
   - backup executado com sucesso
   - restore ensaiado em ambiente nao produtivo

## Automatizacao recomendada

Executar:

```bash
bash infra/scripts/go-no-go.sh
```

Com health remoto (opcional):

```bash
DOMAIN=estoque.example.com bash infra/scripts/go-no-go.sh
```

## Criterios de NO-GO

Qualquer item abaixo implica bloqueio de producao:

- CI com falha.
- Smoke test com falha.
- Healthcheck degradado.
- Segredos ausentes/placeholder/default.
- Migracao de banco sem plano de rollback ensaiado.
