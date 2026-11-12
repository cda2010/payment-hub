package com.gateway.payment.network.api;

import com.gateway.payment.network.NetworkException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.List;

@RestControllerAdvice
public class NetworkExceptionHandler {

    @ExceptionHandler(NetworkException.class)
    public ResponseEntity<ApiError> handle(NetworkException e, HttpServletRequest request) {
        return ResponseEntity.status(e.getStatus())
                .body(ApiError.of(e.getStatus().value(), e.getCode(), e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handle(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<ApiError.Detail> details = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new ApiError.Detail("CHAMP_INVALIDE", f.getField(), f.getField() + " : " + f.getDefaultMessage()))
                .toList();
        return badRequest("REQUETE_INVALIDE", "Requête invalide.", request, details);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> handleUnreadable(Exception e, HttpServletRequest request) {
        return badRequest("REQUETE_ILLISIBLE", "Corps ou paramètre de requête illisible.", request, List.of());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleTaille(MaxUploadSizeExceededException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(ApiError.of(HttpStatus.PAYLOAD_TOO_LARGE.value(),
                "FICHIER_TROP_GROS", "Le fichier dépasse 10 Mo : découpez-le.", request.getRequestURI()));
    }

    @ExceptionHandler({MissingServletRequestPartException.class, MultipartException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> handleFichierAbsent(Exception e, HttpServletRequest request) {
        return badRequest("REQUETE_INVALIDE", "Fichier ou paramètre manquant.", request, List.of());
    }

    private static ResponseEntity<ApiError> badRequest(
            String code, String message, HttpServletRequest request, List<ApiError.Detail> details
    ) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST.value(), code, message, request.getRequestURI(), details));
    }
}
