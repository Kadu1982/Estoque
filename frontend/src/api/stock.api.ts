import { apiClient } from './client';
import { StockBalance, StockPage } from '../types/stock.types';

export const stockApi = {
  list: async (params?: { lowStock?: boolean }): Promise<StockBalance[]> => {
    const response = await apiClient.get<StockBalance[]>('/api/v1/stock', { params });
    return response.data;
  },

  getLowStock: async (): Promise<StockBalance[]> => {
    const response = await apiClient.get<StockBalance[]>('/api/v1/stock/low-stock');
    return response.data;
  },
};