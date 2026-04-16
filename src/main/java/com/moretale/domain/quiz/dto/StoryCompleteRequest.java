package com.moretale.domain.quiz.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

// POST /api/quiz/story-complete 요청 DTO
// 동화 완독 시 꿀단지 지급 요청
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryCompleteRequest {

    @NotNull(message = "동화 ID는 필수입니다.")
    private Long storyId;
}
