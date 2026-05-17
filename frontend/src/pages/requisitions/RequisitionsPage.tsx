import React from 'react';
import { Link } from 'react-router-dom';
import { AppLayout } from '../../components/layout/AppLayout';
import { Table } from '../../components/ui/Table';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { useApproveRequisition, useRejectRequisition, useRequisitions } from '../../hooks/useRequisitions';
import { Requisition } from '../../types/requisition.types';
import { toast } from 'react-hot-toast';

export const RequisitionsPage: React.FC = () => {
  document.title = 'Requisicoes - Austral Estoque';

  const { data, isLoading } = useRequisitions();
  const approveMutation = useApproveRequisition();
  const rejectMutation = useRejectRequisition();

  const handleApprove = async (id: string) => {
    try {
      await approveMutation.mutateAsync(id);
      toast.success('Requisicao aprovada');
    } catch {
      toast.error('Erro ao aprovar requisicao');
    }
  };

  const handleReject = async (id: string) => {
    try {
      await rejectMutation.mutateAsync(id);
      toast.success('Requisicao reprovada');
    } catch {
      toast.error('Erro ao reprovar requisicao');
    }
  };

  return (
    <AppLayout>
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-bold text-text">Requisicoes</h1>
          <Link to="/requisitions/new">
            <Button>Nova Requisicao</Button>
          </Link>
        </div>

        <Table<Requisition>
          columns={[
            { key: 'code', label: 'Codigo' },
            {
              key: 'urgency',
              label: 'Urgencia',
              render: (value) => <Badge urgency={String(value)} />,
            },
            {
              key: 'status',
              label: 'Status',
              render: (value) => <Badge status={String(value)} />,
            },
            {
              key: 'requester',
              label: 'Solicitante',
              render: (value: Requisition['requester']) => value?.fullName || '-',
            },
            {
              key: 'estimatedTotalUsd',
              label: 'Total (USD)',
              render: (value) => `$${Number(value || 0).toFixed(2)}`,
            },
            {
              key: 'id',
              label: 'Acoes',
              render: (_value: string, row: Requisition) => (
                <div className="flex gap-2">
                  <Link to={`/requisitions/${row.id}`} className="text-primary hover:underline">
                    Ver
                  </Link>
                  {row.status === 'PENDENTE_APROVACAO' && (
                    <>
                      <button
                        type="button"
                        className="text-success hover:underline"
                        onClick={() => handleApprove(row.id)}
                      >
                        Aprovar
                      </button>
                      <button
                        type="button"
                        className="text-danger hover:underline"
                        onClick={() => handleReject(row.id)}
                      >
                        Reprovar
                      </button>
                    </>
                  )}
                </div>
              ),
            },
          ]}
          data={data?.content || []}
          isLoading={isLoading}
        />
      </div>
    </AppLayout>
  );
};
