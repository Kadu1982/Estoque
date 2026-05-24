import { apiClient } from './client';
import { StockAlert, StockBalance } from '../types/stock.types';

export const stockApi = {
  list: async (params?: { lowStock?: boolean }): Promise<StockBalance[]> => {
    const response = await apiClient.get<StockBalance[]>('/api/v1/stock', { params });
    return response.data;
  },

  getLowStock: async (): Promise<StockBalance[]> => {
    const response = await apiClient.get<StockBalance[]>('/api/v1/stock/low-stock');
    return response.data;
  },

  getAlerts: async (params?: { status?: 'GREEN' | 'YELLOW' | 'RED' }): Promise<StockAlert[]> => {
    const response = await apiClient.get<StockAlert[]>('/api/v1/stock-alerts', { params });
    return response.data;
  },
};
