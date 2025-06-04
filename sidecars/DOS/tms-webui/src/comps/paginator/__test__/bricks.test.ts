import { test, describe, it, vi, beforeEach, expect } from 'vitest';
import { generatePageNumbers, fetchDtos } from '../bricks';
import { AdmApi } from '../../../ipc/AdmApi';

describe('generatePageNumbers', () => {
  test('centered range within bounds', () => {
    expect(generatePageNumbers(5, 10, 5)).toEqual([3, 4, 5, 6, 7]);
  });

  test('start of range (currentPage near 1)', () => {
    expect(generatePageNumbers(1, 10, 5)).toEqual([1, 2, 3, 4, 5]);
  });

  test('end of range (currentPage near totalPages)', () => {
    expect(generatePageNumbers(10, 10, 5)).toEqual([6, 7, 8, 9, 10]);
  });

  test('displayCount exceeds totalPages', () => {
    expect(generatePageNumbers(2, 3, 10)).toEqual([1, 2, 3]);
  });

  test('single page only', () => {
    expect(generatePageNumbers(1, 1, 5)).toEqual([1]);
  });

  test('displayCount is 1', () => {
    expect(generatePageNumbers(3, 5, 1)).toEqual([3]);
  });

  test('odd displayCount with even totalPages', () => {
    expect(generatePageNumbers(4, 8, 3)).toEqual([3, 4, 5]);
  });

  test('minimal totalPages and currentPage > 1', () => {
    expect(generatePageNumbers(2, 2, 5)).toEqual([1, 2]);
  });

  test('displayCount = totalPages', () => {
    expect(generatePageNumbers(3, 5, 5)).toEqual([1, 2, 3, 4, 5]);
  });
});

interface DummyDto {
  id: string | null;
  name: string;
}

class DummyApi extends AdmApi<DummyDto> {
  constructor(tenantId: string, authToken: string) {
    super(tenantId, authToken, 'dummy');
  }
}

describe('fetchDtos', () => {
  let mockApi: DummyApi;
  const dummyData: DummyDto[] = [{ id: 'abc123', name: 'Test Dummy' }];

  const setData = vi.fn();
  const setTotalPages = vi.fn();
  const setLoading = vi.fn();

  const pageOpts = { number: 1, size: 10 };
  const filters = { status: 'active' };

  beforeEach(() => {
    vi.clearAllMocks();
    mockApi = {
      listPaginated: vi.fn().mockResolvedValue({
        data: dummyData,
        totalPages: 2,
      }),
    } as unknown as AdmApi<DummyDto>;
  });

  it('calls listPaginated with pageOpts and filters', async () => {
    await fetchDtos(mockApi, pageOpts, filters, setData, setTotalPages);

    expect(mockApi.listPaginated).toHaveBeenCalledWith({
      pageOpts,
      filters,
    });
    expect(setData).toHaveBeenCalledWith(dummyData);
    expect(setTotalPages).toHaveBeenCalledWith(2);
  });

  it('handles errors and sets defaults', async () => {
    (mockApi.listPaginated as any).mockRejectedValue(new Error('Failure'));

    await fetchDtos(mockApi, pageOpts, filters, setData, setTotalPages);

    expect(setData).toHaveBeenCalledWith([]);
    expect(setTotalPages).toHaveBeenCalledWith(1);
  });

  it('uses setLoading if provided', async () => {
    await fetchDtos(mockApi, pageOpts, filters, setData, setTotalPages, setLoading);

    expect(setLoading).toHaveBeenNthCalledWith(1, true);
    expect(setLoading).toHaveBeenLastCalledWith(false);
  });

  it('does not fail if setLoading is not provided', async () => {
    await expect(fetchDtos(mockApi, pageOpts, filters, setData, setTotalPages)).resolves.not.toThrow();
  });

  it('calls setLoading on error if provided', async () => {
    (mockApi.listPaginated as any).mockRejectedValue(new Error('Failure'));
    await fetchDtos(mockApi, pageOpts, filters, setData, setTotalPages, setLoading);

    expect(setLoading).toHaveBeenNthCalledWith(1, true);
    expect(setLoading).toHaveBeenLastCalledWith(false);
  });
});
