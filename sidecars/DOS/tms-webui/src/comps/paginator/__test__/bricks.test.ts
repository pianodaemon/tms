import { getPages } from '../bricks';

describe('getPages', () => {
  test('centered range within bounds', () => {
    expect(getPages(5, 10, 5)).toEqual([3, 4, 5, 6, 7]);
  });

  test('start of range (currentPage near 1)', () => {
    expect(getPages(1, 10, 5)).toEqual([1, 2, 3, 4, 5]);
  });

  test('end of range (currentPage near totalPages)', () => {
    expect(getPages(10, 10, 5)).toEqual([6, 7, 8, 9, 10]);
  });

  test('displayCount exceeds totalPages', () => {
    expect(getPages(2, 3, 10)).toEqual([1, 2, 3]);
  });

  test('single page only', () => {
    expect(getPages(1, 1, 5)).toEqual([1]);
  });

  test('displayCount is 1', () => {
    expect(getPages(3, 5, 1)).toEqual([3]);
  });

  test('odd displayCount with even totalPages', () => {
    expect(getPages(4, 8, 3)).toEqual([3, 4, 5]);
  });

  test('minimal totalPages and currentPage > 1', () => {
    expect(getPages(2, 2, 5)).toEqual([1, 2]);
  });

  test('displayCount = totalPages', () => {
    expect(getPages(3, 5, 5)).toEqual([1, 2, 3, 4, 5]);
  });
});

