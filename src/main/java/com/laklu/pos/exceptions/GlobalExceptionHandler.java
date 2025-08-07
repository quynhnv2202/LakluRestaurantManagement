package com.laklu.pos.exceptions;

import com.laklu.pos.dataObjects.ApiResponseEntity;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Map;
import java.util.stream.Collectors;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JwtException.class)
    public ApiResponseEntity handleBaseException(JwtException e) {
        return ApiResponseEntity.exception(HttpStatus.BAD_REQUEST, ExceptionCode.INVALID_TOKEN.getMessage());
    }

    @ExceptionHandler(RestHttpException.class)
    public ApiResponseEntity handleBaseException(RestHttpException e) {
        return ApiResponseEntity.exception(e. getStatusCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponseEntity handleException(Exception e) {
        return ApiResponseEntity.exception(HttpStatus.INTERNAL_SERVER_ERROR, e.getCause(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ApiResponseEntity handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existingValue, newValue) -> newValue
                ));
        return ApiResponseEntity.exception(HttpStatus.UNPROCESSABLE_ENTITY, errors);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    protected ApiResponseEntity handleMissingServletRequestPart(MissingServletRequestPartException ex) {
        return ApiResponseEntity.exception(HttpStatus.BAD_REQUEST, ex.getRequestPartName() + " is missing");
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ApiResponseEntity accessDenied(AccessDeniedException ex) {
        return ApiResponseEntity.exception(HttpStatus.FORBIDDEN, ExceptionCode.FORBIDDEN.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ApiResponseEntity handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ApiResponseEntity.exception(HttpStatus.BAD_REQUEST, ExceptionCode.BAD_REQUEST.getCode());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ApiResponseEntity handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ApiResponseEntity.exception(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
    }

    @ExceptionHandler(MultipartException.class)
    public ApiResponseEntity handleFileUploadException(MultipartException e) {
        return ApiResponseEntity.exception(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(RuleNotValidException.class)
    protected ApiResponseEntity handleRuleException(RuleNotValidException ex) {
        return ApiResponseEntity.exception(HttpStatus.UNPROCESSABLE_ENTITY, ex.getErrors());
    }

}
