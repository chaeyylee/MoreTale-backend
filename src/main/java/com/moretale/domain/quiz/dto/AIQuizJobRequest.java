package com.moretale.domain.quiz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

// AI 서버 POST /internal/ai/quiz/jobs 요청 DTO
// Python QuizInternalJobRequest 구조에 맞췄음
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIQuizJobRequest {

    // 완료 시 호출할 백엔드 callback URL
    @JsonProperty("callbackUrl")
    private String callbackUrl;

    // 백엔드 내부 동화 ID
    @JsonProperty("storyId")
    private String storyId;

    // 퀴즈 생성에 사용할 동화 본문
    @JsonProperty("story")
    private AIQuizStoryPayload story;

    // 생성할 문제 수
    @JsonProperty("questionCount")
    private int questionCount;

    // 사용할 AI 모델명
    @JsonProperty("model")
    private String model;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIQuizStoryPayload {

        // 제1언어 동화 제목
        @JsonProperty("title_primary")
        private String titlePrimary;

        // 제2언어 동화 제목
        @JsonProperty("title_secondary")
        private String titleSecondary;

        // 저자명
        @JsonProperty("author_name")
        private String authorName;

        // 제1언어 코드
        @JsonProperty("primary_language")
        private String primaryLanguage;

        // 제2언어 코드
        @JsonProperty("secondary_language")
        private String secondaryLanguage;

        // 그림 스타일
        @JsonProperty("image_style")
        private String imageStyle;

        // 주인공 디자인 설명
        @JsonProperty("main_character_design")
        private String mainCharacterDesign;

        // 페이지 목록
        @JsonProperty("pages")
        private List<AIQuizPagePayload> pages;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIQuizPagePayload {

        // 페이지 번호
        @JsonProperty("page_number")
        private int pageNumber;

        // 제1언어 본문
        @JsonProperty("text_primary")
        private String textPrimary;

        // 제2언어 본문
        @JsonProperty("text_secondary")
        private String textSecondary;

        // 이미지 생성 프롬프트
        @JsonProperty("illustration_prompt")
        private String illustrationPrompt;

        // 해당 페이지의 핵심 단어 목록
        @JsonProperty("vocabulary")
        private List<AIQuizVocabEntry> vocabulary;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIQuizVocabEntry {

        // 단어 엔트리 ID
        @JsonProperty("entry_id")
        private String entryId;

        // 제1언어 단어
        @JsonProperty("primary_word")
        private String primaryWord;

        // 제2언어 단어
        @JsonProperty("secondary_word")
        private String secondaryWord;

        // 제1언어 뜻
        @JsonProperty("primary_definition")
        private String primaryDefinition;

        // 제2언어 뜻
        @JsonProperty("secondary_definition")
        private String secondaryDefinition;
    }
}
