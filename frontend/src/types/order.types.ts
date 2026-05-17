export interface OrderItem {
  id?: string;
  itemId: string;
  itemCode: string;
  itemDescription: string;
  quantity: number;
  receivedQuantity?: number;
  unitPrice: number;
  total: number;
}

export interface Supplier {
  id: string;
  name: string;
}

export interface Requester {
  id: string;
  username: string;
  fullName: string;
}

export interface Order {
  id: string;
  code: string;
  status: 'ABERTO' | 'EM_PRODUCAO' | 'ENTREGUE' | 'PARCIALMENTE_RECEBIDO' | 'CANCELADO';
  notes?: string;
  supplier: Supplier;
  requester: Requester;
  items: OrderItem[];
}

export interface OrderRequestItem {
  itemId: string;
  quantity: number;
  unitPrice: number;
}

export interface OrderRequest {
  code: string;
  supplierId: string;
  requesterId?: string;
  requisitionId?: string;
  notes?: string;
  items?: OrderRequestItem[];
}

export interface OrderReceiveRequest {
  warehouseId: string;
  receiverId: string;
  items: {
    orderItemId: string;
    quantity: number;
  }[];
}
