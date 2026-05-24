import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { AppLayout } from '../../components/layout/AppLayout';
import { Table } from '../../components/ui/Table';
import { stockApi } from '../../api/stock.api';
import { StockAlert, StockBalance } from '../../types/stock.types';

export const StockPage: React.FC = () => {
  document.title = 'Estoque - Austral Estoque';

  const { data: stock, isLoading } = useQuery({
    queryKey: ['stock'],
    queryFn: () => stockApi.list(),
  });

  const { data: alerts, isLoading: isLoadingAlerts } = useQuery({
    queryKey: ['stock-alerts', 'RED'],
    queryFn: () => stockApi.getAlerts({ status: 'RED' }),
  });

  const statusLabel = (status: StockBalance['statusColor']) => {
    if (status === 'RED') return 'Critico';
    if (status === 'YELLOW') return 'Atencao';
    return 'Normal';
  };

  const statusClass = (status: StockBalance['statusColor']) => {
    if (status === 'RED') return 'text-danger';
    if (status === 'YELLOW') return 'text-warning';
    return 'text-success';
  };

  return (
    <AppLayout>
      <div className="space-y-4">
        <h1 className="text-2xl font-bold text-text">Estoque</h1>

        <Table<StockBalance>
          columns={[
            { key: 'itemCode', label: 'Codigo' },
            { key: 'itemDescription', label: 'Descricao' },
            { key: 'warehouseName', label: 'Armazem' },
            {
              key: 'quantity',
              label: 'Quantidade',
              render: (value, row) => (
                <span className={statusClass(row.statusColor)}>
                  {Number(value)}
                </span>
              ),
            },
            { key: 'unitOfMeasure', label: 'Unidade' },
            {
              key: 'plannedQuantity',
              label: 'Planejado',
              render: (value) => (
                <span className="text-text">
                  {value == null ? '-' : Number(value)}
                </span>
              ),
            },
            {
              key: 'percentageOfPlanned',
              label: '% Planejado',
              render: (value) => value == null ? '-' : `${Number(value).toFixed(1)}%`,
            },
            {
              key: 'statusColor',
              label: 'Semaforo',
              render: (value) => (
                <span className={statusClass(value as StockBalance['statusColor'])}>
                  {statusLabel(value as StockBalance['statusColor'])}
                </span>
              ),
            },
          ]}
          data={stock || []}
          isLoading={isLoading}
        />

        <div className="space-y-3">
          <h2 className="text-xl font-semibold text-text">Alertas criticos</h2>
          <Table<StockAlert>
            columns={[
              { key: 'itemCode', label: 'Codigo' },
              { key: 'itemDescription', label: 'Descricao' },
              { key: 'warehouseName', label: 'Armazem' },
              { key: 'quantityOnHand', label: 'Saldo' },
              { key: 'plannedQuantity', label: 'Planejado' },
              {
                key: 'percentageOfPlanned',
                label: '% Planejado',
                render: (value) => `${Number(value).toFixed(1)}%`,
              },
              {
                key: 'statusColor',
                label: 'Status',
                render: () => <span className="text-danger">Critico</span>,
              },
            ]}
            data={alerts || []}
            isLoading={isLoadingAlerts}
            emptyMessage="Nenhum alerta critico encontrado."
          />
        </div>
      </div>
    </AppLayout>
  );
};
