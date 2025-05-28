package com.agnux.tms.api.handler;

import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.PaginationSegment;

import com.agnux.tms.repository.model.TmsBasicModel;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.util.MultiValueMap;
import com.agnux.tms.api.service.CrudService;

import org.springframework.web.reactive.function.server.ServerRequest;

public abstract class ScaffoldHandler<T extends TmsBasicModel, D> extends AbstractCrudHandler<T, D> implements CrudHandler<ServerRequest, Mono<ServerResponse>> {

    protected abstract T entMapper(D dto, UUID tenantId) throws TmsException;

    protected abstract D dtoMapper(T ent);

    @SuppressWarnings("unchecked")
    public ScaffoldHandler(CrudService<T> service) {
        super(service);
    }

    @Override
    public Mono<ServerResponse> create(ServerRequest request) {
        UUID tenantId = UUID.fromString(request.pathVariable("tenantId"));
        return mtCreate(request.bodyToMono(dtoClazz), tenantId);
    }

    @Override
    public Mono<ServerResponse> read(ServerRequest request) {
        UUID tenantId = UUID.fromString(request.pathVariable("tenantId"));
        UUID entityId = UUID.fromString(request.pathVariable("id"));
        return mtRead(tenantId, entityId);
    }

    @Override
    public Mono<ServerResponse> update(ServerRequest request) {
        UUID tenantId = UUID.fromString(request.pathVariable("tenantId"));
        return mtUpdate(request.bodyToMono(dtoClazz), tenantId);
    }

    @Override
    public Mono<ServerResponse> delete(ServerRequest request) {
        UUID tenantId = UUID.fromString(request.pathVariable("tenantId"));
        UUID entityId = UUID.fromString(request.pathVariable("id"));
        return mtDelete(tenantId, entityId);
    }

    @Override
    public Mono<ServerResponse> listPaginated(ServerRequest request) {
        UUID tenantId = UUID.fromString(request.pathVariable("tenantId"));
        return mtListPaginated(tenantId, request.queryParams());
    }

    protected Mono<ServerResponse> mtCreate(Mono<D> dtoMono, UUID tenantId) {
        return dtoMono.flatMap(dto -> {
            try {
                T entity = entMapper(dto, tenantId);
                UUID newId = service.create(entity);
                entity.setId(newId);
                return ServiceResponseHelper.successWithBody(dtoMapper(entity));
            } catch (TmsException e) {
                return onCreateiFailure(e);
            }
        });
    }

    protected Mono<ServerResponse> mtRead(UUID tenantId, UUID entityId) {
        try {
            T entity = service.read(entityId);
            D dto = dtoMapper(entity);
            return ServiceResponseHelper.successWithBody(dto);
        } catch (TmsException e) {
            return onReadFailure(e);
        }
    }

    protected Mono<ServerResponse> mtUpdate(Mono<D> dtoMono, UUID tenantId) {
        return dtoMono.flatMap(dto -> {
            try {
                T entity = entMapper(dto, tenantId);
                service.update(entity);
                return ServiceResponseHelper.successWithBody(entity);
            } catch (TmsException e) {
                return onUpdateFailure(e);
            }
        });
    }

    protected Mono<ServerResponse> mtDelete(UUID tenantId, UUID entityId) {
        try {
            service.delete(entityId);
            return ServerResponse.noContent().build();
        } catch (TmsException e) {
            return onDeleteFailure(e);
        }
    }

    protected Mono<ServerResponse> mtListPaginated(UUID tenantId, MultiValueMap<String, String> queryParams) {
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
            PaginationSegment<T> segmentEnt = service.listPage(tenantId, searchParams, pageParams);
            List<D> dtos = segmentEnt.getData().stream().map(this::dtoMapper).collect(Collectors.toList());
            PaginationSegment<D> segmentDto = new PaginationSegment(dtos, segmentEnt.getTotalElements(), segmentEnt.getTotalPages());
            return ServiceResponseHelper.successWithBody(segmentDto);
        } catch (TmsException e) {
            return this.onPaginationFailure(e);
        }
    }

    protected Mono<ServerResponse> onPaginationFailure(TmsException e) {
        if (e.getErrorCode() == ErrorCodes.INVALID_DATA.getCode()) {
            return ServiceResponseHelper.badRequest("invalid request data", e);
        }

        if (e.getErrorCode() == ErrorCodes.REPO_PROVIDER_NONPRESENT_DATA.getCode()) {
            return ServiceResponseHelper.notFound("non-present data", e);
        }
        return ServiceResponseHelper.internalServerError(e);
    }

    protected Mono<ServerResponse> onDeleteFailure(TmsException e) {
        if (ErrorCodes.INVALID_DATA.getCode() == e.getErrorCode()) {
            return ServiceResponseHelper.badRequest("invalid identifier", e);
        }
        if (e.getErrorCode() == ErrorCodes.REPO_PROVIDER_NONPRESENT_DATA.getCode()) {
            return ServiceResponseHelper.notFound("non-present identifier", e);
        }
        return ServiceResponseHelper.internalServerError(e);
    }

    protected Mono<ServerResponse> onUpdateFailure(TmsException e) {
        if (ErrorCodes.REPO_PROVIDER_ISSUES.getCode() == e.getErrorCode()) {
            return ServiceResponseHelper.badRequest("data supplied face issues", e);
        }
        return ServiceResponseHelper.internalServerError(e);
    }

    protected Mono<ServerResponse> onReadFailure(TmsException e) {
        if (ErrorCodes.REPO_PROVIDER_NONPRESENT_DATA.getCode() == e.getErrorCode()) {
            return ServiceResponseHelper.notFound("data is not locatable", e);
        }
        return ServiceResponseHelper.internalServerError(e);
    }

    protected Mono<ServerResponse> onCreateiFailure(TmsException e) {
        if (ErrorCodes.INVALID_DATA.getCode() == e.getErrorCode()) {
            return ServiceResponseHelper.badRequest("data supplied face issues", e);
        }
        return ServiceResponseHelper.internalServerError(e);
    }
}
