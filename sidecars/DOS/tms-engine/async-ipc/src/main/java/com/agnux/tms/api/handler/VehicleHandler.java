package com.agnux.tms.api.handler;

import com.agnux.tms.api.service.VehicleService;
import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.model.Vehicle;
import org.springframework.stereotype.Component;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Log4j2
public class VehicleHandler extends GenCrudHandler<Vehicle> {

    public VehicleHandler(VehicleService service) {
        super(service);
    }

    @Override
    public Mono<ServerResponse> onPaginationFailure(TmsException e) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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
