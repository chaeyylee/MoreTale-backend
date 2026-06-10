package com.moretale.domain.story.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Schema(description = "AI 동화 생성 완료 결과 응답 DTO (저장 전 임시 상태)")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryGenerateResponse {

    @Schema(description = "동화 제목", example = "흥부와 놀부")
    private String title;

    @Schema(description = "주인공 아이 이름", example = "민준")
    private String childName;

    @Schema(description = "제1언어 ISO 코드", example = "ko")
    private String primaryLanguage;

    @Schema(description = "제2언어 ISO 코드", example = "vi")
    private String secondaryLanguage;

    @Schema(description = "생성된 슬라이드 목록. 텍스트, 이미지 URL, TTS URL, 핵심 단어 포함")
    private List<GeneratedSlide> slides;

    @Schema(description = "생성된 슬라이드 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GeneratedSlide {

        @Schema(description = "슬라이드 순서 (0부터 시작)", example = "0")
        private Integer order;

        @Schema(description = "슬라이드 이미지 URL",
                example = "https://storage.googleapis.com/moretale-ai-generated-project-640335ef-3b09-441e-a26/generated/20260602_160057_story_job/illus_0.png")
        private String imageUrl;

        @Schema(description = "제1언어 텍스트", example = "옛날 옛날에 흥부와 놀부가 살았어요.")
        private String textKr;

        @Schema(description = "제2언어 텍스트", example = "Ngày xưa có Heungbu và Nolbu.")
        private String textNative;

        @Schema(description = "제1언어 음성 URL (wav 형식)",
                example = "https://storage.googleapis.com/moretale-ai-generated-project-640335ef-3b09-441e-a26/generated/20260602_160057_story_job/audio_0_primary.wav")
        private String audioUrlKr;

        @Schema(description = "제2언어 음성 URL (wav 형식)",
                example = "https://storage.googleapis.com/moretale-ai-generated-project-640335ef-3b09-441e-a26/generated/20260602_160057_story_job/audio_0_secondary.wav")
        private String audioUrlNative;

        @Schema(description = "AI가 생성한 슬라이드별 핵심 단어 목록 (없으면 빈 리스트)")
        private List<VocabularyItem> vocabulary;
    }
}
