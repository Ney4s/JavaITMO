package neyas.itmo.web;

import neyas.itmo.exception.AccessDeniedException;
import neyas.itmo.exception.DomainException;
import neyas.itmo.exception.DomainValidationException;
import neyas.itmo.exception.EntityNotFoundException;
import neyas.itmo.exception.IncompatibleComponentException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException exception){
        return build(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(IncompatibleComponentException.class)
    public ResponseEntity<ApiError> handleIncompatible(IncompatibleComponentException exception){
        return build(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException exception){
        return build(HttpStatus.FORBIDDEN, exception);
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ApiError> handleValidation(DomainValidationException exception){
        return build(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomain(DomainException exception){
        return build(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException exception){
        return build(HttpStatus.FORBIDDEN, exception);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException exception){
        return build(HttpStatus.BAD_REQUEST, exception);
    }

    private static ResponseEntity<ApiError> build(HttpStatus status, Exception exception){
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), exception.getMessage()));
    }
}
