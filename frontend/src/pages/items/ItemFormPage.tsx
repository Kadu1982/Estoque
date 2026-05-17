import React, { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { AppLayout } from '../../components/layout/AppLayout';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { Select } from '../../components/ui/Select';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useItem, useCreateItem, useUpdateItem } from '../../hooks/useItems';
import { toast } from 'react-hot-toast';

const itemSchema = z.object({
  code: z.string().min(2, 'Mínimo 2 caracteres').max(100),
  description: z.string().min(3, 'Mínimo 3 caracteres').max(500),
  brand: z.string().optional(),
  unitOfMeasure: z.string().min(1, 'Obrigatório'),
  criticality: z.enum(['CRITICO', 'ALTO', 'MEDIO', 'BAIXO']),
  minStock: z.number().min(0),
  maxStock: z.number().min(0).optional(),
  active: z.boolean().default(true),
});

type ItemFormData = z.infer<typeof itemSchema>;

const criticalityOptions = [
  { value: 'CRITICO', label: 'Crítico' },
  { value: 'ALTO', label: 'Alto' },
  { value: 'MEDIO', label: 'Médio' },
  { value: 'BAIXO', label: 'Baixo' },
];

export const ItemFormPage: React.FC = () => {
  document.title = 'Item - Austral Estoque';

  const params = useParams<{ id: string }>();
  const id = params.id;
  const navigate = useNavigate();
  const isEditing = !!id;

  const { data: item, isLoading: itemLoading } = useItem(id || '');
  const createMutation = useCreateItem();
  const updateMutation = useUpdateItem();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ItemFormData>({
    resolver: zodResolver(itemSchema),
    defaultValues: {
      active: true,
    },
  });

  useEffect(() => {
    if (item && isEditing) {
      reset({
        code: item.code,
        description: item.description,
        brand: item.brand,
        unitOfMeasure: item.unitOfMeasure,
        criticality: item.criticality,
        minStock: item.minStock,
        maxStock: item.maxStock,
        active: item.active,
      });
    }
  }, [item, isEditing, reset]);

  const onSubmit = async (data: ItemFormData) => {
    try {
      if (isEditing && id) {
        await updateMutation.mutateAsync({ id, data });
        toast.success('Item atualizado com sucesso!');
      } else {
        await createMutation.mutateAsync(data);
        toast.success('Item criado com sucesso!');
      }
      navigate('/items');
    } catch (error) {
      toast.error('Erro ao salvar item');
    }
  };

  if (itemLoading) {
    return (
      <AppLayout>
        <div className="flex justify-center items-center h-64">
          <div className="text-text">Carregando...</div>
        </div>
      </AppLayout>
    );
  }

  return (
    <AppLayout>
      <div className="space-y-4">
        <h1 className="text-2xl font-bold text-text">
          {isEditing ? 'Editar Item' : 'Novo Item'}
        </h1>

        <form onSubmit={handleSubmit(onSubmit)} className="max-w-2xl space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input
              label="Código"
              {...register('code')}
              error={errors.code?.message}
            />

            <Input
              label="Marca"
              {...register('brand')}
              error={errors.brand?.message}
            />
          </div>

          <Input
            label="Descrição"
            {...register('description')}
            error={errors.description?.message}
          />

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Input
              label="Unidade de Medida"
              {...register('unitOfMeasure')}
              error={errors.unitOfMeasure?.message}
            />

            <Select
              label="Criticidade"
              options={criticalityOptions}
              {...register('criticality')}
              error={errors.criticality?.message}
            />

            <Input
              label="Estoque Mínimo"
              type="number"
              {...register('minStock', { valueAsNumber: true })}
              error={errors.minStock?.message}
            />
          </div>

          <Input
            label="Estoque Máximo"
            type="number"
            {...register('maxStock', { valueAsNumber: true })}
            error={errors.maxStock?.message}
          />

          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              {...register('active')}
              id="active"
            />
            <label htmlFor="active" className="text-text">
              Ativo
            </label>
          </div>

          <div className="flex gap-4">
            <Button type="submit" isLoading={createMutation.isPending || updateMutation.isPending}>
              Salvar
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={() => navigate('/items')}
            >
              Cancelar
            </Button>
          </div>
        </form>
      </div>
    </AppLayout>
  );
};