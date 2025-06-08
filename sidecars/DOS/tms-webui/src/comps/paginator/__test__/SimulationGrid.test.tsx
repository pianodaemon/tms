import { render, screen, waitFor } from '@testing-library/react';
import SimulationGrid from '../SimulationGrid';
import { describe, expect, it, vi } from 'vitest';
import { useEffect, useState } from 'react';

// Mock PaginatedGrid to isolate test
vi.mock('../PaginatedGrid', () => ({
  default: ({ title, api, columns, pageOpts, filters }: any) => {

    const [data, setData] = useState<any[]>([]);

    useEffect(() => {
      api.listPaginated({ pageOpts, filters }).then((res: any) => {
        setData(res.data);
      });
    }, [api, pageOpts, filters]);

    return (
      <div>
        <h1>{title}</h1>
        <table>
          <thead>
            <tr>
              {columns.map((col: any, idx: number) => (
                <th key={idx}>{col.header}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {data.map((item: any) => (
              <tr key={item.id}>
                {columns.map((col: any, idx: number) => (
                  <td key={idx}>{col.render(item)}</td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  },
}));

describe('SimulationGrid', () => {
  it('renders grid with paginated data for page 1', async () => {
    render(<SimulationGrid />);

    // Wait for grid title
    await waitFor(() => {
      expect(screen.getByText('Boxes')).toBeInTheDocument();
    });

    // Page 1: Boxes 1–10
    expect(screen.getByText('Box 1')).toBeInTheDocument();
    expect(screen.getByText('Box 10')).toBeInTheDocument();

    // Page 2: should not show
    expect(screen.queryByText('Box 11')).not.toBeInTheDocument();
  });
});
