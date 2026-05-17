# Austral Estoque

Sistema de controle de estoque com:
- `backend`: Spring Boot (Java 21)
- `frontend`: React + Vite
- `infra`: scripts de deploy, smoke test, backup/restore

## Requisitos

- Java 21
- Maven 3.9+
- Node.js 20+
- Docker + Docker Compose (para subir stack completa)

## 1) Rodar em modo desenvolvimento (local)

### Backend

```bash
cd backend
mvn spring-boot:run
```

API local: `http://localhost:8080`

### Frontend

Em outro terminal:

```bash
cd frontend
npm install
npm run dev
```

Frontend local: `http://localhost:5173`

## 2) Rodar stack com Docker Compose

```bash
docker compose up -d --build
```

Servicos principais:
- Frontend: `http://localhost`
- API: `http://localhost:8080`

Para derrubar:

```bash
docker compose down
```

## 3) Build e validacoes

### Backend
```bash
cd backend
mvn test
```

### Frontend
```bash
cd frontend
npm run build
```

### Validacao compose (producao)
```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.production.example config
```

## 4) Operacao (producao)

Scripts principais em `infra/scripts/`:
- `deploy.sh`
- `smoke-test.sh`
- `go-no-go.sh`
- `backup-db.sh`
- `restore-db.sh`
- `staging-rehearsal.sh`

Documentacao:
- `docs/production-checklist.md`
- `docs/go-no-go.md`
- `docs/db-rollback-and-restore.md`
