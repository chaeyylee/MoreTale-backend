package com.moretale.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;

// 모든 API 응답을 일관된 JSON 구조로 반환하기 위해 사용
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // null인 필드는 JSON 결과에서 제외
public class ApiResponse<T> {

    private final boolean success; // 요청 성공 여부
    private final T data; // 응답 데이터 (성공 시 객체, 실패 시 null 또는 에러 상세)
    private final String message; // 사용자에게 보여줄 메세지
    private final String errorCode; // 비즈니스 에러 코드 (ex. USER_NOT_FOUND)
    private final LocalDateTime timestamp; // 응답 생성 시각

    // 정적 팩토리 메서드 사용을 위해 생성자를 private으로 제한
    private ApiResponse(boolean success, T data, String message, String errorCode) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    // 성공 응답 - 데이터만 반환
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    // 성공 응답 - 데이터와 성공 메시지 함께 반환
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    // 에러 응답 - 메시지만 반환 (기존 response.ApiResponse와의 호환용)
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, null);
    }

    // 에러 응답 - 에러 코드 + 메시지
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return new ApiResponse<>(false, null, message, errorCode);
    }

    // 에러 응답 - 에러 코드 + 메시지 + 데이터
    public static <T> ApiResponse<T> error(String errorCode, String message, T data) {
        return new ApiResponse<>(false, data, message, errorCode);
    }
}
