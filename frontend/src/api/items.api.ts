import { apiClient } from './client';
import { Item, ItemPage, ItemFilters } from '../types/item.types';

export const itemsApi = {
  list: async (params?: ItemFilters): Promise<ItemPage> => {
    const response = await apiClient.get<ItemPage>('/api/v1/items', { params });
    return response.data;
  },

  findById: async (id: string): Promise<Item> => {
    const response = await apiClient.get<Item>(`/api/v1/items/${id}`);
    return response.data;
  },

  create: async (data: Partial<Item>): Promise<Item> => {
    const response = await apiClient.post<Item>('/api/v1/items', data);
    return response.data;
  },

  update: async (id: string, data: Partial<Item>): Promise<Item> => {
    const response = await apiClient.put<Item>(`/api/v1/items/${id}`, data);
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/api/v1/items/${id}`);
  },
};