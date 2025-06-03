import axios from 'axios';
import { AdmApi, PaginatedResponse } from '../AdmApi';
import AxiosMockAdapter from 'axios-mock-adapter';

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
  let api: DummyApi;
  let mock: AxiosMockAdapter;

  beforeEach(() => {
    api = new DummyApi(tenantId, authToken);
    mock = new AxiosMockAdapter((api as any).http); // access protected .http
  });

  afterEach(() => {
    mock.restore();
  });

  it('should call PUT with correct url and data, and return response data', async () => {
    const dummyItem = { id: 'abc123', name: 'Updated name' };
    const expectedUrl = `/adm/${tenantId}/dummy`;

    mock.onPut(expectedUrl, dummyItem).reply(200, dummyItem);

    const result = await api.update(dummyItem);

    expect(result).toEqual(dummyItem);
  });

  it('should throw if id is missing', async () => {
    const invalidItem = { id: null, name: 'No id' };

    await expect(api.update(invalidItem as any)).rejects.toThrow(
      'Cannot update item: missing valid id'
    );
  });
});
