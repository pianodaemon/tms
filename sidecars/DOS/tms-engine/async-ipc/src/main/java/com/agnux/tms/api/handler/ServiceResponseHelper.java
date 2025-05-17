package com.agnux.tms.api.handler;

import com.agnux.tms.errors.TmsException;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

class ServiceResponseHelper {

    public static Mono<ServerResponse> success() {
        return successWithBody(Map.of("message", "success"));
    }

    public static Mono<ServerResponse> successWithBody(Object body) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
    }

    public static Mono<ServerResponse> badRequest(String context, TmsException e) {
        return errorResponse(400, context, e);
    }

    public static Mono<ServerResponse> notFound(String context, TmsException e) {
        return errorResponse(404, context, e);
    }

    public static Mono<ServerResponse> internalServerError(TmsException e) {
        return errorResponse(500, "Server Error", e);
    }

    public static Mono<ServerResponse> notImplemented(String context, TmsException e) {
        return errorResponse(501, context, e);
    }

    private static Mono<ServerResponse> errorResponse(int status, String context, TmsException e) {
        Map<String, Object> error = Map.of(
                "message", context + ": " + e.getMessage(),
                "errorCode", e.getErrorCode()
        );
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(error);
    }
}
