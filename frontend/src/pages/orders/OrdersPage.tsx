import React, { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { AppLayout } from '../../components/layout/AppLayout';
import { Table } from '../../components/ui/Table';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Select } from '../../components/ui/Select';
import { useOrders, useReceiveOrder } from '../../hooks/useOrders';
import { Order } from '../../types/order.types';
import { toast } from 'react-hot-toast';
import { lookupsApi } from '../../api/lookups.api';

export const OrdersPage: React.FC = () => {
  document.title = 'Pedidos - Austral Estoque';

  const { data: orders, isLoading } = useOrders();
  const receiveMutation = useReceiveOrder();
  const { data: warehouses } = useQuery({
    queryKey: ['lookup-warehouses'],
    queryFn: lookupsApi.warehouses,
  });
  const { data: users } = useQuery({
    queryKey: ['lookup-users'],
    queryFn: lookupsApi.users,
  });

  const [warehouseId, setWarehouseId] = useState('');
  const [receiverId, setReceiverId] = useState('');

  const warehouseOptions = useMemo(
    () => [{ value: '', label: 'Selecione armazem' }, ...(warehouses || []).map((w) => ({ value: w.id, label: w.label }))],
    [warehouses]
  );
  const userOptions = useMemo(
    () => [{ value: '', label: 'Selecione recebedor' }, ...(users || []).map((u) => ({ value: u.id, label: u.label }))],
    [users]
  );

  const handleReceive = async (order: Order) => {
    if (!warehouseId || !receiverId) {
      toast.error('Selecione armazem e recebedor');
      return;
    }

    const items = (order.items || [])
      .filter((i) => i.id)
      .map((i) => {
        const received = Number(i.receivedQuantity || 0);
        const quantity = Number(i.quantity || 0);
        const pending = quantity - received;
        return {
          orderItemId: String(i.id),
          quantity: pending > 0 ? pending : 0,
        };
      })
      .filter((i) => i.quantity > 0);

    if (items.length === 0) {
      toast.error('Nao ha itens pendentes para receber');
      return;
    }

    try {
      await receiveMutation.mutateAsync({
        id: order.id,
        data: {
          warehouseId,
          receiverId,
          items,
        },
      });
      toast.success('Recebimento registrado');
    } catch {
      toast.error('Erro ao registrar recebimento');
    }
  };

  return (
    <AppLayout>
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-bold text-text">Pedidos</h1>
          <Link to="/orders/new">
            <Button>Novo Pedido</Button>
          </Link>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Select
            label="Armazem de recebimento"
            options={warehouseOptions}
            value={warehouseId}
            onChange={(e) => setWarehouseId(e.target.value)}
          />
          <Select
            label="Recebedor"
            options={userOptions}
            value={receiverId}
            onChange={(e) => setReceiverId(e.target.value)}
          />
        </div>

        <Table<Order>
          columns={[
            { key: 'code', label: 'Codigo' },
            {
              key: 'status',
              label: 'Status',
              render: (value) => <Badge status={String(value)} />,
            },
            {
              key: 'supplier',
              label: 'Fornecedor',
              render: (value: Order['supplier']) => value?.name || '-',
            },
            {
              key: 'id',
              label: 'Acoes',
              render: (_value: string, row: Order) => (
                <div className="flex gap-2">
                  <Link to={`/orders/${row.id}`} className="text-primary hover:underline">
                    Ver
                  </Link>
                  {row.status !== 'ENTREGUE' && row.status !== 'CANCELADO' && (
                    <button
                      type="button"
                      className="text-success hover:underline"
                      onClick={() => handleReceive(row)}
                    >
                      Receber
                    </button>
                  )}
                </div>
              ),
            },
          ]}
          data={orders || []}
          isLoading={isLoading}
        />
      </div>
    </AppLayout>
  );
};
