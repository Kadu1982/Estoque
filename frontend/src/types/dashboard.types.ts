export interface DashboardSummary {
  activeItems: number;
  nonDeliveredOrders: number;
  pendingRequisitions: number;
  activeSuppliers: number;
  orderStatusCounts: {
    status: string;
    count: number;
  }[];
  latestRequisitions: {
    id: string;
    code: string;
    urgency: string;
    status: string;
    requesterName: string;
  }[];
}
