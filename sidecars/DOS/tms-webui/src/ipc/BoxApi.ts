import { AdmApi } from './AdmApi';

export interface BoxDto {
  id: string | null;
  name: string;
  boxType: string;
  brand: string;
  numberOfAxis: number;
  numberSerial: string;
  numberPlate: string;
  numberPlateExpiration: string; // ISO string for Date
  boxYear: number;
  lease: boolean;
}

export class BoxApi extends AdmApi<BoxDto> {
  constructor(tenantId: string, authToken: string, baseUrl?: string) {
    super(tenantId, authToken, 'boxes', baseUrl);
  }
}
