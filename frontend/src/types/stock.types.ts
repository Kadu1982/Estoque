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