package com.laklu.pos.dataObjects;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

public class ApiResponseEntity extends ResponseEntity<ApiResponseEntity.GenericBody> {
    @Builder
    @AllArgsConstructor
    @Setter
    @Getter
    @NoArgsConstructor
    public static class GenericBody {
        private Object data;
        private Object message;
        private Integer httpStatus;
        private String timestamp;
        private Object error;
    }

    public ApiResponseEntity(HttpStatusCode status) {
        super(status);
    }

    public ApiResponseEntity(GenericBody body, HttpStatusCode status) {
        super(body, status);
    }

    public ApiResponseEntity(MultiValueMap<String, String> headers, HttpStatusCode status) {
        super(headers, status);
    }

    public ApiResponseEntity(GenericBody body, MultiValueMap<String, String> headers, int rawStatus) {
        super(body, headers, rawStatus);
    }

    public ApiResponseEntity(GenericBody body, MultiValueMap<String, String> headers, HttpStatusCode statusCode) {
        super(body, headers, statusCode);
    }

    public static ApiResponseEntity success(Object data) {
        var body = GenericBody.builder()
                .data(data)
                .message("success")
                .httpStatus(HttpStatus.OK.value())
                .timestamp(new java.util.Date().toString())
                .build();
        return new ApiResponseEntity(body, HttpStatus.OK);
    }

    public static ApiResponseEntity success(Object data, String message) {
        var body = GenericBody.builder()
                .data(data)
                .message(message)
                .httpStatus(HttpStatus.OK.value())
                .timestamp(new java.util.Date().toString())
                .build();
        return new ApiResponseEntity(body, HttpStatus.OK);
    }

    public static ApiResponseEntity exception(HttpStatusCode status, Object error) {
        var body = GenericBody.builder()
                .error(error)
                .httpStatus(status.value())
                .timestamp(new java.util.Date().toString())
                .build();
        return new ApiResponseEntity(body, status);
    }

    public static ApiResponseEntity exception(HttpStatusCode status, Object error, String message) {
        var body = GenericBody.builder()
                .error(error)
                .message(message)
                .httpStatus(status.value())
                .timestamp(new java.util.Date().toString())
                .build();
        return new ApiResponseEntity(body, status);
    }

    public static ApiResponseEntity accepted(Object data) {
        var body = GenericBody.builder()
                .data(data)
                .message("accepted")
                .httpStatus(HttpStatus.ACCEPTED.value())
                .timestamp(new java.util.Date().toString())
                .build();
        return new ApiResponseEntity(body, HttpStatus.ACCEPTED);
    }
}
