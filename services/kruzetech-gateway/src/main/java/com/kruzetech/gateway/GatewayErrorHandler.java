package com.kruzetech.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

/** Lỗi do gateway (service sập, timeout, không có route) cũng trả envelope {error, code, message, data, traceId}. */
@Component
@Order(-2)
public class GatewayErrorHandler implements WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GatewayErrorHandler.class);

    private final ObjectMapper objectMapper;

    public GatewayErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }
        HttpStatusCode status;
        String message;
        if (hasCause(ex, ConnectException.class)) {
            status = HttpStatus.BAD_GATEWAY;
            message = "Service unavailable";
        } else if (hasCause(ex, TimeoutException.class)) {
            status = HttpStatus.GATEWAY_TIMEOUT;
            message = "Service timed out";
        } else if (ex instanceof ResponseStatusException rse) {
            status = rse.getStatusCode();
            message = status.value() == 404 ? "Cannot " + exchange.getRequest().getMethod() + " "
                    + exchange.getRequest().getPath() : String.valueOf(rse.getReason() != null ? rse.getReason() : status);
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "Internal server error";
        }
        log.warn("{} {} -> {} ({})", exchange.getRequest().getMethod(), exchange.getRequest().getPath(), status.value(), ex.toString());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", true);
        body.put("code", status.value());
        body.put("message", message);
        body.put("data", null);
        body.put("traceId", exchange.getAttributeOrDefault(TraceIdFilter.ATTR, null));

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            DataBuffer buf = exchange.getResponse().bufferFactory().wrap(objectMapper.writeValueAsBytes(body));
            return exchange.getResponse().writeWith(Mono.just(buf));
        } catch (Exception e) {
            DataBuffer buf = exchange.getResponse().bufferFactory().wrap("{\"error\":true}".getBytes(StandardCharsets.UTF_8));
            return exchange.getResponse().writeWith(Mono.just(buf));
        }
    }

    private static boolean hasCause(Throwable ex, Class<? extends Throwable> type) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (type.isInstance(t)) {
                return true;
            }
        }
        return false;
    }
}
