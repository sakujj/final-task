package io.github.sakujj.nms.exception;

import feign.FeignException;
import jakarta.validation.ConstraintViolationException;
import org.apache.http.HttpStatus;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Global exception handler
 */
@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleServiceException(RuntimeException runtimeException) {

        ApiError apiError = ApiError.builder()
                .errorMessage(runtimeException.getMessage() + " " + runtimeException.getClass())
                .build();

        return ResponseEntity
                .status(HttpStatus.SC_BAD_REQUEST)
                .body(apiError);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException accessDeniedException) {

        ApiError apiError = ApiError.builder()
                .errorMessage(accessDeniedException.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.SC_FORBIDDEN)
                .body(apiError);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(AuthenticationException authenticationException) {

        ApiError apiError = ApiError.builder()
                .errorMessage(authenticationException.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.SC_UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"Access to comments\"")
                .body(apiError);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintValidationException(ConstraintViolationException ex) {

        ApiError apiError = ApiError.builder()
                .errorMessage(ex.getMessage())
                .validationErrors(ex.getConstraintViolations()
                        .stream()
                        .map(violation -> violation.getPropertyPath().toString().replaceAll(".*\\.", "")
                                + " : "
                                + violation.getMessage())
                        .toList())
                .build();

        return ResponseEntity
                .status(HttpStatus.SC_BAD_REQUEST)
                .body(apiError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {

        ApiError apiError = ApiError.builder()
                .errorMessage("The request contains invalid data")
                .validationErrors(ex.getFieldErrors().stream()
                        .map(fe -> fe.getField() + " : " + fe.getDefaultMessage())
                        .toList())
                .build();

        return ResponseEntity
                .status(HttpStatus.SC_BAD_REQUEST)
                .body(apiError);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ApiError apiError = ApiError.builder()
                .errorMessage(ex.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.SC_BAD_REQUEST)
                .body(apiError);
    }


    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        ApiError apiError = ApiError.builder()
                .errorMessage(ex.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.SC_NOT_FOUND)
                .body(apiError);
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex) {
        ApiError apiError = ApiError.builder()
                .errorMessage("""
                        %s. Incorrect property "%s" value : %s""".formatted(ex.getMessage(), ex.getPropertyName(), ex.getValue())
                )
                .build();

        return ResponseEntity
                .status(HttpStatus.SC_BAD_REQUEST)
                .body(apiError);
    }
}
