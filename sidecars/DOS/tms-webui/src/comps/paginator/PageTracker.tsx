import React, { useEffect } from 'react';
import { generatePageNumbers } from './bricks';

interface PageTrackerProps {
  totalPages: number;
  currentPage: number;
  displayCount: number;
  onPageChange: (page: number) => void;
}

const PageTracker: React.FC<PageTrackerProps> = ({
  totalPages,
  currentPage,
  displayCount,
  onPageChange,
}) => {

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, [currentPage]);

  const handleClick = (page: number) => {
    if (page !== currentPage) {
      onPageChange(page);
    }
  };

  if (totalPages <= 1) return null;

  const pages = generatePageNumbers(currentPage, totalPages, displayCount);

  return (
    <div className="page-tracker flex gap-2 items-center mt-4">
      {pages.map((page) => (
        <button
          key={page}
          onClick={() => handleClick(page)}
          className={`px-3 py-1 rounded ${
            page === currentPage
              ? 'bg-gray-800 text-white font-bold'
              : 'bg-gray-200 text-gray-700'
          } hover:bg-gray-400 transition`}
        >
          {page}
        </button>
      ))}
    </div>
  );
};

export default PageTracker;
