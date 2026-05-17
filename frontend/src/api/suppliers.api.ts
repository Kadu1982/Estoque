import { apiClient } from './client';
import { Supplier, SupplierPage, SupplierFilters } from '../types/supplier.types';

export const suppliersApi = {
  list: async (params?: SupplierFilters): Promise<SupplierPage> => {
    const response = await apiClient.get<SupplierPage>('/api/v1/suppliers', { params });
    return response.data;
  },

  findById: async (id: string): Promise<Supplier> => {
    const response = await apiClient.get<Supplier>(`/api/v1/suppliers/${id}`);
    return response.data;
  },

  create: async (data: Partial<Supplier>): Promise<Supplier> => {
    const response = await apiClient.post<Supplier>('/api/v1/suppliers', data);
    return response.data;
  },

  update: async (id: string, data: Partial<Supplier>): Promise<Supplier> => {
    const response = await apiClient.put<Supplier>(`/api/v1/suppliers/${id}`, data);
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/api/v1/suppliers/${id}`);
  },
};