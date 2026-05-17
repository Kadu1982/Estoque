import React, { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { AppLayout } from '../../components/layout/AppLayout';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useSupplier, useCreateSupplier, useUpdateSupplier } from '../../hooks/useSuppliers';
import { toast } from 'react-hot-toast';

const supplierSchema = z.object({
  name: z.string().min(2, 'Mínimo 2 caracteres'),
  country: z.string().min(2, 'Mínimo 2 caracteres'),
  currency: z.string().min(3, 'Código de moeda de 3 caracteres').max(3),
  paymentTermDays: z.number().int().min(0).optional(),
  contactName: z.string().optional(),
  contactEmail: z.string().email('E-mail inválido').optional().or(z.literal('')),
  contactPhone: z.string().optional(),
  active: z.boolean().default(true),
});

type SupplierFormData = z.infer<typeof supplierSchema>;

export const SupplierFormPage: React.FC = () => {
  document.title = 'Fornecedor - Austral Estoque';

  const params = useParams<{ id: string }>();
  const id = params.id;
  const navigate = useNavigate();
  const isEditing = !!id;

  const { data: supplier, isLoading: supplierLoading } = useSupplier(id || '');
  const createMutation = useCreateSupplier();
  const updateMutation = useUpdateSupplier();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<SupplierFormData>({
    resolver: zodResolver(supplierSchema),
    defaultValues: {
      active: true,
    },
  });

  useEffect(() => {
    if (supplier && isEditing) {
      reset({
        name: supplier.name,
        country: supplier.country,
        currency: supplier.currency,
        paymentTermDays: supplier.paymentTermDays,
        contactName: supplier.contactName,
        contactEmail: supplier.contactEmail,
        contactPhone: supplier.contactPhone,
        active: supplier.active,
      });
    }
  }, [supplier, isEditing, reset]);

  const onSubmit = async (data: SupplierFormData) => {
    try {
      if (isEditing && id) {
        await updateMutation.mutateAsync({ id, data });
        toast.success('Fornecedor atualizado com sucesso!');
      } else {
        await createMutation.mutateAsync(data);
        toast.success('Fornecedor criado com sucesso!');
      }
      navigate('/suppliers');
    } catch (error) {
      toast.error('Erro ao salvar fornecedor');
    }
  };

  if (supplierLoading) {
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
          {isEditing ? 'Editar Fornecedor' : 'Novo Fornecedor'}
        </h1>

        <form onSubmit={handleSubmit(onSubmit)} className="max-w-2xl space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input
              label="Nome"
              {...register('name')}
              error={errors.name?.message}
            />

            <Input
              label="País"
              {...register('country')}
              error={errors.country?.message}
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input
              label="Moeda (3 caracteres)"
              maxLength={3}
              {...register('currency')}
              error={errors.currency?.message}
            />

            <Input
              label="Prazo de pagamento (dias)"
              type="number"
              {...register('paymentTermDays', { valueAsNumber: true })}
              error={errors.paymentTermDays?.message}
            />
          </div>

          <Input
            label="Nome do contato"
            {...register('contactName')}
            error={errors.contactName?.message}
          />

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input
              label="E-mail do contato"
              type="email"
              {...register('contactEmail')}
              error={errors.contactEmail?.message}
            />

            <Input
              label="Telefone do contato"
              {...register('contactPhone')}
              error={errors.contactPhone?.message}
            />
          </div>

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
              onClick={() => navigate('/suppliers')}
            >
              Cancelar
            </Button>
          </div>
        </form>
      </div>
    </AppLayout>
  );
};