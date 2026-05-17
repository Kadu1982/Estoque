import React from 'react';
import { AppLayout } from '../../components/layout/AppLayout';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { Select } from '../../components/ui/Select';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useCreateRequisition } from '../../hooks/useRequisitions';
import { toast } from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';

const requisitionSchema = z.object({
  urgency: z.enum(['NORMAL', 'URGENTE', 'EMERGENCIAL']),
  justification: z.string().min(10, 'Mínimo 10 caracteres'),
});

type RequisitionFormData = z.infer<typeof requisitionSchema>;

const urgencyOptions = [
  { value: 'NORMAL', label: 'Normal' },
  { value: 'URGENTE', label: 'Urgente' },
  { value: 'EMERGENCIAL', label: 'Emergencial' },
];

export const RequisitionFormPage: React.FC = () => {
  document.title = 'Requisição - Austral Estoque';

  const navigate = useNavigate();
  const createMutation = useCreateRequisition();

  const { register, handleSubmit, formState: { errors } } = useForm<RequisitionFormData>({
    resolver: zodResolver(requisitionSchema),
    defaultValues: {
      urgency: 'NORMAL',
    },
  });

  const onSubmit = async (data: RequisitionFormData) => {
    try {
      await createMutation.mutateAsync(data);
      toast.success('Requisição criada com sucesso!');
      navigate('/requisitions');
    } catch (error) {
      toast.error('Erro ao criar requisição');
    }
  };

  return (
    <AppLayout>
      <div className="space-y-4">
        <h1 className="text-2xl font-bold text-text">Nova Requisição</h1>

        <form onSubmit={handleSubmit(onSubmit)} className="max-w-2xl space-y-4">
          <Select
            label="Urgência"
            options={urgencyOptions}
            {...register('urgency')}
            error={errors.urgency?.message}
          />

          <Input
            label="Justificativa"
            {...register('justification')}
            error={errors.justification?.message}
          />

          <div className="flex gap-4">
            <Button type="submit" isLoading={createMutation.isPending}>
              Salvar
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={() => navigate('/requisitions')}
            >
              Cancelar
            </Button>
          </div>
        </form>
      </div>
    </AppLayout>
  );
};