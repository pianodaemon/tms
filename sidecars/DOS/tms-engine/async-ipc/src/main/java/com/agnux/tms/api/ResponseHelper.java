package com.agnux.tms.api;

import com.agnux.tms.errors.TmsException;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

class ResponseHelper {

    public static Mono<ServerResponse> success(Object body) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
    }

    public static Mono<ServerResponse> successWithBody(Object body) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
    }

    public static Mono<ServerResponse> notFound(String context, TmsException e) {
        Map<String, Object> error = Map.of(
                "message", context + ": " + e.getMessage(),
                "errorCode", e.getErrorCode()
        );
        return ServerResponse.status(404)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(error);
    }

    public static Mono<ServerResponse> badRequest(String context, TmsException e) {
        Map<String, Object> error = Map.of(
                "message", context + ": " + e.getMessage(),
                "errorCode", e.getErrorCode()
        );
        return ServerResponse.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(error);
    }

    public static Mono<ServerResponse> internalServerError(String context, TmsException e) {
        Map<String, Object> error = Map.of(
                "message", context + ": " + e.getMessage(),
                "errorCode", e.getErrorCode()
        );
        return ServerResponse.status(500)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(error);
    }

    public static Mono<ServerResponse> notImplemented(String context, TmsException e) {
        Map<String, Object> error = Map.of(
                "message", context + ": " + e.getMessage(),
                "errorCode", e.getErrorCode()
        );
        return ServerResponse.status(501)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(error);
    }
}
