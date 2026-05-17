export interface Supplier {
  id: string;
  name: string;
  country: string;
  currency: string;
  paymentTermDays?: number;
  contactName?: string;
  contactEmail?: string;
  contactPhone?: string;
  active: boolean;
}

export interface SupplierPage {
  content: Supplier[];
  totalElements: number;
  totalPages: number;
  number: number;
}

export interface SupplierFilters {
  search?: string;
  active?: boolean;
  page?: number;
  size?: number;
}