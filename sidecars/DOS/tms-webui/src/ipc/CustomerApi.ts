import { AdmApi, PaginatedResponse } from './AdmApi';

export interface CustomerDto {
  id: string | null;
  name: string;
}

export class CustomerApi extends AdmApi<CustomerDto> {
  constructor(tenantId: string, authToken: string, baseUrl?: string) {
    super(tenantId, authToken, 'customers', baseUrl);
  }
}
