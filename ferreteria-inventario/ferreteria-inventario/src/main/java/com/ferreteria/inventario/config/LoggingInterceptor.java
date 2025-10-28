package com.ferreteria.inventario.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Enumeration;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.info("===== NUEVA SOLICITUD =====");
        logger.info("URL: {}", request.getRequestURL());
        logger.info("Método: {}", request.getMethod());
        logger.info("Context Path: {}", request.getContextPath());
        logger.info("Servlet Path: {}", request.getServletPath());
        logger.info("Path Info: {}", request.getPathInfo());

        logger.info("=== PARÁMETROS ===");
        request.getParameterMap().forEach((key, value) -> logger.info("{} = {}", key, String.join(",", value)));

        logger.info("=== HEADERS ===");
        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()){
            String header = headerNames.nextElement();
            logger.info("{}: {}", header, request.getHeader(header));
        }

        return true; // continuar con la solicitud
    }
}
