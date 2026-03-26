package com.moretale.domain.story.dto;

import com.moretale.domain.story.entity.Slide;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlideResponse {

    private Long slideId;
    private Integer order;
    private String imageUrl;
    private String textKr;
    private String textNative;
    private String audioUrlKr;
    private String audioUrlNative;

    // 단어 토큰 목록
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
