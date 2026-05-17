import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { requisitionsApi } from '../api/requisitions.api';
import { Requisition, RequisitionPage } from '../types/requisition.types';

export const useRequisitions = (params?: { status?: string; page?: number; size?: number }) => {
  return useQuery<RequisitionPage>({
    queryKey: ['requisitions', params],
    queryFn: () => requisitionsApi.list(params),
  });
};

export const useRequisition = (id: string) => {
  return useQuery<Requisition>({
    queryKey: ['requisitions', id],
    queryFn: () => requisitionsApi.findById(id),
    enabled: !!id,
  });
};

export const useCreateRequisition = () => {
  const queryClient = useQueryClient();
  return useMutation<Requisition, Error, Partial<Requisition>>({
    mutationFn: requisitionsApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['requisitions'] });
    },
  });
};

export const useUpdateRequisition = () => {
  const queryClient = useQueryClient();
  return useMutation<Requisition, Error, { id: string; data: Partial<Requisition> }>({
    mutationFn: ({ id, data }) => requisitionsApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['requisitions'] });
    },
  });
};

export const useDeleteRequisition = () => {
  const queryClient = useQueryClient();
  return useMutation<void, Error, string>({
    mutationFn: requisitionsApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['requisitions'] });
    },
  });
};

export const useApproveRequisition = () => {
  const queryClient = useQueryClient();
  return useMutation<Requisition, Error, string>({
    mutationFn: requisitionsApi.approve,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['requisitions'] });
    },
  });
};

export const useRejectRequisition = () => {
  const queryClient = useQueryClient();
  return useMutation<Requisition, Error, string>({
    mutationFn: requisitionsApi.reject,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['requisitions'] });
    },
  });
};
