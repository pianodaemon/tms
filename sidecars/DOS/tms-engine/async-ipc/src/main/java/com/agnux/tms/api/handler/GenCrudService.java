package com.agnux.tms.api.handler;

import java.util.UUID;
import com.agnux.tms.errors.TmsException;

public interface GenCrudService<T> {

    UUID create(T entity) throws TmsException;

    T read(UUID id) throws TmsException;

    UUID update(T entity) throws TmsException;

    void delete(UUID id) throws TmsException;
}
