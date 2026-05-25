package com.moretale.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

// 프로젝트 전역 설정 프로퍼티 클래스
@Configuration
@ConfigurationProperties(prefix = "moretale")
@Getter
@Setter
public class MoreTaleProperties {

    private Ai ai = new Ai(); // AI 관련 설정 (동화/이미지 생성)
    private Tts tts = new Tts(); // TTS 관련 설정 (음성 변환 API)
    private Quiz quiz = new Quiz(); // 퀴즈 관련 설정 (자동 생성 API)
    private Cors cors = new Cors(); // 프론트엔드 CORS 허용 origin 설정

    // AI 생성 모델 관련 API 주소 설정
    @Getter
    @Setter
    public static class Ai {
        private String baseUrl;
        private String apiKey;
        private String callbackBaseUrl;
        private String storyGenerationUrl;
        private String imageGenerationUrl;

        public String resolveBaseUrl() {
            if (baseUrl != null && !baseUrl.isBlank()) {
                return baseUrl;
            }
            return storyGenerationUrl;
        }
    }

    // 텍스트 음성 변환(TTS) 서비스 설정
    @Getter
    @Setter
    public static class Tts {
        private String url;
    }

    // 학습용 퀴즈 서비스 설정
    @Getter
    @Setter
    public static class Quiz {
        private String autoGenerationUrl;
    }

    // 프론트엔드 CORS 허용 origin 설정
    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();
    }
}
