import axios from 'axios';
import { AdmApi } from '../../src/ipc/AdmApi';

jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

interface DummyDto {
  id: string | null;
  name: string;
}

class DummyApi extends AdmApi<DummyDto> {
  constructor(tenantId: string, authToken: string) {
    super(tenantId, authToken, 'dummy');
  }
}

describe('AdmApi update method', () => {
  const tenantId = 'tenant-123';
  const authToken = 'Bearer token';
  const api = new DummyApi(tenantId, authToken);

  it('should call PUT with correct url and data, and return response data', async () => {
    const dummyItem = { id: 'abc123', name: 'Updated name' };
    const mockResponse = { data: dummyItem };

    mockedAxios.put.mockResolvedValueOnce(mockResponse);

    const result = await api.update(dummyItem);

    expect(mockedAxios.put).toHaveBeenCalledWith(
      `/adm/${tenantId}/dummy`,
      dummyItem
    );
    expect(result).toEqual(dummyItem);
  });

  it('should throw if id is missing', async () => {
    const invalidItem = { id: null, name: 'No id' };

    await expect(api.update(invalidItem as any)).rejects.toThrow(
      'Cannot update item: missing valid id'
    );
  });
});
