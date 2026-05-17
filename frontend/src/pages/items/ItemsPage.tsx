import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { AppLayout } from '../../components/layout/AppLayout';
import { Table } from '../../components/ui/Table';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { useItems, useDeleteItem } from '../../hooks/useItems';
import { Item } from '../../types/item.types';
import { toast } from 'react-hot-toast';

export const ItemsPage: React.FC = () => {
  document.title = 'Itens - Austral Estoque';

  const [search, setSearch] = useState('');
  const [activeFilter, setActiveFilter] = useState<boolean | undefined>(undefined);
  const [page, setPage] = useState(0);

  const { data, isLoading, refetch } = useItems({
    search,
    active: activeFilter,
    page,
    size: 10,
  });

  const deleteMutation = useDeleteItem();

  const handleDelete = async (id: string) => {
    if (window.confirm('Tem certeza que deseja excluir este item?')) {
      try {
        await deleteMutation.mutateAsync(id);
        toast.success('Item excluído com sucesso!');
        refetch();
      } catch (error) {
        toast.error('Erro ao excluir item');
      }
    }
  };

  return (
    <AppLayout>
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-bold text-text">Itens</h1>
          <Link to="/items/new">
            <Button>Novo Item</Button>
          </Link>
        </div>

        <div className="flex gap-4">
          <Input
            placeholder="Buscar por código ou descrição..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="max-w-xs"
          />
        </div>

        <Table<Item>
          columns={[
            { key: 'code', label: 'Código' },
            { key: 'description', label: 'Descrição' },
            { key: 'brand', label: 'Marca' },
            { key: 'unitOfMeasure', label: 'Unidade' },
            {
              key: 'criticality',
              label: 'Criticidade',
              render: (value) => <Badge status={String(value)} />,
            },
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
              render: (value: string, row: Item) => (
                <div className="flex gap-2">
                  <Link
                    to={`/items/${row.id}/edit`}
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

        {/* Paginação simples */}
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