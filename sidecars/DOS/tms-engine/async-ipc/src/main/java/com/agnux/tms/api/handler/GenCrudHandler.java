package com.agnux.tms.api.handler;

import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;

import com.agnux.tms.repository.model.TmsBasicModel;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.extern.log4j.Log4j2;

@Log4j2
class GenCrudHandler<T extends TmsBasicModel> {

    protected final Class<T> clazz;
    private final GenCrudService<T> service;
    private static final ConcurrentMap<Class<?>, Type> typeCache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public GenCrudHandler(GenCrudService<T> service) {
        this.service = service;
        this.clazz = (Class<T>) extractGenericType();
    }

    private Type extractGenericType() {
        return typeCache.computeIfAbsent(getClass(), cls -> {
            Type type = cls.getGenericSuperclass();
            Class<?> current = cls;
            while (!(type instanceof ParameterizedType) && current != null) {
                current = current.getSuperclass();
                type = current.getGenericSuperclass();
            }
            if (type instanceof ParameterizedType pt) {
                return pt.getActualTypeArguments()[0];
            } else {
                throw new IllegalStateException("Could not determine generic type T.");
            }
        });
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(clazz)
                .flatMap(entity -> {
                    try {
                        UUID newId = service.create(entity);
                        entity.setId(newId);
                        return ServiceResponseHelper.successWithBody(entity);
                    } catch (TmsException e) {
                        if (ErrorCodes.REPO_PROVIDER_ISSUES.getCode() == e.getErrorCode()) {
                            return ServiceResponseHelper.badRequest("data supplied face issues", e);
                        }
                        return ServiceResponseHelper.internalServerError(e);
                    }
                });
    }

    public Mono<ServerResponse> read(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        try {
            T entity = service.read(id);
            return ServiceResponseHelper.successWithBody(entity);
        } catch (TmsException e) {
            if (ErrorCodes.REPO_PROVIDER_ISSUES.getCode() == e.getErrorCode()) {
                return ServiceResponseHelper.notFound("data is not locatable", e);
            }
            return ServiceResponseHelper.internalServerError(e);
        }
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        return request.bodyToMono(clazz)
                .flatMap(entity -> {
                    try {
                        service.update(entity);
                        return ServiceResponseHelper.successWithBody(entity);
                    } catch (TmsException e) {
                        if (ErrorCodes.REPO_PROVIDER_ISSUES.getCode() == e.getErrorCode()) {
                            return ServiceResponseHelper.badRequest("data supplied face issues", e);
                        }
                        return ServiceResponseHelper.internalServerError(e);
                    }
                });
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        try {
            service.delete(id);
            return ServerResponse.noContent().build();
        } catch (TmsException e) {
            if (ErrorCodes.REPO_PROVIDER_ISSUES.getCode() == e.getErrorCode()) {
                return ServiceResponseHelper.badRequest("data supplied face issues", e);
            }
            return ServiceResponseHelper.internalServerError(e);
        }
    }
}
