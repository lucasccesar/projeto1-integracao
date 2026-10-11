package com.example.oauthoidcdemo.common.exception;

import com.example.oauthoidcdemo.common.dto.ApiError;
import com.example.oauthoidcdemo.common.dto.ValidationApiError;
import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomainException(DomainException exception, HttpServletRequest request) {
        return ResponseEntity.status(exception.getStatus())
                .body(apiError(exception.getStatus(), exception.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        log.error("Falha interna ao processar a requisição", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(apiError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno no servidor.", request.getRequestURI()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));
        exception.getBindingResult().getGlobalErrors()
                .forEach(error -> fieldErrors.put(error.getObjectName(), error.getDefaultMessage()));
        ValidationApiError body = new ValidationApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Requisição inválida",
                "Verifique os campos informados.",
                path(request),
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        return response(HttpStatus.BAD_REQUEST, "O corpo da requisição está malformado.", path(request), headers);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        return response(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "O formato da requisição não é suportado.", path(request), headers);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        return response(HttpStatus.BAD_REQUEST, "O parâmetro '" + exception.getParameterName() + "' é obrigatório.", path(request), headers);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            org.springframework.beans.TypeMismatchException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        return response(HttpStatus.BAD_REQUEST, "O valor informado possui formato inválido.", path(request), headers);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest()
                .body(apiError(HttpStatus.BAD_REQUEST, "O valor informado possui formato inválido.", request.getRequestURI()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest()
                .body(apiError(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI()));
    }

    private ResponseEntity<Object> response(HttpStatus status, String message, String path, HttpHeaders headers) {
        return ResponseEntity.status(status)
                .headers(headers)
                .body(apiError(status, message, path));
    }

    private ApiError apiError(HttpStatus status, String message, String path) {
        return new ApiError(Instant.now(), status.value(), errorName(status), message, path);
    }

    private String path(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    private String errorName(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Requisição inválida";
            case NOT_FOUND -> "Recurso não encontrado";
            case CONFLICT -> "Conflito";
            case FORBIDDEN -> "Acesso negado";
            case UNSUPPORTED_MEDIA_TYPE -> "Formato não suportado";
            default -> "Erro interno";
        };
    }
}
