package com.example.PermutApp.config;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ProductoRequestSizeFilter extends OncePerRequestFilter {

    static final long MAX_CREAR_PRODUCTO_REQUEST_BYTES = 36L * 1024L * 1024L;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String ruta = request.getServletPath();
        boolean esCrearProducto = request.getMethod().equalsIgnoreCase("POST")
            && (ruta.equals("/producto") || ruta.equals("/producto/"));
        return !esCrearProducto;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        long contentLength = request.getContentLengthLong();
        if (contentLength > MAX_CREAR_PRODUCTO_REQUEST_BYTES) {
            response.sendError(
                HttpStatus.CONTENT_TOO_LARGE.value(),
                "La solicitud supera el tamaño máximo permitido para las fotos"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}
