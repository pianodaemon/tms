import type { AdmApi } from '../../ipc/AdmApi';


export async function fetchDtos<T extends { id: string | null; }>(
  api: AdmApi<T>,
  pageOpts: Record<string, any>,
  filters: Record<string, any>,
  setData: (data: T[]) => void,
  setTotalPages: (pages: number) => void,
  setLoading?: (isLoading: boolean) => void
): Promise<void> {
  setLoading?.(true);
  try {
    const response = await api.listPaginated({
      pageOpts,
      filters,
    });
    setData(response.data);
    setTotalPages(response.totalPages);
  } catch (err) {
    console.error('fetchRows error:', err);
    setData([]);
    setTotalPages(1);
  } finally {
    setLoading?.(false);
  }
}

export function generatePageNumbers(currentPage: number, totalPages: number, displayCount: number): number[] {
  const half = Math.floor(displayCount / 2);
  let start = Math.max(currentPage - half, 1);
  let end = Math.min(start + displayCount - 1, totalPages);
  start = Math.max(end - displayCount + 1, 1);

  return [...Array(end - start + 1)].map((_, i) => start + i);
}