import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { suppliersApi } from '../api/suppliers.api';
import { Supplier, SupplierPage, SupplierFilters } from '../types/supplier.types';

export const useSuppliers = (params?: SupplierFilters) => {
  return useQuery<SupplierPage>({
    queryKey: ['suppliers', params],
    queryFn: () => suppliersApi.list(params),
  });
};

export const useSupplier = (id: string) => {
  return useQuery<Supplier>({
    queryKey: ['suppliers', id],
    queryFn: () => suppliersApi.findById(id),
    enabled: !!id,
  });
};

export const useCreateSupplier = () => {
  const queryClient = useQueryClient();
  return useMutation<Supplier, Error, Partial<Supplier>>({
    mutationFn: suppliersApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['suppliers'] });
    },
  });
};

export const useUpdateSupplier = () => {
  const queryClient = useQueryClient();
  return useMutation<Supplier, Error, { id: string; data: Partial<Supplier> }>({
    mutationFn: ({ id, data }) => suppliersApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['suppliers'] });
    },
  });
};

export const useDeleteSupplier = () => {
  const queryClient = useQueryClient();
  return useMutation<void, Error, string>({
    mutationFn: suppliersApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['suppliers'] });
    },
  });
};