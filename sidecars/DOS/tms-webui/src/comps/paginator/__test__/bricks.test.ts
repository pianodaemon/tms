import { describe, expect, test } from 'vitest';
import { generatePageNumbers } from '../bricks';

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

