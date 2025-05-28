package com.agnux.tms.api.handler;

import com.agnux.tms.api.service.CrudService;
import com.agnux.tms.repository.model.TmsBasicModel;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

abstract class AbstractCrudHandler<T extends TmsBasicModel, D> {

    protected final CrudService<T> service;
    protected final Class<D> dtoClazz;

    protected static final ConcurrentMap<Class<?>, Type> typeCache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public AbstractCrudHandler(CrudService<T> service) {
        this.service = service;
        this.dtoClazz = (Class<D>) extractGenericType();
    }

    private Type extractGenericType() {
        return typeCache.computeIfAbsent(getClass(), cls -> {
            Type type = cls.getGenericSuperclass();
            Class<?> current = cls;
            while (!(type instanceof ParameterizedType pt)) {
                current = current.getSuperclass();
                type = current.getGenericSuperclass();
            }
            return pt.getActualTypeArguments()[1]; // [1] for D
        });
    }
}
