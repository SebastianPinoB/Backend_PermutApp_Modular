package com.example.PermutApp.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;
import software.amazon.awssdk.services.textract.model.TextractException;

@RestControllerAdvice
public class ApiExceptionHandler {

   @ExceptionHandler(ResponseStatusException.class)
   public ResponseEntity<ApiErrorResponse> handleResponseStatusException(ResponseStatusException exception) {
      HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
      return ResponseEntity
            .status(status)
            .body(new ApiErrorResponse(status.value(), exception.getReason()));
   }

   @ExceptionHandler(RekognitionException.class)
   public ResponseEntity<ApiErrorResponse> handleRekognitionException(RekognitionException exception) {
      return ResponseEntity
            .badRequest()
            .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), "No se pudo comparar el rostro. Revisa que el carnet y la selfie sean fotos claras y válidas."));
   }

   @ExceptionHandler(TextractException.class)
   public ResponseEntity<ApiErrorResponse> handleTextractException(TextractException exception) {
      return ResponseEntity
            .badRequest()
            .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), "No se pudo leer el carnet. Usa una imagen clara, frontal y sin recortes."));
   }

   @ExceptionHandler(SdkClientException.class)
   public ResponseEntity<ApiErrorResponse> handleAwsClientException(SdkClientException exception) {
      return ResponseEntity
            .status(HttpStatus.BAD_GATEWAY)
            .body(new ApiErrorResponse(HttpStatus.BAD_GATEWAY.value(), "No se pudo conectar con el servicio de verificación. Revisa las credenciales AWS."));
   }

   @ExceptionHandler(MethodArgumentNotValidException.class)
   public ResponseEntity<ApiValidationErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
      List<String> errors = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .toList();

      return ResponseEntity
            .badRequest()
            .body(new ApiValidationErrorResponse(HttpStatus.BAD_REQUEST.value(), "Solicitud invalida", errors));
   }

   public record ApiErrorResponse(int status, String message) {
   }

   public record ApiValidationErrorResponse(int status, String message, List<String> errors) {
   }
}
