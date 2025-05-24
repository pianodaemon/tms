package com.agnux.tms.api.handler;

import com.agnux.tms.api.service.GenCrudService;
import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.PaginationSegment;

import com.agnux.tms.repository.model.TmsBasicModel;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.MultiValueMap;

@Log4j2
public class GenCrudHandler<T extends TmsBasicModel> {

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
            while (!(type instanceof ParameterizedType pt)) {
                current = current.getSuperclass();
                type = current.getGenericSuperclass();
            }
            return pt.getActualTypeArguments()[0];
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
                        if (ErrorCodes.INVALID_DATA.getCode() == e.getErrorCode()) {
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
            if (ErrorCodes.REPO_PROVIDER_NONPRESENT_DATA.getCode() == e.getErrorCode()) {
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

    public Mono<ServerResponse> listPaginated(ServerRequest request) {

        MultiValueMap<String, String> queryParams = request.queryParams();
        Map<String, String> searchParams = new HashMap<>();
        Map<String, String> pageParams = new HashMap<>();

        // Parse and split parameters
        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().get(0); // Take the first value

            if (key.startsWith("filter_")) {
                searchParams.put(key.substring(7), value);
            } else if (key.startsWith("page_")) {
                pageParams.put(key.substring(5), value);
            }
        }

        try {
            UUID tenantId = request.queryParam("tenant_id")
                    .map(UUID::fromString)
                    .orElseThrow(() -> new TmsException("missing or invalid tenant identifier", ErrorCodes.INVALID_DATA));

            PaginationSegment<T> segment = service.listPage(tenantId, searchParams, pageParams);
            return ServiceResponseHelper.successWithBody(segment);

        } catch (TmsException e) {
            if (e.getErrorCode() == ErrorCodes.INVALID_DATA.getCode()) {
                return ServiceResponseHelper.badRequest("invalid request data", e);
            }

            if (e.getErrorCode() == ErrorCodes.REPO_PROVIDER_NONPRESENT_DATA.getCode()) {
                return ServiceResponseHelper.notFound("non-present data", e);
            }
            return ServiceResponseHelper.internalServerError(e);
        }
    }

}
