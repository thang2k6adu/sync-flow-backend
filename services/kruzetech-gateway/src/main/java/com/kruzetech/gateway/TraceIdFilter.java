package com.kruzetech.gateway;

import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/** WebFilter (chạy cả khi không khớp route nào) gán X-Trace-Id (giữ của client nếu hợp lệ), chuyển xuống service và trả lại trong response. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter implements WebFilter {

    public static final String HEADER = "X-Trace-Id";
    public static final String ATTR = "traceId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String incoming = exchange.getRequest().getHeaders().getFirst(HEADER);
        String traceId = incoming != null && incoming.matches("[A-Za-z0-9-]{1,64}") ? incoming : UUID.randomUUID().toString();
        exchange.getAttributes().put(ATTR, traceId);
        exchange.getResponse().beforeCommit(() -> {
            exchange.getResponse().getHeaders().set(HEADER, traceId);
            return Mono.empty();
        });
        return chain.filter(exchange.mutate()
                .request(r -> r.headers(h -> h.set(HEADER, traceId)))
                .build());
    }
}
