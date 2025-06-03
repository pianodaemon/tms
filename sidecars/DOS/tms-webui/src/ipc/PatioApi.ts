import { AdmApi, PaginatedResponse } from './AdmApi';

export interface PatioDto {
  id: string | null;
  name: string;
  latitudeLocation: number;
  longitudeLocation: number;
}

export class PatioApi extends AdmApi<PatioDto> {
  constructor(tenantId: string, authToken: string, baseUrl?: string) {
    super(tenantId, authToken, 'patios', baseUrl);
  }
}
