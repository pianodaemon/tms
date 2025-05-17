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

import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class GenCrudHandler<T extends TmsBasicModel> {

    protected final Class<T> clazz;

    @SuppressWarnings("unchecked")
    public GenCrudHandler() {
        this.clazz = (Class<T>) extractGenericType();
    }

    private Type extractGenericType() {
        Type type = getClass().getGenericSuperclass();
        Class<?> current = getClass();

        while (!(type instanceof ParameterizedType) && current != null) {
            current = current.getSuperclass();
            type = current.getGenericSuperclass();
        }

        if (type instanceof ParameterizedType parameterizedType) {
            return parameterizedType.getActualTypeArguments()[0];
        } else {
            throw new IllegalStateException("Could not determine generic type T.");
        }
    }

    protected abstract UUID createEntity(T entity) throws TmsException;

    protected abstract T getEntity(UUID id) throws TmsException;

    protected abstract UUID updateEntity(T entity) throws TmsException;

    protected abstract void deleteEntity(UUID id) throws TmsException;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(clazz)
                .flatMap(entity -> {
                    try {
                        UUID newId = createEntity(entity);
                        entity.setId(newId);
                        return ServiceResponseHelper.successWithBody(entity);
                    } catch (TmsException e) {
                        return handleError(e, "data supplied face issues");
                    }
                });
    }

    public Mono<ServerResponse> read(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        try {
            T entity = getEntity(id);
            return ServiceResponseHelper.successWithBody(entity);
        } catch (TmsException e) {
            return handleError(e, "data is not locatable");
        }
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        return request.bodyToMono(clazz)
                .flatMap(entity -> {
                    try {
                        updateEntity(entity);
                        return ServiceResponseHelper.successWithBody(entity);
                    } catch (TmsException e) {
                        return handleError(e, "data supplied face issues");
                    }
                });
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        try {
            deleteEntity(id);
            return ServerResponse.noContent().build();
        } catch (TmsException e) {
            return handleError(e, "data supplied face issues");
        }
    }

    private Mono<ServerResponse> handleError(TmsException e, String userMessage) {
        if (ErrorCodes.REPO_PROVIDER_ISSUES.getCode() == e.getErrorCode()) {
            return ServiceResponseHelper.badRequest(userMessage, e);
        }
        return ServiceResponseHelper.internalServerError(e);
    }
}
