export interface StockBalance {
  id: string;
  itemId: string;
  itemCode: string;
  itemDescription: string;
  warehouseId: string;
  warehouseName: string;
  quantity: number;
  minStock: number;
  maxStock: number;
  unitOfMeasure: string;
  plannedQuantity: number | null;
  percentageOfPlanned: number | null;
  statusColor: 'GREEN' | 'YELLOW' | 'RED';
}

export interface StockPage {
  content: StockBalance[];
  totalElements: number;
  totalPages: number;
  number: number;
}

export interface StockFilters {
  search?: string;
  lowStock?: boolean;
  page?: number;
  size?: number;
}

export interface StockAlert {
  id: string;
  warehouseId: string;
  warehouseName: string;
  itemId: string;
  itemCode: string;
  itemDescription: string;
  quantityOnHand: number;
  plannedQuantity: number;
  percentageOfPlanned: number;
  statusColor: 'GREEN' | 'YELLOW' | 'RED';
  alertType: string;
  alertStatus: string;
  createdAt: string;
}
