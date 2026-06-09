package com.userFront.config;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestFilter implements Filter {

    private static final String ALLOWED_METHODS = "POST, PUT, GET, OPTIONS, DELETE";
    private static final String ALLOWED_HEADERS = "Authorization, Content-Type, "
            + "access-control-request-headers, access-control-request-method, accept, origin, x-requested-with";

    /**
     * Comma-separated list of allowed CORS origins, configurable via the
     * {@code app.cors.allowed-origins} property. Defaults to the common local
     * dev servers (React/Vite/Angular).
     */
    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:4200}")
    private String allowedOriginsProperty;

    private Set<String> allowedOrigins = new HashSet<>();

    @Override
    public void init(FilterConfig filterConfig) {
        for (String origin : allowedOriginsProperty.split(",")) {
            String trimmed = origin.trim();
            if (!trimmed.isEmpty()) {
                allowedOrigins.add(trimmed);
            }
        }
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        String origin = request.getHeader("Origin");
        if (origin != null && allowedOrigins.contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Vary", "Origin");
        }
        response.setHeader("Access-Control-Allow-Methods", ALLOWED_METHODS);
        response.setHeader("Access-Control-Allow-Headers", ALLOWED_HEADERS);
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        if (!(request.getMethod().equalsIgnoreCase("OPTIONS"))) {
            try {
                chain.doFilter(req, res);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    public void destroy() {}

}
