package com.moretale.domain.story.dto;

import com.moretale.domain.story.entity.Slide;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "슬라이드 응답 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlideResponse {

    @Schema(description = "슬라이드 ID", example = "10")
    private Long slideId;

    @Schema(description = "슬라이드 순서 (0부터 시작)", example = "0")
    private Integer order;

    @Schema(description = "슬라이드 이미지 URL", example = "https://storage.example.com/images/slide1.png")
    private String imageUrl;

    @Schema(description = "한국어 텍스트", example = "옛날 옛날에 흥부와 놀부가 살았어요.")
    private String textKr;

    @Schema(description = "제2언어 텍스트", example = "Ngày xưa có Heungbu và Nolbu.")
    private String textNative;

    @Schema(description = "한국어 음성 URL", example = "https://storage.example.com/audio/slide1_kr.mp3")
    private String audioUrlKr;

    @Schema(description = "제2언어 음성 URL", example = "https://storage.example.com/audio/slide1_vi.mp3")
    private String audioUrlNative;

    @Schema(description = "단어 토큰 목록 (하이라이트 단어)")
    private List<TokenResponse> tokens;

    public static SlideResponse from(Slide slide) {
        return SlideResponse.builder()
                .slideId(slide.getSlideId())
                .order(slide.getOrder())
                .imageUrl(slide.getImageUrl())
                .textKr(slide.getTextKr())
                .textNative(slide.getTextNative())
                .audioUrlKr(slide.getAudioUrlKr())
                .audioUrlNative(slide.getAudioUrlNative())
                .tokens(slide.getTokens().stream()
                        .map(TokenResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
