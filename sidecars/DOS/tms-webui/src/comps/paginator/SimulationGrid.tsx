import type { BoxDto } from '../../ipc/BoxApi';
import PaginatedGrid from './PaginatedGrid';
import { useState } from 'react';
import { MockApi } from '../../ipc/MockApi'; // path to your mock class

const initialBoxes: BoxDto[] = Array.from({ length: 45 }, (_, i) => ({
  id: `${i + 1}`,
  name: `Box ${i + 1}`,
  boxType: i % 2 === 0 ? 'Refrigerated' : 'Standard',
  brand: `Brand ${i % 5}`,
  numberOfAxis: 2 + (i % 3),
  numberSerial: `SN-${1000 + i}`,
  numberPlate: `PLATE-${i + 1}`,
  numberPlateExpiration: new Date(2026, (i % 12), 1).toISOString(),
  boxYear: 2020 + (i % 5),
  lease: i % 2 === 0,
}));

const mockBoxApi = new MockApi<BoxDto>(initialBoxes);

const SimulationGrid = () => {
  const [pageOpts] = useState({ page: 1, size: 10 });
  const [filters] = useState({});

  const columns = [
    { header: 'Name', render: (b: BoxDto) => b.name },
    { header: 'Type', render: (b: BoxDto) => b.boxType },
    { header: 'Brand', render: (b: BoxDto) => b.brand },
    { header: 'Axis', render: (b: BoxDto) => b.numberOfAxis },
    { header: 'Serial', render: (b: BoxDto) => b.numberSerial },
    { header: 'Plate', render: (b: BoxDto) => b.numberPlate },
    {
      header: 'Plate Expiration',
      render: (b: BoxDto) => new Date(b.numberPlateExpiration).toLocaleDateString(),
    },
    { header: 'Year', render: (b: BoxDto) => b.boxYear },
    { header: 'Lease', render: (b: BoxDto) => (b.lease ? 'Yes' : 'No') },
  ];

  return (
    <PaginatedGrid
      title="Boxes"
      api={mockBoxApi}
      columns={columns}
      pageOpts={pageOpts}
      filters={filters}
    />
  );
};

export default SimulationGrid;
