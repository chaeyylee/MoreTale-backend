package com.moretale.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통 (Common)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C002", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "C003", "권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버 오류가 발생했습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C005", "허용되지 않은 메서드입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C006", "요청한 리소스를 찾을 수 없습니다."),

    // 사용자 (User)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "이미 존재하는 사용자입니다."),
    USER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "U003", "이미 탈퇴된 사용자입니다."),

    // 프로필 (Profile)
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "프로필을 찾을 수 없습니다."),
    PROFILE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "P002", "접근 권한이 없습니다."),
    PROFILE_ALREADY_EXISTS(HttpStatus.CONFLICT, "P003", "이미 프로필이 존재합니다."),

    // 동화 (Story)
    STORY_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "동화를 찾을 수 없습니다."),
    STORY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "S002", "동화에 접근할 권한이 없습니다."),
    STORY_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S003", "동화 생성에 실패했습니다."),
    STORY_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S004", "동화 저장에 실패했습니다."),

    // 슬라이드 (Slide)
    SLIDE_NOT_FOUND(HttpStatus.NOT_FOUND, "SL001", "슬라이드를 찾을 수 없습니다."),
    INVALID_SLIDE_ORDER(HttpStatus.BAD_REQUEST, "SL002", "슬라이드 순서가 올바르지 않습니다."),

    // 음성 생성 (TTS)
    TTS_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "T001", "음성 생성(TTS)에 실패했습니다."),
    TTS_INVALID_LANGUAGE(HttpStatus.BAD_REQUEST, "T002", "지원하지 않는 언어 코드입니다."),
    TTS_TEXT_TOO_LONG(HttpStatus.BAD_REQUEST, "T003", "텍스트가 너무 깁니다 (최대 5000자)"),

    // AI 서비스 (AI)
    AI_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "A001", "AI 서비스 오류가 발생했습니다."),
    AI_RESPONSE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "A002", "AI 응답이 올바르지 않습니다."),

    // 파일/저장 (File)
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F001", "파일 업로드에 실패했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "F002", "파일을 찾을 수 없습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "F003", "파일 크기가 제한을 초과했습니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F004", "파일 삭제에 실패했습니다."),

    // 퀴즈 (Quiz)
    QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "Q001", "퀴즈를 찾을 수 없습니다."),
    QUIZ_ALREADY_EXISTS(HttpStatus.CONFLICT, "Q002", "이미 생성된 퀴즈가 있습니다."),
    QUIZ_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Q003", "퀴즈 생성에 실패했습니다."),
    QUIZ_ACCESS_DENIED(HttpStatus.FORBIDDEN, "Q004", "퀴즈에 접근할 권한이 없습니다."),
    QUIZ_SUBMIT_INVALID(HttpStatus.BAD_REQUEST, "Q005", "퀴즈 답안이 올바르지 않습니다."),

    // 꿀단지 (HoneyJar)
    HONEY_JAR_NOT_FOUND(HttpStatus.NOT_FOUND, "H001", "꿀단지 정보를 찾을 수 없습니다."),
    HONEY_JAR_INSUFFICIENT(HttpStatus.BAD_REQUEST, "H002", "꿀단지가 부족합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
