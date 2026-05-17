import { apiClient } from './client';
import { LookupOption } from '../types/lookup.types';

export const lookupsApi = {
  warehouses: async (): Promise<LookupOption[]> => {
    const response = await apiClient.get<LookupOption[]>('/api/v1/lookups/warehouses');
    return response.data;
  },

  users: async (): Promise<LookupOption[]> => {
    const response = await apiClient.get<LookupOption[]>('/api/v1/lookups/users');
    return response.data;
  },
};
