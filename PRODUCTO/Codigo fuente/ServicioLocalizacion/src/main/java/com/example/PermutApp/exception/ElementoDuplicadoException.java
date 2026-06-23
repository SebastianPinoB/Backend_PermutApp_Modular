package com.example.PermutApp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.CONFLICT)
public class ElementoDuplicadoException extends RuntimeException {
    public ElementoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
