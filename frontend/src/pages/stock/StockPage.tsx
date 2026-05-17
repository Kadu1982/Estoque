import React from 'react';
import { AppLayout } from '../../components/layout/AppLayout';
import { Table } from '../../components/ui/Table';
import { Badge } from '../../components/ui/Badge';
import { useQuery } from '@tanstack/react-query';
import { stockApi } from '../../api/stock.api';
import { StockBalance } from '../../types/stock.types';

export const StockPage: React.FC = () => {
  document.title = 'Estoque - Austral Estoque';

  const { data: stock, isLoading } = useQuery({
    queryKey: ['stock'],
    queryFn: () => stockApi.list(),
  });

  return (
    <AppLayout>
      <div className="space-y-4">
        <h1 className="text-2xl font-bold text-text">Estoque</h1>

        <Table<StockBalance>
          columns={[
            { key: 'itemCode', label: 'Código' },
            { key: 'itemDescription', label: 'Descrição' },
            { key: 'warehouseName', label: 'Armazém' },
            {
              key: 'quantity',
              label: 'Quantidade',
              render: (value, row) => (
                <span className={Number(value) < row.minStock ? 'text-danger' : 'text-text'}>
                  {Number(value)}
                </span>
              ),
            },
            { key: 'unitOfMeasure', label: 'Unidade' },
            {
              key: 'minStock',
              label: 'Est. Mín.',
              render: (value, row) => (
                <span className={Number(row.quantity) < Number(value) ? 'text-danger' : 'text-text'}>
                  {Number(value)}
                </span>
              ),
            },
            {
              key: 'quantity',
              label: 'Status',
              render: (value, row) => {
                const isLow = Number(value) < row.minStock;
                return (
                  <span className={isLow ? 'text-danger' : 'text-success'}>
                    {isLow ? 'Abaixo do mínimo' : 'Normal'}
                  </span>
                );
              },
            },
          ]}
          data={stock || []}
          isLoading={isLoading}
        />
      </div>
    </AppLayout>
  );
};