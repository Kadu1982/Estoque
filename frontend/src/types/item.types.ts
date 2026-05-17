export interface Category {
  id: string;
  name: string;
}

export interface Item {
  id: string;
  code: string;
  description: string;
  brand: string;
  unitOfMeasure: string;
  criticality: 'CRITICO' | 'ALTO' | 'MEDIO' | 'BAIXO';
  minStock: number;
  maxStock: number;
  active: boolean;
  category: Category;
}

export interface ItemPage {
  content: Item[];
  totalElements: number;
  totalPages: number;
  number: number;
}

export interface ItemFilters {
  search?: string;
  categoryId?: string;
  active?: boolean;
  page?: number;
  size?: number;
  sort?: string;
}