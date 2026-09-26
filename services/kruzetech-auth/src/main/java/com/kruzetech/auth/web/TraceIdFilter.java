package com.kruzetech.auth.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Gán traceId cho mỗi request: đưa vào MDC, request attribute và header X-Trace-Id. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String ATTR = "traceId";
    public static final String HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String traceId = incoming(req);
        req.setAttribute(ATTR, traceId);
        res.setHeader(HEADER, traceId);
        MDC.put(ATTR, traceId);
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.remove(ATTR);
        }
    }

    /** Dùng lại X-Trace-Id do gateway gán (nếu hợp lệ) để một request có cùng traceId qua các service. */
    private static String incoming(HttpServletRequest req) {
        String v = req.getHeader(HEADER);
        return v != null && v.matches("[A-Za-z0-9-]{1,64}") ? v : UUID.randomUUID().toString();
    }

    public static String current(HttpServletRequest req) {
        Object v = req.getAttribute(ATTR);
        return v != null ? v.toString() : UUID.randomUUID().toString();
    }
}
