package com.agnux.tms.api.service;

import java.util.UUID;
import com.agnux.tms.errors.TmsException;

public interface GenCrudService<T> {

    UUID create(T entity) throws TmsException;

    T read(UUID id) throws TmsException;

    void update(T entity) throws TmsException;

    void delete(UUID id) throws TmsException;
}
