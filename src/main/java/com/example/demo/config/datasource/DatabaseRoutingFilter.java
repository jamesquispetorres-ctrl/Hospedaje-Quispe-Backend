package com.example.demo.config.datasource;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class DatabaseRoutingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String demoHeader = request.getHeader("X-Demo-Mode");
        String demoParam = request.getParameter("demo");
        String dbTypeHeader = request.getHeader("X-Database-Type");

        boolean isDemo = "true".equalsIgnoreCase(demoHeader) 
                || "1".equals(demoHeader) 
                || "true".equalsIgnoreCase(demoParam)
                || "h2".equalsIgnoreCase(dbTypeHeader);

        if (isDemo) {
            DbContextHolder.setDatabaseType(DatabaseType.H2);
            log.info(">>> [ROUTER: H2 DEMO] [{}] {}", request.getMethod(), request.getRequestURI());
        } else {
            DbContextHolder.setDatabaseType(DatabaseType.MYSQL);
            log.info(">>> [ROUTER: MYSQL PROD] [{}] {}", request.getMethod(), request.getRequestURI());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            DbContextHolder.clear();
        }
    }
}
