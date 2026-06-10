package com.moretale.domain.quiz.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

// AI 서버 GET /internal/ai/quiz/jobs/{jobId}/result 응답 DTO
// InternalJobResultResponse.data 필드 안의 Quiz 결과를 매핑
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AIQuizResultResponse {

    // AI job ID
    @JsonProperty("jobId")
    @JsonAlias("job_id")
    private String jobId;

    // job 타입
    @JsonProperty("type")
    private String type;

    // completed / failed / running
    @JsonProperty("status")
    private String status;

    // Quiz 결과 본문
    @JsonProperty("data")
    private AIQuizData data;

    // 실패 시 에러 정보
    @JsonProperty("error")
    private Object error;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIQuizData {

        // 동화 ID
        @JsonProperty("storyId")
        @JsonAlias("story_id")
        private String storyId;

        // 제1언어 동화 제목
        @JsonProperty("storyTitlePrimary")
        @JsonAlias("story_title_primary")
        private String storyTitlePrimary;

        // 제2언어 동화 제목
        @JsonProperty("storyTitleSecondary")
        @JsonAlias("story_title_secondary")
        private String storyTitleSecondary;

        // 제1언어 코드
        @JsonProperty("primaryLanguage")
        @JsonAlias("primary_language")
        private String primaryLanguage;

        // 제2언어 코드
        @JsonProperty("secondaryLanguage")
        @JsonAlias("secondary_language")
        private String secondaryLanguage;

        // 문제 수
        @JsonProperty("questionCount")
        @JsonAlias("question_count")
        private int questionCount;

        // 문제 목록
        @JsonProperty("questions")
        private List<AIQuizQuestionData> questions;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIQuizQuestionData {

        // AI 내부 문제 ID
        @JsonProperty("questionId")
        @JsonAlias("question_id")
        private String questionId;

        // 문제 유형
        @JsonProperty("type")
        private String type;

        // VOCABULARY 또는 STORY
        @JsonProperty("skill")
        private String skill;

        // 문제 텍스트
        @JsonProperty("questionText")
        @JsonAlias("question_text")
        private String questionText;

        // 선택지 목록
        @JsonProperty("choices")
        private List<AIQuizChoice> choices;

        // 정답
        @JsonProperty("answer")
        private AIQuizAnswer answer;

        // 해설
        @JsonProperty("explanation")
        private String explanation;

        // 출처 페이지 번호
        @JsonProperty("sourcePageNumbers")
        @JsonAlias("source_page_numbers")
        private List<Integer> sourcePageNumbers;

        // 출처 vocabulary ID
        @JsonProperty("sourceVocabularyEntryIds")
        @JsonAlias("source_vocabulary_entry_ids")
        private List<String> sourceVocabularyEntryIds;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIQuizChoice {

        // 선택지 ID
        @JsonProperty("choiceId")
        @JsonAlias("choice_id")
        private String choiceId;

        // 선택지 텍스트
        @JsonProperty("text")
        private String text;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIQuizAnswer {

        // 정답 선택지 ID
        @JsonProperty("choiceId")
        @JsonAlias("choice_id")
        private String choiceId;

        // 정답 텍스트
        @JsonProperty("text")
        private String text;
    }
}
