package com.sns.backend.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ApiResponse<T> {
    private final int status;          // HTTP 상태 코드 (예: 200, 201, 400...)
    private final boolean success;     // 성공 여부
    private final String message;      // 설명 메시지
    private final T data;              // 성공 시 데이터
    private final List<ErrorDetail> errors; // 실패 시 에러 목록
    private final String timestamp;    // 응답 시간 (ISO-8601)

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ErrorDetail {
        private final String field;    // 유효성 검증 시 필드명(없으면 null)
        private final String reason;   // 에러 사유
    }

    public static <T> ApiResponse<T> success(T data, String message, int httpStatus) {
        return ApiResponse.<T>builder()
                .status(httpStatus)
                .success(true)
                .message(message)
                .data(data)
                .timestamp(OffsetDateTime.now().toString())
                .build();
    }

    public static <T> ApiResponse<T> success(T data, int httpStatus) {
        return success(data, null, httpStatus);
    }

    public static <T> ApiResponse<T> failure(String message, int httpStatus, List<ErrorDetail> errors) {
        return ApiResponse.<T>builder()
                .status(httpStatus)
                .success(false)
                .message(message)
                .errors(errors)
                .timestamp(OffsetDateTime.now().toString())
                .build();
    }

    public static <T> ApiResponse<T> failure(String message, int httpStatus) {
        return failure(message, httpStatus, null);
    }
}
