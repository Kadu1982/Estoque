import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { itemsApi } from '../api/items.api';
import { Item, ItemPage, ItemFilters } from '../types/item.types';

export const useItems = (params: ItemFilters = {}) => {
  return useQuery<ItemPage>({
    queryKey: ['items', params],
    queryFn: () => itemsApi.list(params),
  });
};

export const useItem = (id: string) => {
  return useQuery<Item>({
    queryKey: ['items', id],
    queryFn: () => itemsApi.findById(id),
    enabled: !!id,
  });
};

export const useCreateItem = () => {
  const queryClient = useQueryClient();
  return useMutation<Item, Error, Partial<Item>>({
    mutationFn: itemsApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['items'] });
    },
  });
};

export const useUpdateItem = () => {
  const queryClient = useQueryClient();
  return useMutation<Item, Error, { id: string; data: Partial<Item> }>({
    mutationFn: ({ id, data }) => itemsApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['items'] });
    },
  });
};

export const useDeleteItem = () => {
  const queryClient = useQueryClient();
  return useMutation<void, Error, string>({
    mutationFn: itemsApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['items'] });
    },
  });
};