package com.hean.consigueventas.oonabe.common.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({EntityNotFoundException.class, ResourceNotFoundException.class})
    public ProblemDetail handleNotFound(RuntimeException ex, WebRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), "not-found", request);
    }

    @ExceptionHandler(BusinessLogicException.class)
    public ProblemDetail handleBusiness(BusinessLogicException ex, WebRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Regla de negocio invalida", ex.getMessage(), "business-rule", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        return validationProblem(ex.getBindingResult(), request);
    }

    @ExceptionHandler(BindException.class)
    public ProblemDetail handleBindingValidation(BindException ex, WebRequest request) {
        return validationProblem(ex.getBindingResult(), request);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(Exception ex, WebRequest request) {
        return problem(HttpStatus.FORBIDDEN, "No autorizado", "No tienes permisos para realizar esta accion.", "forbidden", request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        return problem(HttpStatus.UNAUTHORIZED, "Credenciales invalidas", "Credenciales invalidas.", "invalid-credentials", request);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleUserAlreadyExists(UserAlreadyExistsException ex, WebRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Usuario duplicado", ex.getMessage(), "user-already-exists", request);
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ProblemDetail handleTokenRefresh(TokenRefreshException ex, WebRequest request) {
        return problem(HttpStatus.FORBIDDEN, "Refresh token invalido", ex.getMessage(), "refresh-token-invalid", request);
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    public ProblemDetail handleOptimisticLock(Exception ex, WebRequest request) {
        return problem(
                HttpStatus.CONFLICT,
                "Conflicto de actualizacion",
                "El recurso fue modificado por otra solicitud. Recarga los datos e intenta nuevamente.",
                "concurrent-update",
                request);
    }

    private ProblemDetail validationProblem(BindingResult bindingResult, WebRequest request) {
        ProblemDetail detail = problem(
                HttpStatus.BAD_REQUEST,
                "Solicitud invalida",
                "Uno o mas campos no cumplen las reglas de validacion.",
                "validation-error",
                request);
        detail.setProperty("errors", bindingResult.getFieldErrors().stream()
                .map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
                .toList());
        return detail;
    }

    private ProblemDetail problem(
            HttpStatus status,
            String title,
            String message,
            String type,
            WebRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        detail.setTitle(title);
        detail.setType(URI.create("https://api.oona.local/errors/" + type));
        detail.setProperty("message", message);
        detail.setProperty("timestamp", LocalDateTime.now());
        detail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        return detail;
    }

    private record FieldViolation(String field, String message) {
    }
}
