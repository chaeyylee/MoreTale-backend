package com.moretale.global.exception;

import com.moretale.domain.vocabulary.exception.TokenNotFoundException;
import com.moretale.domain.vocabulary.exception.VocabularyAccessDeniedException;
import com.moretale.domain.vocabulary.exception.VocabularyDuplicateException;
import com.moretale.domain.vocabulary.exception.VocabularyNotFoundException;
import com.moretale.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 비즈니스 예외 (ErrorCode 기반)

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage());

        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode.getCode(), e.getMessage()));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        log.error("CustomException: {}", e.getMessage());

        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode.getCode(), e.getMessage()));
    }

    // 2. Vocabulary 도메인 예외
    // ResponseEntity<ApiResponse<...>> 구조로 통일하여 상태코드와 응답 바디를 일관되게 반환

    @ExceptionHandler(VocabularyNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleVocabularyNotFound(VocabularyNotFoundException e) {
        log.error("VocabularyNotFoundException: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("VOCABULARY_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(VocabularyAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleVocabularyAccessDenied(VocabularyAccessDeniedException e) {
        log.error("VocabularyAccessDeniedException: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("VOCABULARY_ACCESS_DENIED", e.getMessage()));
    }

    @ExceptionHandler(VocabularyDuplicateException.class)
    public ResponseEntity<ApiResponse<Void>> handleVocabularyDuplicate(VocabularyDuplicateException e) {
        log.error("VocabularyDuplicateException: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("VOCABULARY_DUPLICATE", e.getMessage()));
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenNotFound(TokenNotFoundException e) {
        log.error("TokenNotFoundException: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("TOKEN_NOT_FOUND", e.getMessage()));
    }

    // 3. 클라이언트 요청 오류

    // @Valid 검증 실패 시 발생 (필수값 누락 등)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException e
    ) {
        log.error("ValidationException: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", "입력값 검증 실패", errors));
    }

    // JSON 파싱 에러 (타입 불일치, 잘못된 형식 등)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e
    ) {
        log.error("HttpMessageNotReadableException: {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(
                        "INVALID_JSON_FORMAT",
                        "요청 데이터 형식이 올바르지 않습니다. 필드 타입을 확인해주세요."
                ));
    }

    // 4. 최종 방어선

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected Exception: ", e);

        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."));
    }
}
