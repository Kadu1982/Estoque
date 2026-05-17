import { apiClient } from './client';
import { DashboardSummary } from '../types/dashboard.types';

export const dashboardApi = {
  summary: async (): Promise<DashboardSummary> => {
    const response = await apiClient.get<DashboardSummary>('/api/v1/dashboard/summary');
    return response.data;
  },
};
