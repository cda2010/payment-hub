package com.gateway.payment.pricing.api;

import com.gateway.payment.pricing.tarif.TarifException;
import com.gateway.payment.pricing.tarif.validation.ValidationIssue;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestControllerAdvice
public class PricingExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handle(ResponseStatusException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.name(), e.getReason(), request.getRequestURI()));
    }

    @ExceptionHandler(TarifException.class)
    public ResponseEntity<ApiError> handle(TarifException e, HttpServletRequest request) {
        return ResponseEntity.status(e.getStatus())
                .body(ApiError.of(e.getStatus().value(), e.getCode(), e.getMessage(), request.getRequestURI(),
                        e.getDetails()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handle(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<ValidationIssue> details = e.getBindingResult().getFieldErrors().stream()
                .map(f -> ValidationIssue.grille("CHAMP_INVALIDE", f.getField(), f.getField() + " : " + f.getDefaultMessage()))
                .toList();
        return badRequest("REQUETE_INVALIDE", "Requête invalide.", request, details);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> handleUnreadable(Exception e, HttpServletRequest request) {
        return badRequest("REQUETE_ILLISIBLE", "Corps ou paramètre de requête illisible.", request, List.of());
    }

    private static ResponseEntity<ApiError> badRequest(
            String code, String message, HttpServletRequest request, List<ValidationIssue> details
    ) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST.value(), code, message, request.getRequestURI(), details));
    }
}
