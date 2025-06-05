import React, { useEffect, useState } from 'react';

import { AdmApi } from '../../ipc/AdmApi';
import PageTracker from './PageTracker';
import { fetchDtos } from './bricks';


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
    <div>
      {title && <h2>{title}</h2>}

      {loading ? (
        <p>Loading...</p>
      ) : (
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr>
              {columns.map((col, index) => (
                <th key={index}>{col.header}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((item, i) => (
              <tr key={i}>
                {columns.map((col, j) => (
                  <td key={j}>{col.render(item)}</td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
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
