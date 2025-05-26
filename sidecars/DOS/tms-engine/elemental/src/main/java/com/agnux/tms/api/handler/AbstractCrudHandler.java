package com.agnux.tms.api.handler;

import com.agnux.tms.api.service.CrudService;
import com.agnux.tms.repository.model.TmsBasicModel;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

abstract class AbstractCrudHandler<T extends TmsBasicModel, R, W> {

    protected final Class<T> clazz;
    protected final CrudService<T> service;
    protected static final ConcurrentMap<Class<?>, Type> typeCache = new ConcurrentHashMap<>();

    public abstract R create(W request);

    public abstract R read(W request);

    public abstract R update(W request);

    public abstract R delete(W request);

    public abstract R listPaginated(W request);

    @SuppressWarnings("unchecked")
    public AbstractCrudHandler(CrudService<T> service) {
        this.service = service;
        this.clazz = (Class<T>) extractGenericType();
    }

    private Type extractGenericType() {
        return typeCache.computeIfAbsent(getClass(), cls -> {
            Type type = cls.getGenericSuperclass();
            Class<?> current = cls;
            while (!(type instanceof ParameterizedType pt)) {
                current = current.getSuperclass();
                type = current.getGenericSuperclass();
            }
            return pt.getActualTypeArguments()[0];
        });
    }
}
