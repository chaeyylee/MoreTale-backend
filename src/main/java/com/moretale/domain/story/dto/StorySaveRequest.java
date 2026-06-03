package com.moretale.domain.story.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Schema(description = "동화 저장 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorySaveRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    @Schema(description = "동화 제목", example = "민준이의 우주 모험")
    private String title;

    @Schema(description = "사용자 입력 프롬프트", example = "우주를 탐험하는 이야기")
    private String prompt;

    @Schema(description = "프로필 ID (미입력 시 가장 최근 프로필 사용)", example = "1")
    private Long profileId;

    @NotEmpty(message = "슬라이드가 비어있습니다.")
    @Schema(description = "슬라이드 목록 (생성 API 응답값을 그대로 사용)")
    private List<SlideRequest> slides;

    @Schema(description = "슬라이드 저장 요청 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SlideRequest {

        @Schema(description = "슬라이드 순서 (DB 저장 기준, 0부터 시작)", example = "0")
        private Integer order;

        @Schema(description = "슬라이드 이미지 URL",
                example = "https://storage.googleapis.com/moretale-ai-generated-project-640335ef-3b09-441e-a26/generated/20260602_160057_story_job/illus_0.png")
        private String imageUrl;

        @Schema(description = "제1언어 텍스트", example = "옛날 옛날에 민준이가 살았어요.")
        private String textKr;

        @Schema(description = "제2언어 텍스트", example = "Ngày xưa có Minjun.")
        private String textNative;

        @Schema(description = "제1언어 음성 URL (wav 형식)",
                example = "https://storage.googleapis.com/moretale-ai-generated-project-640335ef-3b09-441e-a26/generated/20260602_160057_story_job/audio_0_primary.wav")
        private String audioUrlKr;

        @Schema(description = "제2언어 음성 URL (wav 형식)",
                example = "https://storage.googleapis.com/moretale-ai-generated-project-640335ef-3b09-441e-a26/generated/20260602_160057_story_job/audio_0_secondary.wav")
        private String audioUrlNative;

        @Schema(description = "AI가 생성한 슬라이드별 핵심 단어 목록 (없으면 빈 리스트 또는 null)")
        private List<VocabularyItem> vocabulary;
    }
}
