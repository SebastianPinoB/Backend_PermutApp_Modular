package com.example.PermutApp.service;

import java.util.Base64;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class ProductoImagenValidator {

    static final int MAX_IMAGEN_BYTES = 5 * 1024 * 1024;

    private static final long MAX_BASE64_CHARACTERS = 4L * ((MAX_IMAGEN_BYTES + 2L) / 3L);
    private static final Set<String> MIME_TYPES_PERMITIDOS = Set.of("image/jpeg", "image/png", "image/webp");

    private ProductoImagenValidator() {
    }

    static String validarYNormalizar(String dataUrl) {
        int separador = dataUrl.indexOf(',');
        if (separador <= 0 || separador == dataUrl.length() - 1) {
            throw formatoNoPermitido();
        }

        String metadata = dataUrl.substring(0, separador).toLowerCase(Locale.ROOT);
        if (!metadata.startsWith("data:") || !metadata.endsWith(";base64")) {
            throw formatoNoPermitido();
        }

        String mimeType = metadata.substring("data:".length(), metadata.length() - ";base64".length());
        if (mimeType.equals("image/jpg")) {
            mimeType = "image/jpeg";
        }
        if (!MIME_TYPES_PERMITIDOS.contains(mimeType)) {
            throw formatoNoPermitido();
        }

        String contenidoBase64 = dataUrl.substring(separador + 1);
        if (contenidoBase64.length() > MAX_BASE64_CHARACTERS) {
            throw imagenMuyPesada();
        }

        byte[] contenido;
        try {
            contenido = Base64.getDecoder().decode(contenidoBase64);
        } catch (IllegalArgumentException exception) {
            throw formatoNoPermitido();
        }

        if (contenido.length > MAX_IMAGEN_BYTES) {
            throw imagenMuyPesada();
        }
        if (!firmaCoincide(contenido, mimeType)) {
            throw formatoNoPermitido();
        }

        return "data:" + mimeType + ";base64," + contenidoBase64;
    }

    private static boolean firmaCoincide(byte[] contenido, String mimeType) {
        return switch (mimeType) {
            case "image/jpeg" -> contenido.length >= 3
                && (contenido[0] & 0xFF) == 0xFF
                && (contenido[1] & 0xFF) == 0xD8
                && (contenido[2] & 0xFF) == 0xFF;
            case "image/png" -> contenido.length >= 8
                && (contenido[0] & 0xFF) == 0x89
                && contenido[1] == 0x50
                && contenido[2] == 0x4E
                && contenido[3] == 0x47
                && contenido[4] == 0x0D
                && contenido[5] == 0x0A
                && contenido[6] == 0x1A
                && contenido[7] == 0x0A;
            case "image/webp" -> contenido.length >= 12
                && contenido[0] == 0x52
                && contenido[1] == 0x49
                && contenido[2] == 0x46
                && contenido[3] == 0x46
                && contenido[8] == 0x57
                && contenido[9] == 0x45
                && contenido[10] == 0x42
                && contenido[11] == 0x50;
            default -> false;
        };
    }

    private static ResponseStatusException formatoNoPermitido() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de imagen no permitido. Usa JPG, PNG o WebP");
    }

    private static ResponseStatusException imagenMuyPesada() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cada foto puede pesar como maximo 5 MB");
    }
}
