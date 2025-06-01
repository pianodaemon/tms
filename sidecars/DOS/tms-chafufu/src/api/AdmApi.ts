import axios, { AxiosInstance } from 'axios';

export interface PaginatedResponse<T> {
  data: T[];
  totalElements: number;
  totalPages: number;
}

export class AdmApi<T, CreateDto = Omit<T, 'id'>> {
  protected http: AxiosInstance;
  protected basePath: string;

  constructor(
    tenantId: string,
    authToken: string,
    resourcePath: string,
    baseUrl: string = '/adm'
  ) {
    this.basePath = `${baseUrl}/${tenantId}/${resourcePath}`;
    this.http = axios.create({
      headers: {
        Authorization: authToken,
        'Content-Type': 'application/json',
      },
    });
  }

  async create(item: CreateDto): Promise<T> {
    const response = await this.http.post<T>(this.basePath, item);
    return response.data;
  }

  async get(id: string): Promise<T> {
    const response = await this.http.get<T>(`${this.basePath}/${id}`);
    return response.data;
  }

  async delete(id: string): Promise<void> {
    await this.http.delete(`${this.basePath}/${id}`);
  }

  async listPaginated(pageNumber: number, pageSize: number): Promise<PaginatedResponse<T>> {
    const response = await this.http.get<PaginatedResponse<T>>(this.basePath, {
      params: {
        page_number: pageNumber,
        page_size: pageSize,
      },
    });
    return response.data;
  }
}
