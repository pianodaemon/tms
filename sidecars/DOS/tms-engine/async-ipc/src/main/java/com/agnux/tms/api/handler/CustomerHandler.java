package com.agnux.tms.api.handler;

import com.agnux.tms.api.dto.CustomerDto;
import com.agnux.tms.api.service.CustomerService;
import com.agnux.tms.repository.model.Customer;
import java.util.UUID;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Log4j2
public class CustomerHandler extends ScaffoldHandler<Customer, CustomerDto> {

    public CustomerHandler(CustomerService service) {
        super(service, CustomerHandler::entMapper, CustomerHandler::dtoMapper, CustomerDto.class);
    }
/*
    public Mono<ServerResponse> create(ServerRequest request) {
        UUID tenantId = UUID.fromString(request.pathVariable("tenantId"));
        return mtCreate(request.bodyToMono(CustomerDto.class), tenantId, CustomerHandler::entMapper);
    }

    public Mono<ServerResponse> read(ServerRequest request) {
        UUID tenantId = UUID.fromString(request.pathVariable("tenantId"));
        UUID entityId = UUID.fromString(request.pathVariable("id"));
        return mtRead(tenantId, entityId, CustomerHandler::dtoMapper);
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        UUID tenantId = UUID.fromString(request.pathVariable("tenantId"));
        return mtUpdate(request.bodyToMono(CustomerDto.class), tenantId, CustomerHandler::entMapper);
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        UUID tenantId = UUID.fromString(request.pathVariable("tenantId"));
        UUID entityId = UUID.fromString(request.pathVariable("id"));
        return mtDelete(tenantId, entityId);
    }

    public Mono<ServerResponse> listPaginated(ServerRequest request) {
        UUID tenantId = UUID.fromString(request.pathVariable("tenantId"));
        return mtListPaginated(tenantId, request.queryParams(), CustomerHandler::dtoMapper);
    }
*/
    private static Customer entMapper(CustomerDto dto, UUID tenantId) {
        UUID id = dto.getId();
        String name = dto.getName();
        return new Customer(id, tenantId, name);
    }

    private static CustomerDto dtoMapper(Customer ent) {
        UUID id = ent.getId().orElseThrow();
        String name = ent.getName();
        CustomerDto dto = new CustomerDto();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }
}
