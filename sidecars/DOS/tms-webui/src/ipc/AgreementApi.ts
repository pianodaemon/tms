import { AdmApi, PaginatedResponse } from './AdmApi';

export interface AgreementDto {
  id: string | null;
  customerId: string;
  receiver: string;
  latitudeOrigin: number;
  longitudeOrigin: number;
  latitudeDestiny: number;
  longitudeDestiny: number;
  distUnit: string;
  distScalar: string;    // BigDecimal as string to preserve precision
}

export class AgreementApi extends AdmApi<AgreementDto> {
  constructor(tenantId: string, authToken: string, baseUrl?: string) {
    super(tenantId, authToken, 'agreements', baseUrl);
  }
}
