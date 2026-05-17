import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ordersApi } from '../api/orders.api';
import { Order, OrderReceiveRequest, OrderRequest } from '../types/order.types';

export const useOrders = () => {
  return useQuery<Order[]>({
    queryKey: ['orders'],
    queryFn: () => ordersApi.list(),
  });
};

export const useOrder = (id: string) => {
  return useQuery<Order>({
    queryKey: ['orders', id],
    queryFn: () => ordersApi.findById(id),
    enabled: !!id,
  });
};

export const useCreateOrder = () => {
  const queryClient = useQueryClient();
  return useMutation<Order, Error, OrderRequest>({
    mutationFn: ordersApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
};

export const useUpdateOrder = () => {
  const queryClient = useQueryClient();
  return useMutation<Order, Error, { id: string; data: OrderRequest }>({
    mutationFn: ({ id, data }) => ordersApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
};

export const useDeleteOrder = () => {
  const queryClient = useQueryClient();
  return useMutation<void, Error, string>({
    mutationFn: ordersApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
};

export const useReceiveOrder = () => {
  const queryClient = useQueryClient();
  return useMutation<Order, Error, { id: string; data: OrderReceiveRequest }>({
    mutationFn: ({ id, data }) => ordersApi.receive(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
};
