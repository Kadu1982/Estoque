export interface RequisitionItem {
  id: string;
  itemId: string;
  itemCode: string;
  itemDescription: string;
  quantity: number;
  estimatedPriceUsd: number;
  estimatedTotalUsd: number;
}

export interface Requisition {
  id: string;
  code: string;
  status: 'RASCUNHO' | 'PENDENTE_APROVACAO' | 'APROVADA' | 'REPROVADA' | 'EM_COTACAO' | 'PEDIDO_EMITIDO' | 'ENCERRADA' | 'CANCELADA';
  urgency: 'NORMAL' | 'URGENTE' | 'EMERGENCIAL';
  justification: string;
  estimatedTotalUsd: number;
  requester: {
    id: string;
    fullName: string;
  };
  items: RequisitionItem[];
}

export interface RequisitionPage {
  content: Requisition[];
  totalElements: number;
  totalPages: number;
  number: number;
}