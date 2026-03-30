package com.example.silverpear.errors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request) {
        int code = ex.getStatusCode().value();
        HttpStatus resolved = HttpStatus.resolve(code);
        String reason = ex.getReason() != null ? ex.getReason()
                : (resolved != null ? resolved.getReasonPhrase() : "Ошибка");
        ErrorResponse body = build(code, reason, List.of(reason), request);
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<String> errors = collectBindingErrors(ex.getBindingResult());
        return ResponseEntity.badRequest().body(build(
                HttpStatus.BAD_REQUEST,
                "Плохой запрос",
                errors,
                request));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException ex,
            HttpServletRequest request) {
        List<String> errors = collectBindingErrors(ex.getBindingResult());
        return ResponseEntity.badRequest().body(build(
                HttpStatus.BAD_REQUEST,
                "Ошибка привязки данных",
                errors,
                request));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
        return ResponseEntity.badRequest().body(build(
                HttpStatus.BAD_REQUEST,
                "Ошибка валидации",
                errors,
                request));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        String detail = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();
        return ResponseEntity.badRequest().body(build(
                HttpStatus.BAD_REQUEST,
                "Некорректное тело запроса",
                List.of(detail != null ? detail : "JSON или формат данных неверен"),
                request));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        String msg = "Отсутствует обязательный параметр: " + ex.getParameterName();
        return ResponseEntity.badRequest().body(build(
                HttpStatus.BAD_REQUEST,
                "Плохой запрос",
                List.of(msg),
                request));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        String name = ex.getName() != null ? ex.getName() : "параметр";
        String required = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "?";
        String msg = "Параметр «" + name + "» должен быть типа " + required;
        return ResponseEntity.badRequest().body(build(
                HttpStatus.BAD_REQUEST,
                "Неверный тип параметра",
                List.of(msg),
                request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Недопустимый аргумент";
        return ResponseEntity.badRequest().body(build(
                HttpStatus.BAD_REQUEST,
                "Некорректные данные",
                List.of(msg),
                request));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException ex,
            HttpServletRequest request) {
        String msg = "Ресурс не найден: " + ex.getResourcePath();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(
                HttpStatus.NOT_FOUND,
                "Не найдено",
                List.of(msg),
                request));
    }

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ResponseEntity<ErrorResponse> handleHttpClientErrorNotFound(
            HttpClientErrorException.NotFound ex,
            HttpServletRequest request) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Ресурс не найден";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(
                HttpStatus.NOT_FOUND,
                "Ресурс не найден",
                List.of(msg),
                request));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        String detail = ex.getSupportedHttpMethods() != null && !ex.getSupportedHttpMethods().isEmpty()
                ? "Допустимые методы: " + String.join(", ",
                ex.getSupportedHttpMethods().stream()
                        .map(HttpMethod::name)
                        .collect(Collectors.toList()))
                : (ex.getMessage() != null ? ex.getMessage() : "");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(build(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Метод не поддерживается",
                List.of(detail),
                request));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {
        log.warn("Нарушение ограничения БД: {}", ex.getMostSpecificCause().getMessage());
        String msg = "Конфликт данных: нарушено ограничение уникальности или целостности";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(build(
                HttpStatus.CONFLICT,
                msg,
                List.of(msg),
                request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        log.error("Необработанное исключение", ex);
        String msg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка сервера",
                List.of(msg),
                request));
    }

    private List<String> collectBindingErrors(BindingResult result) {
        List<String> errors = new ArrayList<>();
        result.getFieldErrors().forEach(
                error -> errors.add(error.getField() + ": " + error.getDefaultMessage()));
        result.getGlobalErrors().forEach(
                error -> errors.add(error.getDefaultMessage()));
        return errors;
    }

    private ErrorResponse build(
            HttpStatus status,
            String message,
            List<String> errors,
            HttpServletRequest request) {
        return build(status.value(), message, errors, request);
    }

    private ErrorResponse build(
            int statusCode,
            String message,
            List<String> errors,
            HttpServletRequest request) {
        return ErrorResponse.builder()
                .message(message)
                .errors(errors)
                .status(statusCode)
                .path(request.getRequestURL().toString())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
