package com.agnux.tms.api.handler;

import com.agnux.tms.api.service.CustomerService;
import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.model.Customer;
import org.springframework.stereotype.Component;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Log4j2
public class CustomerHandler extends GenCrudHandler<Customer> {

    public CustomerHandler(CustomerService service) {
        super(service);
    }

    @Override
    public Mono<ServerResponse> onPaginationFailure(TmsException e) {
        if (e.getErrorCode() == ErrorCodes.INVALID_DATA.getCode()) {
            return ServiceResponseHelper.badRequest("invalid request data", e);
        }

        if (e.getErrorCode() == ErrorCodes.REPO_PROVIDER_NONPRESENT_DATA.getCode()) {
            return ServiceResponseHelper.notFound("non-present data", e);
        }
        return ServiceResponseHelper.internalServerError(e);
    }

    @Override
    protected Mono<ServerResponse> onDeleteFailure(TmsException e) {
        if (ErrorCodes.REPO_PROVIDER_ISSUES.getCode() == e.getErrorCode()) {
            return ServiceResponseHelper.badRequest("data supplied face issues", e);
        }
        return ServiceResponseHelper.internalServerError(e);
    }

    @Override
    protected Mono<ServerResponse> onUpdateFailure(TmsException e) {
        if (ErrorCodes.REPO_PROVIDER_ISSUES.getCode() == e.getErrorCode()) {
            return ServiceResponseHelper.badRequest("data supplied face issues", e);
        }
        return ServiceResponseHelper.internalServerError(e);
    }

    @Override
    protected Mono<ServerResponse> onReadFailure(TmsException e) {
        if (ErrorCodes.REPO_PROVIDER_NONPRESENT_DATA.getCode() == e.getErrorCode()) {
            return ServiceResponseHelper.notFound("data is not locatable", e);
        }
        return ServiceResponseHelper.internalServerError(e);
    }

    @Override
    protected Mono<ServerResponse> onCreateiFailure(TmsException e) {
        if (ErrorCodes.INVALID_DATA.getCode() == e.getErrorCode()) {
            return ServiceResponseHelper.badRequest("data supplied face issues", e);
        }
        return ServiceResponseHelper.internalServerError(e);
    }
}
