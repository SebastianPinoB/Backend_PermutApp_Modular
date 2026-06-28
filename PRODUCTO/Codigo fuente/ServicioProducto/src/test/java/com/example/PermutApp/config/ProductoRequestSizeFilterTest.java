package com.example.PermutApp.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ProductoRequestSizeFilterTest {

    private final ProductoRequestSizeFilter filter = new ProductoRequestSizeFilter();

    @Test
    void rechazaCrearProductoCuandoLaPeticionSuperaElLimiteTotal() throws Exception {
        MockHttpServletRequest request = crearRequest(ProductoRequestSizeFilter.MAX_CREAR_PRODUCTO_REQUEST_BYTES + 1);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(413, response.getStatus());
    }

    @Test
    void permiteCrearProductoCuandoLaPeticionEstaDentroDelLimite() throws Exception {
        MockHttpServletRequest request = crearRequest(1024);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertNotNull(chain.getRequest());
    }

    private MockHttpServletRequest crearRequest(long contentLength) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/producto") {
            @Override
            public long getContentLengthLong() {
                return contentLength;
            }
        };
        request.setServletPath("/producto");
        return request;
    }
}
