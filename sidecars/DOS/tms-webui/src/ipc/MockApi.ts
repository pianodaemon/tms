import type { PaginatedResponse } from './AdmApi';
import { AdmApi } from './AdmApi';

export class MockApi<T extends { id: string | null }, CreateDto = Omit<T, 'id'>> extends AdmApi<T, CreateDto> {
  private dataStore: T[];

  constructor(initialData: T[]) {
    super('mockTenant', 'mockToken', 'mockResource');
    this.dataStore = [...initialData];
  }

  override async create(item: CreateDto): Promise<T> {
    const newItem = {
      ...item,
      id: (Math.random() * 1000000).toFixed(0),
    } as unknown as T;
    this.dataStore.push(newItem);
    return Promise.resolve(newItem);
  }

  override async get(id: string): Promise<T> {
    const item = this.dataStore.find((d) => d.id === id);
    if (!item) throw new Error('Item not found');
    return Promise.resolve(item);
  }

  override async delete(id: string): Promise<void> {
    this.dataStore = this.dataStore.filter((d) => d.id !== id);
    return Promise.resolve();
  }

  override async update(item: T): Promise<T> {
    const index = this.dataStore.findIndex((d) => d.id === item.id);
    if (index === -1) throw new Error('Item not found');
    this.dataStore[index] = item;
    return Promise.resolve(item);
  }

  override async listPaginated(params: {
    pageOpts?: Record<string, string | number>;
    filters?: Record<string, string | number>;
  }): Promise<PaginatedResponse<T>> {
    const { pageOpts = {}, filters = {} } = params;
    const page = Math.max(0, Number(pageOpts.page ?? 1) - 1);
    const size = Number(pageOpts.size ?? 10);

    // Simple filtering
    let filtered = this.dataStore;
    for (const [key, value] of Object.entries(filters)) {
      filtered = filtered.filter((item) => item[key as keyof T] === value);
    }

    const totalElements = filtered.length;
    const totalPages = Math.ceil(totalElements / size);
    const start = page * size;
    const end = start + size;

    const data = filtered.slice(start, end);

    return Promise.resolve({
      data,
      totalElements,
      totalPages,
    });
  }
}