package com.example.PermutApp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ProductoImagenValidatorTest {

    @Test
    void aceptaJpgPngYWebpConFirmasValidas() {
        String jpeg = dataUrl("image/jpeg", new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF });
        String png = dataUrl("image/png", new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A });
        String webp = dataUrl("image/webp", new byte[] { 0x52, 0x49, 0x46, 0x46, 0, 0, 0, 0, 0x57, 0x45, 0x42, 0x50 });

        assertTrue(ProductoImagenValidator.validarYNormalizar(jpeg).startsWith("data:image/jpeg;base64,"));
        assertTrue(ProductoImagenValidator.validarYNormalizar(png).startsWith("data:image/png;base64,"));
        assertTrue(ProductoImagenValidator.validarYNormalizar(webp).startsWith("data:image/webp;base64,"));
    }

    @Test
    void rechazaFormatosNoPermitidos() {
        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> ProductoImagenValidator.validarYNormalizar(dataUrl("image/gif", new byte[] { 0x47, 0x49, 0x46 }))
        );

        assertEquals("Formato de imagen no permitido. Usa JPG, PNG o WebP", error.getReason());
    }

    @Test
    void rechazaCuandoLaFirmaNoCoincideConElMimeType() {
        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> ProductoImagenValidator.validarYNormalizar(dataUrl("image/png", new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }))
        );

        assertEquals("Formato de imagen no permitido. Usa JPG, PNG o WebP", error.getReason());
    }

    @Test
    void rechazaImagenesMayoresA5Mb() {
        byte[] imagen = new byte[ProductoImagenValidator.MAX_IMAGEN_BYTES + 1];
        imagen[0] = (byte) 0xFF;
        imagen[1] = (byte) 0xD8;
        imagen[2] = (byte) 0xFF;

        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> ProductoImagenValidator.validarYNormalizar(dataUrl("image/jpeg", imagen))
        );

        assertEquals("Cada foto puede pesar como maximo 5 MB", error.getReason());
    }

    private String dataUrl(String mimeType, byte[] contenido) {
        return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(contenido);
    }
}
