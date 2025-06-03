import { AdmApi } from './AdmApi';

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
