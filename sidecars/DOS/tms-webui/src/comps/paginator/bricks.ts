export function generatePageNumbers(currentPage: number, totalPages: number, displayCount: number): number[] {
  const half = Math.floor(displayCount / 2);
  let start = Math.max(currentPage - half, 1);
  let end = Math.min(start + displayCount - 1, totalPages);
  start = Math.max(end - displayCount + 1, 1);

  return [...Array(end - start + 1)].map((_, i) => start + i);
}
