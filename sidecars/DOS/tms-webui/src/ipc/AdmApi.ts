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

  async update(item: T): Promise<T> {
    if (!item || !('id' in item) || !item.id) {
      throw new Error('Cannot update item: missing valid id');
    }

    const response = await this.http.put<T>(this.basePath, item);
    return response.data;
  }

  async listPaginated(params: {
    pageOpts?: Record<string, string | number>;
    filters?: Record<string, string | number>;
  }): Promise<PaginatedResponse<T>> {
    const { pageOpts = {}, filters = {} } = params;

    const queryParams: Record<string, string | number> = {};

    for (const [key, value] of Object.entries(filters)) {
      queryParams[`filter_${key}`] = value;
    }

    for (const [key, value] of Object.entries(pageOpts)) {
      queryParams[`page_${key}`] = value;
    }

    const response = await this.http.get<PaginatedResponse<T>>(this.basePath, {
      params: queryParams,
    });

    return response.data;
  }
}
