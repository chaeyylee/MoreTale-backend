package com.moretale.domain.quiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Schema(description = "동화 완독 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryCompleteRequest {

    @NotNull(message = "동화 ID는 필수입니다.")
    @Schema(description = "완독한 동화 ID", example = "5")
    private Long storyId;
}
