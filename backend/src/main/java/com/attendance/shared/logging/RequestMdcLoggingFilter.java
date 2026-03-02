package com.attendance.shared.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestMdcLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String query = request.getQueryString();
        String path = request.getRequestURI() + (query != null ? "?" + query : "");

        MDC.put("httpMethod", request.getMethod());
        MDC.put("requestPath", path);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("httpMethod");
            MDC.remove("requestPath");
        }
    }
}
