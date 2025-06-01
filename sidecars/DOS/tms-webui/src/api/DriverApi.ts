import { AdmApi } from './AdmApi';
import { PaginatedResponse } from './types';

export interface DriverDto {
  id: string | null;
  name: string;
  firstSurname: string;
  secondSurname: string;
  licenseNumber: string;
}

export class DriverApi extends AdmApi<DriverDto> {
  constructor(tenantId: string, authToken: string, baseUrl?: string) {
    super(tenantId, authToken, 'drivers', baseUrl);
  }
}
