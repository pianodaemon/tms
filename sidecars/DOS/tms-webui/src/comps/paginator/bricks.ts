export function getPages(currentPage: number, totalPages: number, displayCount: number): number[] {
  const pages: number[] = [];
  const half = Math.floor(displayCount / 2);
  let start = Math.max(currentPage - half, 1);
  let end = Math.min(start + displayCount - 1, totalPages);

  if (end - start + 1 < displayCount) {
    start = Math.max(end - displayCount + 1, 1);
  }

  for (let i = start; i <= end; i++) {
    pages.push(i);
  }

  return pages;
}
