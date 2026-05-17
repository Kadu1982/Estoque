import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { AppLayout } from '../../components/layout/AppLayout';
import { Table } from '../../components/ui/Table';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { useSuppliers, useDeleteSupplier } from '../../hooks/useSuppliers';
import { Supplier } from '../../types/supplier.types';
import { toast } from 'react-hot-toast';

export const SuppliersPage: React.FC = () => {
  document.title = 'Fornecedores - Austral Estoque';

  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);

  const { data, isLoading, refetch } = useSuppliers({ search, page, size: 10 });
  const deleteMutation = useDeleteSupplier();

  const handleDelete = async (id: string) => {
    if (window.confirm('Tem certeza que deseja excluir este fornecedor?')) {
      try {
        await deleteMutation.mutateAsync(id);
        toast.success('Fornecedor excluído com sucesso!');
        refetch();
      } catch (error) {
        toast.error('Erro ao excluir fornecedor');
      }
    }
  };

  return (
    <AppLayout>
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-bold text-text">Fornecedores</h1>
          <Link to="/suppliers/new">
            <Button>Novo Fornecedor</Button>
          </Link>
        </div>

        <Input
          placeholder="Buscar fornecedores..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="max-w-xs"
        />

        <Table<Supplier>
          columns={[
            { key: 'name', label: 'Nome' },
            { key: 'country', label: 'País' },
            { key: 'currency', label: 'Moeda' },
            {
              key: 'active',
              label: 'Status',
              render: (value) => (
                <span className={value ? 'text-success' : 'text-danger'}>
                  {value ? 'Ativo' : 'Inativo'}
                </span>
              ),
            },
            {
              key: 'id',
              label: 'Ações',
              render: (value: string, row: Supplier) => (
                <div className="flex gap-2">
                  <Link
                    to={`/suppliers/${row.id}/edit`}
                    className="text-primary hover:underline"
                  >
                    Editar
                  </Link>
                  <button
                    onClick={() => handleDelete(row.id)}
                    className="text-danger hover:underline"
                  >
                    Excluir
                  </button>
                </div>
              ),
            },
          ]}
          data={data?.content || []}
          isLoading={isLoading}
        />

        {data && data.totalPages > 1 && (
          <div className="flex justify-center gap-2 mt-4">
            {Array.from({ length: data.totalPages }, (_, i) => (
              <button
                key={i}
                onClick={() => setPage(i)}
                className={`px-3 py-1 rounded ${
                  page === i ? 'bg-primary text-white' : 'bg-surface-2 text-text'
                }`}
              >
                {i + 1}
              </button>
            ))}
          </div>
        )}
      </div>
    </AppLayout>
  );
};