import React, { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { AppLayout } from '../../components/layout/AppLayout';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { Select } from '../../components/ui/Select';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useOrder, useCreateOrder, useUpdateOrder } from '../../hooks/useOrders';
import { useSuppliers } from '../../hooks/useSuppliers';
import { toast } from 'react-hot-toast';

const orderSchema = z.object({
  code: z.string().min(2, 'Codigo e obrigatorio'),
  supplierId: z.string().min(1, 'Fornecedor e obrigatorio'),
  notes: z.string().optional(),
});

type OrderFormData = z.infer<typeof orderSchema>;

export const OrderFormPage: React.FC = () => {
  document.title = 'Pedido - Austral Estoque';

  const params = useParams<{ id: string }>();
  const id = params.id;
  const navigate = useNavigate();
  const isEditing = !!id;

  const { data: order, isLoading: orderLoading } = useOrder(id || '');
  const { data: suppliers } = useSuppliers();
  const createMutation = useCreateOrder();
  const updateMutation = useUpdateOrder();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<OrderFormData>({
    resolver: zodResolver(orderSchema),
  });

  useEffect(() => {
    if (order && isEditing) {
      reset({
        code: order.code || '',
        supplierId: order.supplier?.id || '',
        notes: order.notes || '',
      });
    }
  }, [order, isEditing, reset]);

  const onSubmit = async (data: OrderFormData) => {
    try {
      if (isEditing && id) {
        await updateMutation.mutateAsync({ id, data });
        toast.success('Pedido atualizado com sucesso');
      } else {
        await createMutation.mutateAsync(data);
        toast.success('Pedido criado com sucesso');
      }
      navigate('/orders');
    } catch (error) {
      toast.error('Erro ao salvar pedido');
    }
  };

  const supplierOptions = suppliers?.content?.map((s) => ({
    value: s.id,
    label: s.name,
  })) || [];

  if (orderLoading && isEditing) {
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
          {isEditing ? 'Editar Pedido' : 'Novo Pedido'}
        </h1>

        <form onSubmit={handleSubmit(onSubmit)} className="max-w-2xl space-y-4">
          <Input
            label="Codigo"
            {...register('code')}
            error={errors.code?.message}
          />

          <Select
            label="Fornecedor"
            options={supplierOptions}
            {...register('supplierId')}
            error={errors.supplierId?.message}
          />

          <Input
            label="Observacoes"
            {...register('notes')}
            error={errors.notes?.message}
          />

          <div className="flex gap-4">
            <Button type="submit" isLoading={createMutation.isPending || updateMutation.isPending}>
              Salvar
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={() => navigate('/orders')}
            >
              Cancelar
            </Button>
          </div>
        </form>
      </div>
    </AppLayout>
  );
};
