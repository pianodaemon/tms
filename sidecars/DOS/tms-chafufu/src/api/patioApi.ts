import axios, { AxiosInstance } from 'axios';

export interface PatioDto {
  id: string | null;
  name: string;
  latitudeLocation: number;
  longitudeLocation: number;
}

export interface PaginatedResponse<T> {
  data: T[];
  totalElements: number;
  totalPages: number;
}

export class PatioApi {
  private http: AxiosInstance;
  private basePath: string;

  constructor(private tenantId: string, private authToken: string, baseUrl: string = '/adm') {
    this.basePath = `${baseUrl}/${tenantId}/patios`;
    this.http = axios.create({
      headers: {
        Authorization: this.authToken,
        'Content-Type': 'application/json',
      },
    });
  }

  async create(patio: Omit<PatioDto, 'id'>): Promise<PatioDto> {
    const response = await this.http.post<PatioDto>(this.basePath, patio);
    return response.data;
  }

  async get(patioId: string): Promise<PatioDto> {
    const response = await this.http.get<PatioDto>(`${this.basePath}/${patioId}`);
    return response.data;
  }

  async delete(patioId: string): Promise<void> {
    await this.http.delete(`${this.basePath}/${patioId}`);
  }

  async listPaginated(pageNumber: number, pageSize: number): Promise<PaginatedResponse<PatioDto>> {
    const response = await this.http.get<PaginatedResponse<PatioDto>>(this.basePath, {
      params: {
        page_number: pageNumber,
        page_size: pageSize,
      },
    });
    return response.data;
  }
}
