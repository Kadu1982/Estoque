import { apiClient } from './client';
import { Order, OrderReceiveRequest, OrderRequest } from '../types/order.types';

export const ordersApi = {
  list: async (): Promise<Order[]> => {
    const response = await apiClient.get<Order[]>('/api/v1/orders');
    return response.data;
  },

  findById: async (id: string): Promise<Order> => {
    const response = await apiClient.get<Order>(`/api/v1/orders/${id}`);
    return response.data;
  },

  create: async (data: OrderRequest): Promise<Order> => {
    const response = await apiClient.post<Order>('/api/v1/orders', data);
    return response.data;
  },

  update: async (id: string, data: OrderRequest): Promise<Order> => {
    const response = await apiClient.put<Order>(`/api/v1/orders/${id}`, data);
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/api/v1/orders/${id}`);
  },

  receive: async (id: string, data: OrderReceiveRequest): Promise<Order> => {
    const response = await apiClient.post<Order>(`/api/v1/orders/${id}/receive`, data);
    return response.data;
  },
};
