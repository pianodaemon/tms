import React, { useEffect, useState } from 'react';
import { AdmApi } from '../../ipc/AdmApi';
import PageTracker from './PageTracker';
import { fetchDtos } from './bricks';

type Column<T> = {
  header: string;
  render: (item: T) => React.ReactNode;
};

type PaginatedGridProps<T extends { id: string | null }> = {
  title?: string;
  api: AdmApi<T>;
  columns: Column<T>[];
  pageSize?: number;
};

function PaginatedGrid<T extends { id: string | null; }>({ title, api, columns, pageSize = 10 }: PaginatedGridProps<T>) {
  const [rows, setDtos] = useState<T[]>([]);
  const [totalPages, setTotalPages] = useState(1);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let pageOps = { page: currentPage, size: pageSize };
    let filters = {};
    fetchDtos(api, pageOps, filters, setDtos, setTotalPages, setLoading);
  }, [currentPage, api, pageSize]);

  const handlePageChange = (page: number) => setCurrentPage(page);

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
        currentPage={currentPage}
        displayCount={5}
        onPageChange={handlePageChange}
      />
    </div>
  );
}

export default PaginatedGrid;