import PaginatedGrid from '../comps/paginator/PaginatedGrid';
import type { BoxDto } from '../ipc/BoxApi';
import { AdmApi } from '../ipc/AdmApi';

const tenantId = 'yourTenantId';
const authToken = 'Bearer yourAuthToken';
const boxApi = new AdmApi<BoxDto>(tenantId, authToken, 'boxes');

const BoxesGrid = () => {
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

  return <PaginatedGrid title="Boxes" api={boxApi} columns={columns} />;
};

export default BoxesGrid;
