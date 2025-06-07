import React, { useEffect, useState } from 'react';

import { AdmApi } from '../../ipc/AdmApi';
import PageTracker from './PageTracker';

type Column<T> = {
  header: string;
  render: (item: T) => React.ReactNode;
};

type PaginatedGridProps<T extends { id: string | null; }> = {
  title?: string;
  api: AdmApi<T>;
  columns: Column<T>[];
  pageOpts: { page: number; size: number; [key: string]: any };
  filters?: Record<string, any>;
};

async function fetchDtos<T extends { id: string | null; }>(
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

function PaginatedGrid<T extends { id: string | null; }>({
  title,
  api,
  columns,
  pageOpts,
  filters = {},
}: PaginatedGridProps<T>) {
  const [rows, setRows] = useState<T[]>([]);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchDtos(api, pageOpts, filters, setRows, setTotalPages, setLoading);
  }, [api, pageOpts, filters]);

  const handlePageChange = (page: number) => {
    // let parent component control page state
    if (typeof pageOpts.page === 'number') {
      pageOpts.page = page; // update mutable object
      fetchDtos(api, pageOpts, filters, setRows, setTotalPages, setLoading);
    }
  };

  return (
    <div className="p-4 space-y-4">
      {title && <h2 className="text-xl font-semibold text-gray-800">{title}</h2>}

      {loading ? (
        <div className="text-gray-500">Loading...</div>
      ) : (
        <div className="overflow-x-auto rounded-lg shadow">
          <table className="min-w-full text-sm text-left text-gray-700 bg-white border border-gray-200">
            <thead className="bg-gray-100 text-gray-600 uppercase text-xs">
              <tr>
                {columns.map((col, index) => (
                  <th key={index} className="px-4 py-3 border-b">
                    {col.header}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {rows.map((item, i) => (
                <tr key={i} className="hover:bg-gray-50">
                  {columns.map((col, j) => (
                    <td key={j} className="px-4 py-2 border-b">
                      {col.render(item)}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <PageTracker
        totalPages={totalPages}
        currentPage={pageOpts.page}
        displayCount={5}
        onPageChange={handlePageChange}
      />
    </div>
  );
}

export default PaginatedGrid;