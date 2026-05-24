# Plano de Implementacao para Produto 100% Funcional

## Premissa objetiva

O projeto atual ja tem um MVP funcional parcial: autenticacao, fornecedores, itens, requisicoes, pedidos, recebimento e consulta basica de estoque. Ele ainda nao e a plataforma completa descrita para Austral Congo. Para chegar a um produto 100% funcional, o trabalho precisa ser dividido em entregas fechadas, testaveis e implantaveis.

"100% funcional" aqui significa:

- usuarios conseguem operar os fluxos principais sem acesso direto ao banco;
- backend, frontend e banco estao consistentes;
- cada modulo tem regras de negocio, validacoes, auditoria, permissoes e testes;
- dashboards mostram dados reais;
- operacoes criticas geram rastreabilidade;
- o sistema roda em ambiente local, homologacao e producao com procedimento documentado.

## Estado atual do repositorio

### Ja existe

- Backend Spring Boot 3.3.5 com Java 21.
- Frontend React 18 + Vite + TypeScript.
- Banco PostgreSQL via Flyway.
- Autenticacao JWT com refresh token.
- CRUD de itens e fornecedores.
- Requisicoes, aprovacao basica, pedidos e recebimento.
- Estoque basico por item e almoxarifado.
- Dashboard inicial.
- Docker Compose e scripts de operacao.

### Lacunas reais

- Nao ha modelo completo de almoxarifado central vs descentralizado.
- Nao ha alerta persistido de estoque em 30%.
- Nao ha transferencia formal central -> descentralizado.
- Nao ha saida de consumo por setor/equipamento.
- Nao ha modulo de ativos, manutencao e custo por equipamento.
- Nao ha Agua para Todos.
- Nao ha Qualidade/Vigilancia da Agua.
- Nao ha SST.
- Nao ha controle granular por perfis como ADMIN_GLOBAL, COMPRAS, ALMOX_CENTRAL etc.
- Nao ha internacionalizacao PT/FR/EN.
- Nao ha app mobile/offline.
- Nao ha integracao real com NF/XML.
- Nao ha trilha de auditoria aplicada em todas as operacoes criticas.

## Ordem correta de construcao

### Fase 0 - Estabilizacao do MVP atual

Objetivo: garantir que a base existente compila, testa e roda antes de expandir.

Entregas:

- Corrigir divergencias entre contrato de API e frontend.
- Padronizar erros no backend.
- Garantir build limpo do backend e frontend.
- Revisar seeds para ambiente dev/test.
- Documentar usuarios padrao, portas e comandos.

Arquivos provaveis:

- `backend/src/main/java/com/austral/estoque/exception/GlobalExceptionHandler.java`
- `backend/src/test/java/com/austral/estoque/integration/OperationalFlowIntegrationTest.java`
- `frontend/src/api/*.ts`
- `frontend/src/types/*.ts`
- `README.md`

Validacao:

- `cd backend && mvn test`
- `cd frontend && npm run build`
- fluxo manual: login -> criar item -> criar fornecedor -> criar requisicao -> aprovar -> criar pedido -> receber -> consultar estoque.

### Fase 1 - Almoxarifado completo e alertas criticos

Objetivo: transformar estoque basico em operacao real de almoxarifado central/descentralizado.

Entregas:

- Tipo de almoxarifado: CENTRAL ou DESCENTRALIZADO.
- Nivel planejado por almoxarifado + item.
- Calculo de percentual vs planejado.
- Status GREEN/YELLOW/RED:
  - GREEN: acima de 60%.
  - YELLOW: entre 30% e 60%.
  - RED: menor ou igual a 30%.
- Tabela persistente de alertas de estoque.
- Endpoint `GET /api/v1/stock-alerts`.
- Recalculo automatico apos qualquer movimento.
- Notificacao inicial registrada em banco, mesmo que envio externo venha depois.

Arquivos provaveis:

- Criar `backend/src/main/java/com/austral/estoque/domain/stock/StockAlert.java`
- Criar `backend/src/main/java/com/austral/estoque/domain/stock/WarehouseItemPlan.java`
- Criar `backend/src/main/java/com/austral/estoque/repository/stock/StockAlertRepository.java`
- Criar `backend/src/main/java/com/austral/estoque/repository/stock/WarehouseItemPlanRepository.java`
- Criar `backend/src/main/java/com/austral/estoque/controller/StockAlertController.java`
- Modificar `backend/src/main/java/com/austral/estoque/service/StockService.java`
- Criar migracao Flyway `backend/src/main/resources/db/migration/V2__stock_alerts_and_plans.sql`
- Modificar frontend em `frontend/src/pages/stock/StockPage.tsx`

Validacao:

- teste unitario para calculo de status;
- teste de integracao: receber estoque, consumir ate 30%, verificar alerta criado;
- tela de estoque deve mostrar semaforo real.

### Fase 2 - Movimentos operacionais: transferencia e consumo

Objetivo: permitir que o almoxarifado central abasteca unidades e que unidades registrem consumo.

Entregas:

- Endpoint `POST /api/v1/stock-transfers`.
- Transferencia atomica: baixa origem e entrada destino.
- Bloqueio de transferencia sem saldo.
- Endpoint `POST /api/v1/issues`.
- Saida de consumo vinculada a setor, centro de custo e ativo opcional.
- Historico completo em `stock_movements`.
- Tela para transferencias.
- Tela para saidas/consumo.

Arquivos provaveis:

- Criar `backend/src/main/java/com/austral/estoque/dto/stock/StockTransferRequest.java`
- Criar `backend/src/main/java/com/austral/estoque/dto/stock/StockIssueRequest.java`
- Criar `backend/src/main/java/com/austral/estoque/controller/StockTransferController.java`
- Criar `backend/src/main/java/com/austral/estoque/controller/StockIssueController.java`
- Criar `backend/src/main/java/com/austral/estoque/service/StockMovementService.java`
- Modificar `frontend/src/api/stock.api.ts`
- Criar telas em `frontend/src/pages/stock/`

Validacao:

- transferencia com saldo suficiente altera origem e destino;
- transferencia sem saldo retorna 400;
- consumo reduz estoque e dispara alerta se cruzar 30%;
- movimentos aparecem no historico.

### Fase 3 - Compras mais completas

Objetivo: fechar o ciclo requisicao -> cotacao -> pedido -> recebimento -> estoque.

Entregas:

- Requisicoes com itens completos.
- Aprovar/rejeitar por perfil e alcada.
- Cotacoes e mapa comparativo.
- Pedido de compra gerado a partir de requisicao aprovada/cotacao.
- Recebimento parcial e total.
- Divergencia de recebimento.
- Entrada por nota fiscal manual.

Arquivos provaveis:

- Expandir `backend/src/main/java/com/austral/estoque/domain/requisition/`
- Expandir `backend/src/main/java/com/austral/estoque/domain/order/`
- Criar dominio de cotacao se ainda nao houver entidade Java completa.
- Criar controllers para cotacoes.
- Criar telas `frontend/src/pages/quotations/`

Validacao:

- requisicao nao aprovada nao pode gerar pedido;
- pedido recebido parcialmente fica PARCIALMENTE_RECEBIDO;
- divergencia fica registrada;
- estoque so aumenta por recebimento valido.

### Fase 4 - Patrimonio, ativos e manutencao

Objetivo: rastrear equipamentos, maquinas, veiculos e custo por ativo.

Entregas:

- CRUD de ativos.
- Tipos: EQUIPAMENTO_SAUDE, MAQUINA, VEICULO, OUTRO.
- Vinculo com unidade, setor e centro de custo.
- Ordens de manutencao.
- Consumo de pecas via `POST /issues` com `assetId`.
- Relatorio de custo por ativo.

Arquivos provaveis:

- Criar `backend/src/main/java/com/austral/estoque/domain/asset/`
- Criar `backend/src/main/java/com/austral/estoque/controller/AssetController.java`
- Criar `backend/src/main/java/com/austral/estoque/controller/MaintenanceOrderController.java`
- Criar `frontend/src/pages/assets/`

Validacao:

- ativo aparece em cadastro e lookup;
- consumo com `assetId` entra no custo do ativo;
- relatorio soma pecas e custos corretamente.

### Fase 5 - Agua para Todos

Objetivo: cadastrar infraestrutura de agua e conectar operacao a estoque/manutencao.

Entregas:

- CRUD de pocos/pontos de agua.
- Comunidade atendida, populacao, coordenadas, capacidade.
- Historico de instalacao e manutencao.
- Vinculo com materiais consumidos.
- Tela `/water/wells`.

Arquivos provaveis:

- Criar `backend/src/main/java/com/austral/estoque/domain/water/WaterPoint.java`
- Criar `backend/src/main/java/com/austral/estoque/controller/water/WaterWellController.java`
- Criar `frontend/src/pages/water/`

Validacao:

- cadastrar poco;
- registrar manutencao;
- associar consumo de material;
- listar por comunidade/unidade.

### Fase 6 - Qualidade e vigilancia da agua

Objetivo: transformar o modulo de agua em vigilancia operacional.

Entregas:

- Pontos de coleta.
- Plano amostral por ponto.
- Agenda de coletas.
- Registro de amostra.
- Cadeia de custodia.
- Resultado analitico.
- Classificacao conforme/nao conforme.
- Acao corretiva.
- Consumo de reagentes, frascos e EPIs via estoque.

Arquivos provaveis:

- Criar `backend/src/main/java/com/austral/estoque/domain/waterquality/`
- Criar controllers em `backend/src/main/java/com/austral/estoque/controller/waterquality/`
- Criar telas em `frontend/src/pages/water-quality/`

Validacao:

- coleta agendada aparece em rota;
- resultado fora do limite gera nao conformidade;
- nao conformidade permite acao corretiva;
- consumo de insumos baixa estoque.

### Fase 7 - Hospitalar minimo

Objetivo: permitir controle real de insumos e equipamentos hospitalares, sem ainda virar prontuario clinico.

Entregas:

- Tipos de item hospitalar e medicamento preparatorio.
- Setores hospitalares.
- Almoxarifado hospitalar.
- Consumo por setor e equipamento.
- Controle de lote e validade para itens que exigem isso.
- Alertas por validade proxima.

Arquivos provaveis:

- Expandir `items` com flags de lote/validade/serie.
- Expandir estoque para lote quando aplicavel.
- Criar telas hospitalares se necessario.

Validacao:

- item com validade exige validade no recebimento;
- item vencido nao pode ser consumido sem permissao especial;
- relatorio mostra consumo por setor hospitalar.

### Fase 8 - SST e saude ocupacional

Objetivo: registrar riscos ocupacionais e incidentes ligados a operacoes.

Entregas:

- Exposicoes ocupacionais.
- EPIs entregues.
- Exames ocupacionais.
- Incidentes.
- Vinculo com unidade, setor, atividade e ativo.
- Dashboard de incidentes e pendencias.

Arquivos provaveis:

- Criar `backend/src/main/java/com/austral/estoque/domain/ohs/`
- Criar `backend/src/main/java/com/austral/estoque/controller/ohs/`
- Criar `frontend/src/pages/ohs/`

Validacao:

- incidente registrado gera historico;
- exposicao pode ser vinculada a colaborador/setor;
- dashboard agrega eventos por unidade.

### Fase 9 - Seguranca, perfis e auditoria

Objetivo: impedir acesso indevido e deixar rastro auditavel.

Entregas:

- Perfis: ADMIN_GLOBAL, COMPRAS, ALMOX_CENTRAL, ALMOX_DESCENTRALIZADO, GESTOR_UNIDADE, ENGENHARIA_ATIVOS, AGUA_QUALIDADE, SST, FINANCEIRO_CONTROLADORIA.
- Guards no backend por endpoint.
- Guards no frontend por rota/menu.
- Auditoria de operacoes criticas.
- Logs de acesso a dados sensiveis.

Validacao:

- usuario sem perfil nao acessa endpoint;
- menu esconde modulo sem permissao;
- operacao critica cria audit log;
- testes cobrem acesso permitido e negado.

### Fase 10 - Internacionalizacao e producao

Objetivo: preparar uso real em Congo-Brazzaville.

Entregas:

- PT/FR/EN no frontend.
- Timezone configuravel.
- Moedas e formatos numericos.
- Hardening de Docker/infra.
- Backups e restore testados.
- Smoke test de producao.
- Monitoramento basico.

Validacao:

- build de producao passa;
- compose de producao valida;
- backup e restore rodam em ambiente de homologacao;
- smoke test cobre login e fluxo essencial.

### Fase 11 - Mobile/offline

Objetivo: permitir campo sem conectividade.

Entregas:

- Definir se sera PWA ou app mobile separado.
- Login e cache offline.
- Fila local de operacoes.
- Sincronizacao posterior.
- Resolucao de conflitos.
- Coletas de agua offline.
- Consumo de estoque offline com reconciliacao.

Validacao:

- registrar coleta sem internet;
- sincronizar quando voltar conexao;
- conflito de estoque e tratado sem corromper saldo.

## Sequencia recomendada de commits

1. `chore: stabilize current mvp`
2. `feat: add warehouse item planning`
3. `feat: add stock alert generation`
4. `feat: add stock alerts ui`
5. `feat: add stock transfers`
6. `feat: add stock issues`
7. `feat: add assets and maintenance`
8. `feat: add water wells`
9. `feat: add water quality sampling`
10. `feat: add hospital stock controls`
11. `feat: add ohs records`
12. `feat: enforce role based access`
13. `feat: add i18n`
14. `feat: add offline field workflow`

## Criterios de aceite globais

- Backend: `mvn test` passa sem falhas.
- Frontend: `npm run build` passa sem falhas.
- Cada modulo tem pelo menos um teste de integracao cobrindo fluxo feliz e uma falha de negocio.
- Nenhuma operacao critica ocorre sem usuario autenticado.
- Estoque nunca pode ficar negativo por operacao normal.
- Alerta vermelho e gerado ao cruzar 30%.
- Transferencia e consumo geram movimento rastreavel.
- Recebimento de pedido atualiza estoque uma unica vez.
- Auditoria registra criacao, alteracao, aprovacao, recebimento, transferencia, consumo e ajuste.
- Dashboards nao usam dados mockados em ambiente real.

## Riscos principais

- O escopo e grande demais para uma unica entrega. Tentar implementar tudo junto aumenta a chance de quebrar o MVP atual.
- O contrato de API descrito pelo produto nao bate 100% com o codigo existente. Isso precisa ser tratado antes de expandir frontend.
- Offline first e mobile nao devem ser colocados antes de estoque, alertas, transferencias e consumo estarem corretos.
- Dados de saude e SST exigem governanca forte. Implementar sem RBAC e auditoria seria tecnicamente irresponsavel.
- Controle por lote/validade muda a modelagem de estoque. Deve ser feito antes de medicamentos reais.

## Proxima acao recomendada

Comecar pela Fase 0 e Fase 1. A primeira entrega realmente valiosa deve ser:

1. estabilizar build/testes atuais;
2. adicionar planejamento de estoque por almoxarifado e item;
3. implementar alerta persistido de 30%;
4. mostrar alertas e semaforo no frontend.

Essa entrega cria a base operacional para todas as outras fases.
