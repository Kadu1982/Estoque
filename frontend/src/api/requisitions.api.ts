import { apiClient } from './client';
import { Requisition, RequisitionPage } from '../types/requisition.types';

export const requisitionsApi = {
  list: async (params?: { status?: string; page?: number; size?: number }): Promise<RequisitionPage> => {
    const response = await apiClient.get<RequisitionPage>('/api/v1/requisitions', { params });
    return response.data;
  },

  findById: async (id: string): Promise<Requisition> => {
    const response = await apiClient.get<Requisition>(`/api/v1/requisitions/${id}`);
    return response.data;
  },

  create: async (data: Partial<Requisition>): Promise<Requisition> => {
    const response = await apiClient.post<Requisition>('/api/v1/requisitions', data);
    return response.data;
  },

  update: async (id: string, data: Partial<Requisition>): Promise<Requisition> => {
    const response = await apiClient.put<Requisition>(`/api/v1/requisitions/${id}`, data);
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/api/v1/requisitions/${id}`);
  },

  approve: async (id: string): Promise<Requisition> => {
    const response = await apiClient.patch<Requisition>(`/api/v1/requisitions/${id}/approve`);
    return response.data;
  },

  reject: async (id: string): Promise<Requisition> => {
    const response = await apiClient.patch<Requisition>(`/api/v1/requisitions/${id}/reject`);
    return response.data;
  },
};
