# 🖥️ Plano de Frontend — Austral Estoque

> **Stack:** React 18 + Vite + TypeScript
> **Backend:** Spring Boot 3.3.5 rodando em `http://localhost:8080`
> **Localização do código:** `frontend/` (raiz do monorepo)
> **Executor:** LLM (este documento descreve o que e como escrever — o LLM escreve o código)

---

## 1. Stack & Dependências

### `package.json` — dependências exatas a instalar

```json
{
  "dependencies": {
    "react": "^18.3.1",
    "react-dom": "^18.3.1",
    "react-router-dom": "^6.23.1",
    "@tanstack/react-query": "^5.45.0",
    "axios": "^1.7.2",
    "zustand": "^4.5.4",
    "react-hook-form": "^7.52.0",
    "zod": "^3.23.8",
    "@hookform/resolvers": "^3.6.0",
    "react-hot-toast": "^2.4.1",
    "lucide-react": "^0.395.0",
    "date-fns": "^3.6.0",
    "recharts": "^2.12.7"
  },
  "devDependencies": {
    "@vitejs/plugin-react": "^4.3.1",
    "typescript": "^5.4.5",
    "vite": "^5.3.1",
    "@types/react": "^18.3.3",
    "@types/react-dom": "^18.3.0"
  }
}
```

**Não usar:** Tailwind, MUI, Ant Design, Bootstrap. Usar **CSS puro com variáveis CSS** (design system próprio).

---

## 2. Estrutura de Pastas

```
frontend/
├── Dockerfile
├── nginx.conf
├── index.html
├── vite.config.ts
├── tsconfig.json
├── .env.example
└── src/
    ├── main.tsx                  ← entry point
    ├── App.tsx                   ← router root
    ├── index.css                 ← design system global (variáveis, reset, tipografia)
    │
    ├── api/                      ← camada de comunicação com o backend
    │   ├── client.ts             ← instância axios configurada
    │   ├── auth.api.ts
    │   ├── items.api.ts
    │   ├── orders.api.ts
    │   ├── requisitions.api.ts
    │   ├── suppliers.api.ts
    │   └── stock.api.ts
    │
    ├── types/                    ← TypeScript types espelhando os DTOs do backend
    │   ├── auth.types.ts
    │   ├── item.types.ts
    │   ├── order.types.ts
    │   ├── requisition.types.ts
    │   ├── supplier.types.ts
    │   └── stock.types.ts
    │
    ├── store/                    ← Zustand stores
    │   └── auth.store.ts
    │
    ├── hooks/                    ← Custom hooks (wrappers de React Query)
    │   ├── useAuth.ts
    │   ├── useItems.ts
    │   ├── useOrders.ts
    │   ├── useRequisitions.ts
    │   └── useSuppliers.ts
    │
    ├── components/               ← Componentes reutilizáveis (UI puro)
    │   ├── ui/
    │   │   ├── Button.tsx
    │   │   ├── Input.tsx
    │   │   ├── Select.tsx
    │   │   ├── Modal.tsx
    │   │   ├── Table.tsx
    │   │   ├── Badge.tsx
    │   │   ├── Card.tsx
    │   │   ├── Spinner.tsx
    │   │   └── EmptyState.tsx
    │   └── layout/
    │       ├── Sidebar.tsx
    │       ├── Header.tsx
    │       ├── AppLayout.tsx     ← layout com sidebar + header
    │       └── AuthLayout.tsx    ← layout centralizado para login
    │
    └── pages/                   ← Páginas (uma por rota)
        ├── LoginPage.tsx
        ├── DashboardPage.tsx
        ├── items/
        │   ├── ItemsPage.tsx
        │   └── ItemFormPage.tsx
        ├── orders/
        │   ├── OrdersPage.tsx
        │   └── OrderFormPage.tsx
        ├── requisitions/
        │   ├── RequisitionsPage.tsx
        │   └── RequisitionFormPage.tsx
        ├── suppliers/
        │   ├── SuppliersPage.tsx
        │   └── SupplierFormPage.tsx
        └── stock/
            └── StockPage.tsx
```

---

## 3. Design System — `src/index.css`

O LLM deve criar um CSS com variáveis globais seguindo este design:

- **Paleta:** dark mode como padrão. Fundo `#0f1117`, superfície `#1a1d27`, borda `#2a2d3e`
- **Cor primária:** azul-violeta `hsl(240, 70%, 60%)` → `#4f6ef7`
- **Cor de sucesso:** `#22c55e`, alerta: `#f59e0b`, erro: `#ef4444`
- **Fonte:** `Inter` via Google Fonts
- **Border-radius padrão:** `8px`
- **Sombra padrão:** `0 4px 24px rgba(0,0,0,0.4)`

```css
/* Variáveis CSS que o LLM deve declarar em :root */
--color-bg:            #0f1117;
--color-surface:       #1a1d27;
--color-surface-2:     #21253a;
--color-border:        #2a2d3e;
--color-primary:       #4f6ef7;
--color-primary-hover: #3d5ce8;
--color-text:          #e2e8f0;
--color-text-muted:    #64748b;
--color-success:       #22c55e;
--color-warning:       #f59e0b;
--color-danger:        #ef4444;
--radius:              8px;
--sidebar-width:       240px;
--font:                'Inter', sans-serif;
```

---

## 4. Contratos de API (o que o frontend consome)

### 4.1 Autenticação — `POST /api/v1/auth/login`

**Request:**
```json
{ "login": "string", "password": "string" }
```

**Response:**
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "tokenType": "Bearer",
  "userId": "uuid",
  "username": "string",
  "fullName": "string",
  "email": "string",
  "roles": ["ADMIN"]
}
```

**Refresh — `POST /api/v1/auth/refresh`**
```json
{ "refreshToken": "string" }
```

### 4.2 Items — `GET /api/v1/items`

**Query params:** `search`, `categoryId`, `active`, `page`, `size`, `sort`

**Response:** `Page<Item>` com campos:
```json
{
  "content": [{
    "id": "uuid",
    "code": "string",
    "description": "string",
    "brand": "string",
    "unitOfMeasure": "string",
    "criticality": "CRITICO | ALTO | MEDIO | BAIXO",
    "minStock": 10,
    "maxStock": 100,
    "active": true,
    "category": { "id": "uuid", "name": "string" }
  }],
  "totalElements": 100,
  "totalPages": 5,
  "number": 0
}
```

**POST /api/v1/items** — criar item
**PUT /api/v1/items/{id}** — editar
**DELETE /api/v1/items/{id}** — soft delete

### 4.3 Orders — `GET /api/orders`

**Response:** `List<Order>` com campos:
```json
[{
  "id": "uuid",
  "code": "string",
  "status": "RASCUNHO | CONFIRMADO | EM_PRODUCAO | PRONTOS_PARA_ENTREGA | ENTREGUE | PARCIALMENTE_RECEBIDO | CANCELADO",
  "paymentMethod": "DEPOSITO | DOCUMENTO | CARTAO_DE_CREDITO | PIX",
  "paymentTerm": "PRECOBRO | POSTCOBRO | TRINTA_DIAS | SEXENTA_DIAS | NOVENTA_DIAS",
  "notes": "string",
  "supplier": { "id": "uuid", "name": "string" },
  "requester": { "id": "uuid", "username": "string", "fullName": "string" },
  "items": []
}]
```

### 4.4 Suppliers — `GET /api/v1/suppliers`

```json
{
  "content": [{
    "id": "uuid",
    "name": "string",
    "country": "string",
    "currency": "string",
    "contactName": "string",
    "contactEmail": "string",
    "contactPhone": "string",
    "active": true
  }]
}
```

### 4.5 Requisitions — `GET /api/v1/requisitions`

```json
{
  "content": [{
    "id": "uuid",
    "code": "string",
    "status": "RASCUNHO | PENDENTE_APROVACAO | APROVADA | REPROVADA | EM_COTACAO | PEDIDO_EMITIDO | ENCERRADA | CANCELADA",
    "urgency": "NORMAL | URGENTE | EMERGENCIAL",
    "justification": "string",
    "estimatedTotalUsd": 1500.00,
    "requester": { "id": "uuid", "fullName": "string" },
    "items": []
  }]
}
```

---

## 5. Instruções de Implementação por Arquivo

### 5.1 `src/api/client.ts`

Criar instância axios com:
- `baseURL` = `import.meta.env.VITE_API_URL` (padrão `http://localhost:8080`)
- Interceptor de **request**: adiciona `Authorization: Bearer <token>` lido do Zustand store via `useAuthStore.getState().accessToken`
- Interceptor de **response**: se receber `401`, chama `POST /api/v1/auth/refresh` com o `refreshToken` do store. Se sucesso, salva novo `accessToken` e repete a request original. Se refresh falhar, chama `logout()` e redireciona para `/login`.

### 5.2 `src/store/auth.store.ts`

Zustand store com `persist` middleware salvando em `localStorage`:

```typescript
interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: {
    userId: string;
    username: string;
    fullName: string;
    email: string;
    roles: string[];
  } | null;
  isAuthenticated: boolean;
  login: (response: AuthResponse) => void;
  logout: () => void;
  setAccessToken: (token: string) => void;
}
```

### 5.3 `src/App.tsx` — Rotas

```
/login                     → LoginPage (AuthLayout) — rota pública
/                          → redirect para /dashboard
/dashboard                 → DashboardPage
/items                     → ItemsPage
/items/new                 → ItemFormPage (modo criação)
/items/:id/edit            → ItemFormPage (modo edição)
/orders                    → OrdersPage
/orders/:id                → OrderFormPage (modo visualização/edição)
/requisitions              → RequisitionsPage
/requisitions/new          → RequisitionFormPage
/suppliers                 → SuppliersPage
/suppliers/new             → SupplierFormPage
/stock                     → StockPage
```

- Usar `React.lazy` + `Suspense` para code splitting em todas as páginas
- Criar componente `ProtectedRoute` que redireciona para `/login` se `isAuthenticated === false`

### 5.4 `src/components/layout/Sidebar.tsx`

Menu lateral com:
- Logo "Austral" no topo (texto estilizado com a cor primária)
- Links de navegação com ícones do `lucide-react`:
  - **Dashboard** → ícone `LayoutDashboard`
  - **Itens** → ícone `Package`
  - **Pedidos** → ícone `ShoppingCart`
  - **Requisições** → ícone `ClipboardList`
  - **Fornecedores** → ícone `Building2`
  - **Estoque** → ícone `Warehouse`
- Link ativo com fundo `--color-primary` e texto branco
- Botão de logout no rodapé com ícone `LogOut`
- Em tela `< 768px`: sidebar se torna drawer com overlay

### 5.5 `src/components/ui/Table.tsx`

Componente genérico tipado:
```typescript
interface Column<T> {
  key: keyof T | string;
  label: string;
  render?: (value: unknown, row: T) => React.ReactNode;
}

interface TableProps<T> {
  columns: Column<T>[];
  data: T[];
  isLoading?: boolean;
  emptyMessage?: string;
}
```
Exibir **skeleton de 5 linhas** com animação `pulse` enquanto `isLoading === true`.

### 5.6 `src/components/ui/Badge.tsx`

Badge de status colorido. Mapeamento de cores:
- `RASCUNHO` / `NORMAL` → cinza
- `PENDENTE_APROVACAO` / `PENDENTE` → amarelo (`--color-warning`)
- `APROVADA` / `CONFIRMADO` / `ENTREGUE` → verde (`--color-success`)
- `REPROVADA` / `CANCELADO` → vermelho (`--color-danger`)
- `URGENTE` / `EM_PRODUCAO` → laranja
- `EMERGENCIAL` → vermelho pulsante

### 5.7 `src/pages/DashboardPage.tsx`

4 cards de métricas no topo (buscar via React Query):
- Total de Itens Ativos (`GET /api/v1/items?active=true`)
- Pedidos não entregues (`GET /api/orders` + filtro client-side)
- Requisições pendentes (`GET /api/v1/requisitions?status=PENDENTE_APROVACAO`)
- Fornecedores ativos (`GET /api/v1/suppliers?active=true`)

Gráfico de barras (Recharts `BarChart`) mostrando contagem de pedidos por status.

Tabela das últimas 5 requisições com colunas: Código, Urgência, Status, Solicitante.

### 5.8 Formulários — Padrão React Hook Form + Zod

Cada formulário deve:
1. Definir schema Zod com validações
2. Usar `useForm({ resolver: zodResolver(schema) })`
3. Mostrar mensagens de erro inline abaixo de cada campo (`{errors.campo?.message}`)
4. Botão "Salvar" com `disabled={isSubmitting}` e spinner durante envio
5. Chamar `toast.success()` no sucesso e `toast.error()` no erro

**Schema de Item:**
```typescript
const itemSchema = z.object({
  code:         z.string().min(2, 'Mínimo 2 caracteres').max(100),
  description:  z.string().min(3, 'Mínimo 3 caracteres').max(500),
  brand:        z.string().optional(),
  unitOfMeasure: z.string().min(1, 'Obrigatório'),
  criticality:  z.enum(['CRITICO', 'ALTO', 'MEDIO', 'BAIXO']),
  minStock:     z.number().min(0),
  maxStock:     z.number().min(0).optional(),
  active:       z.boolean().default(true),
});
```

**Schema de Fornecedor:**
```typescript
const supplierSchema = z.object({
  name:           z.string().min(2),
  country:        z.string().min(2),
  currency:       z.string().min(3).max(3),
  paymentTermDays: z.number().int().min(0).optional(),
  contactName:    z.string().optional(),
  contactEmail:   z.string().email().optional().or(z.literal('')),
  contactPhone:   z.string().optional(),
  active:         z.boolean().default(true),
});
```

### 5.9 `src/hooks/useItems.ts` — Padrão de hooks

```typescript
// Padrão a replicar para useOrders, useRequisitions, useSuppliers
export const useItems = (params: { page?: number; size?: number; search?: string }) =>
  useQuery({ queryKey: ['items', params], queryFn: () => itemsApi.list(params) });

export const useItem = (id: string) =>
  useQuery({ queryKey: ['items', id], queryFn: () => itemsApi.findById(id), enabled: !!id });

export const useCreateItem = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: itemsApi.create,
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['items'] }); },
  });
};

export const useUpdateItem = () => { /* mesmo padrão */ };
export const useDeleteItem = () => { /* mesmo padrão */ };
```

---

## 6. Tabela de Páginas e Funcionalidades

| Página | Funcionalidades |
|---|---|
| **Login** | Form com campos `login` (email ou username) e `password`, validação, exibição de erro da API, redirect para `/dashboard` no sucesso |
| **Dashboard** | 4 KPI cards, gráfico de barras de pedidos por status, tabela das últimas requisições |
| **Items** | Tabela paginada com busca por nome/código, filtro por ativo/inativo, botão "Novo Item", ações editar e deletar por linha |
| **ItemForm** | Form criação/edição com todos os campos da entidade, select de unidade de medida e criticidade |
| **Orders** | Tabela com Badge de status colorido, filtro por status, botão "Novo Pedido" |
| **OrderForm** | Seleção de fornecedor via dropdown, lista de itens com quantidade, notas |
| **Requisitions** | Tabela com Badge de urgência e status, filtro por status |
| **RequisitionForm** | Adição dinâmica de múltiplos itens, campo de urgência e justificativa |
| **Suppliers** | Tabela paginada com busca, toggle ativo/inativo, botão "Novo Fornecedor" |
| **SupplierForm** | Form com dados de contato, país, moeda e prazo de pagamento |
| **Stock** | Tabela somente leitura com indicador visual vermelho quando `quantity < minStock` |

---

## 7. Variáveis de Ambiente

**`frontend/.env.example`:**
```env
VITE_API_URL=http://localhost:8080
```

---

## 8. Dockerfile do Frontend

```dockerfile
# Stage 1 — build
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Stage 2 — serve com nginx
FROM nginx:1.25-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

**`frontend/nginx.conf`:**
```nginx
server {
  listen 80;
  root /usr/share/nginx/html;
  index index.html;

  location / {
    try_files $uri $uri/ /index.html;
  }
}
```

---

## 9. `vite.config.ts`

```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})
```

---

## 10. Ordem de Escrita dos Arquivos

O LLM deve criar os arquivos **nesta ordem** para evitar erros de dependência:

1. `package.json` + `vite.config.ts` + `tsconfig.json`
2. `src/index.css` (design system completo)
3. `src/types/*.ts` (todos os tipos TypeScript)
4. `src/api/client.ts`
5. `src/api/*.api.ts` (auth, items, orders, requisitions, suppliers, stock)
6. `src/store/auth.store.ts`
7. `src/hooks/*.ts` (useItems, useOrders, useRequisitions, useSuppliers)
8. `src/components/ui/*.tsx` (Button, Input, Select, Modal, Table, Badge, Card, Spinner, EmptyState)
9. `src/components/layout/*.tsx` (Sidebar, Header, AppLayout, AuthLayout)
10. `src/pages/LoginPage.tsx`
11. `src/App.tsx` + `src/main.tsx`
12. Páginas: Dashboard → Items → Orders → Requisitions → Suppliers → Stock
13. `Dockerfile` + `nginx.conf` + `.env.example`

---

## 11. Checklist de Qualidade

- [ ] Nenhum `any` no TypeScript sem comentário justificando
- [ ] Todos os formulários têm validação Zod com mensagens em português
- [ ] Todas as chamadas de API tratam erro com `toast.error()`
- [ ] Todas as páginas definem `document.title` dinamicamente
- [ ] Loading states (skeleton/spinner) em todas as tabelas e formulários
- [ ] Layout responsivo — sidebar vira drawer em mobile (< 768px)
- [ ] Token JWT salvo em `localStorage` via Zustand `persist`
- [ ] Refresh token implementado no interceptor axios
- [ ] `VITE_API_URL` usada em todo lugar, sem URL hardcoded
- [ ] Code splitting via `React.lazy` em todas as páginas
