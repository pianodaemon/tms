package com.agnux.tms.api.service;

import java.util.UUID;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.PaginationSegment;
import com.agnux.tms.repository.model.TmsBasicModel;
import java.util.Map;

public interface CrudService<T extends TmsBasicModel>  {

    UUID create(T entity) throws TmsException;

    T read(UUID id) throws TmsException;

    void update(T entity) throws TmsException;

    void delete(UUID id) throws TmsException;

    PaginationSegment<T> listPage(UUID tenantId, Map<String, String> filters, Map<String, String> pageParams) throws TmsException;
}
