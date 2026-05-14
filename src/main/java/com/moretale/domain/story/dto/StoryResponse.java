package com.moretale.domain.story.dto;

import com.moretale.domain.story.entity.Story;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "동화 상세 응답 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryResponse {

    @Schema(description = "동화 ID", example = "1")
    private Long storyId;

    @Schema(description = "동화 제목", example = "흥부와 놀부")
    private String title;

    @Schema(description = "사용자 입력 프롬프트", example = "우주를 탐험하는 이야기")
    private String prompt;

    @Schema(description = "주인공 아이 이름", example = "민준")
    private String childName;

    /**
     * ISO 코드 기반 언어 값 (ex. "ko", "vi", "en")
     * Profile.firstLanguage(Enum)의 isoCode를 저장한 값.
     * OTHER 언어인 경우 사용자가 입력한 custom 문자열이 저장될 수 있음.
     *
     * Story 계층 언어 구조 통합 완료 후
     * firstLanguage / firstLanguageDisplay / primaryLanguageCode 로 분리 예정.
     */
    @Schema(
            description = """
                    [Legacy] 제1언어 ISO 코드 (ex. ko, vi, en).
                    Profile.firstLanguage Enum의 isoCode 기반 저장값.
                    OTHER인 경우 사용자 입력 custom 문자열이 저장될 수 있음.
                    향후 firstLanguage / firstLanguageDisplay / primaryLanguageCode 필드로 분리 예정.
                    """,
            example = "ko"
    )
    private String primaryLanguage;

    /**
     * ISO 코드 기반 언어 값 (ex. "vi", "en", "ja")
     * Profile.secondLanguage(Enum)의 isoCode를 저장한 값.
     * OTHER 언어인 경우 사용자가 입력한 custom 문자열이 저장될 수 있음.
     *
     * Story 계층 언어 구조 통합 완료 후
     * secondLanguage / secondLanguageDisplay / secondaryLanguageCode 로 분리 예정.
     */
    @Schema(
            description = """
                    [Legacy] 제2언어 ISO 코드 (ex. vi, en, ja).
                    Profile.secondLanguage Enum의 isoCode 기반 저장값.
                    OTHER인 경우 사용자 입력 custom 문자열이 저장될 수 있음.
                    향후 secondLanguage / secondLanguageDisplay / secondaryLanguageCode 필드로 분리 예정.
                    """,
            example = "en"
    )
    private String secondaryLanguage;

    @Schema(description = "공개 여부", example = "false")
    private Boolean isPublic;

    @Schema(description = "생성일시 (ISO 8601)", example = "2024-01-15T09:30:00Z")
    private LocalDateTime createdAt;

    @Schema(description = "슬라이드 목록 (순서 포함)")
    private List<SlideResponse> slides;

    public static StoryResponse from(Story story) {
        return StoryResponse.builder()
                .storyId(story.getStoryId())
                .title(story.getTitle())
                .prompt(story.getPrompt())
                .childName(story.getChildName())
                .primaryLanguage(story.getPrimaryLanguage())
                .secondaryLanguage(story.getSecondaryLanguage())
                .isPublic(story.getIsPublic())
                .createdAt(story.getCreatedAt())
                .slides(story.getSlides().stream()
                        .map(SlideResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
