import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { AppLayout } from '../components/layout/AppLayout';
import { Card } from '../components/ui/Card';
import { Table } from '../components/ui/Table';
import { Badge } from '../components/ui/Badge';
import { Spinner } from '../components/ui/Spinner';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { dashboardApi } from '../api/dashboard.api';
import { DashboardSummary } from '../types/dashboard.types';

export const DashboardPage: React.FC = () => {
  document.title = 'Dashboard - Austral Estoque';

  const { data, isLoading } = useQuery<DashboardSummary>({
    queryKey: ['dashboard-summary'],
    queryFn: dashboardApi.summary,
  });

  const chartData = data?.orderStatusCounts || [];

  return (
    <AppLayout>
      <div className="space-y-6">
        <h1 className="text-2xl font-bold text-text">Dashboard</h1>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <Card>
            <div className="text-center">
              <p className="text-sm text-text-muted mb-1">Itens Ativos</p>
              <p className="text-2xl font-bold text-text">
                {isLoading ? <Spinner /> : data?.activeItems || 0}
              </p>
            </div>
          </Card>

          <Card>
            <div className="text-center">
              <p className="text-sm text-text-muted mb-1">Pedidos nao entregues</p>
              <p className="text-2xl font-bold text-text">
                {isLoading ? <Spinner /> : data?.nonDeliveredOrders || 0}
              </p>
            </div>
          </Card>

          <Card>
            <div className="text-center">
              <p className="text-sm text-text-muted mb-1">Requisicoes pendentes</p>
              <p className="text-2xl font-bold text-text">
                {isLoading ? <Spinner /> : data?.pendingRequisitions || 0}
              </p>
            </div>
          </Card>

          <Card>
            <div className="text-center">
              <p className="text-sm text-text-muted mb-1">Fornecedores ativos</p>
              <p className="text-2xl font-bold text-text">
                {isLoading ? <Spinner /> : data?.activeSuppliers || 0}
              </p>
            </div>
          </Card>
        </div>

        <Card title="Pedidos por Status">
          <div className="h-64">
            {isLoading ? (
              <div className="flex items-center justify-center h-full">
                <Spinner />
              </div>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#2a2d3e" />
                  <XAxis dataKey="status" stroke="#64748b" />
                  <YAxis stroke="#64748b" />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: '#1a1d27',
                      border: '1px solid #2a2d3e',
                      color: '#e2e8f0',
                    }}
                  />
                  <Bar dataKey="count" fill="#4f6ef7" />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        </Card>

        <Card title="Ultimas Requisicoes">
          <Table
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
                key: 'requesterName',
                label: 'Solicitante',
              },
            ]}
            data={data?.latestRequisitions || []}
            isLoading={isLoading}
            emptyMessage="Nenhuma requisicao encontrada"
          />
        </Card>
      </div>
    </AppLayout>
  );
};
