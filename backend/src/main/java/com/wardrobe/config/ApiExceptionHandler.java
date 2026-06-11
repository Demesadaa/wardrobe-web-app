package com.wardrobe.config;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private final MessageSource messageSource;

    public ApiExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request,
            Locale locale) {
        List<ApiFieldError> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiFieldError(error.getField(), localizedFieldMessage(error, locale)))
                .toList();
        log.warn("Validation failed for {} with {} field errors", request.getRequestURI(), fieldErrors.size());

        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message("error.validation", locale),
                request.getRequestURI(),
                fieldErrors));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(
            ResponseStatusException exception,
            HttpServletRequest request,
            Locale locale) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        if (status.is5xxServerError()) {
            log.error("API error for {} with status {}", request.getRequestURI(), status, exception);
        } else {
            log.warn("API request {} failed with status {} and reason {}", request.getRequestURI(), status, exception.getReason());
        }

        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message(messageKeyFor(status), locale),
                request.getRequestURI(),
                List.of()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request,
            Locale locale) {
        log.warn("Access denied for {}", request.getRequestURI());
        HttpStatus status = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message("error.forbidden", locale),
                request.getRequestURI(),
                List.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request,
            Locale locale) {
        log.error("Unexpected API error for {}", request.getRequestURI(), exception);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message("error.internal", locale),
                request.getRequestURI(),
                List.of()));
    }

    private String localizedFieldMessage(FieldError error, Locale locale) {
        return messageSource.getMessage(error, locale);
    }

    private String message(String key, Locale locale) {
        return messageSource.getMessage(key, null, locale);
    }

    private String messageKeyFor(HttpStatus status) {
        return switch (status) {
            case UNAUTHORIZED -> "error.unauthorized";
            case FORBIDDEN -> "error.forbidden";
            case NOT_FOUND -> "error.notFound";
            case CONFLICT -> "error.conflict";
            case BAD_REQUEST -> "error.badRequest";
            default -> status.is5xxServerError() ? "error.internal" : "error.badRequest";
        };
    }

    public record ApiErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            List<ApiFieldError> fieldErrors) {
    }

    public record ApiFieldError(String field, String message) {
    }
}
