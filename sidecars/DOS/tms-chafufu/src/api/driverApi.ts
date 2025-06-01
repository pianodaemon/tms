import axios, { AxiosInstance } from 'axios';

export interface DriverDto {
  id: string | null;
  name: string;
  firstSurname: string;
  secondSurname: string;
  licenseNumber: string;
}

export interface PaginatedResponse<T> {
  data: T[];
  totalElements: number;
  totalPages: number;
}

export class DriverApi {
  private http: AxiosInstance;
  private tenantId: string;
  private basePath: string;

  constructor(tenantId: string, authToken: string, baseUrl: string = '/adm') {
    this.tenantId = tenantId;
    this.basePath = `${baseUrl}/${tenantId}/drivers`;
    this.http = axios.create({
      headers: {
        Authorization: authToken,
        'Content-Type': 'application/json',
      },
    });
  }

  async create(driver: Omit<DriverDto, 'id'>): Promise<DriverDto> {
    const response = await this.http.post<DriverDto>(this.basePath, driver);
    return response.data;
  }

  async get(driverId: string): Promise<DriverDto> {
    const response = await this.http.get<DriverDto>(`${this.basePath}/${driverId}`);
    return response.data;
  }

  async delete(driverId: string): Promise<void> {
    await this.http.delete(`${this.basePath}/${driverId}`);
  }

  async listPaginated(pageNumber: number, pageSize: number): Promise<PaginatedResponse<DriverDto>> {
    const response = await this.http.get<PaginatedResponse<DriverDto>>(this.basePath, {
      params: {
        page_number: pageNumber,
        page_size: pageSize,
      },
    });
    return response.data;
  }
}
