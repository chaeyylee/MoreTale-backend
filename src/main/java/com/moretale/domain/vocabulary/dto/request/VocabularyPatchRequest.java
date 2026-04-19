package com.moretale.domain.vocabulary.dto.request;

import com.moretale.domain.vocabulary.entity.VocabularyEntry.LearningStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "단어장 항목 수정 요청 DTO (null인 필드는 변경되지 않음)")
@Getter
@NoArgsConstructor
public class VocabularyPatchRequest {

    @Schema(description = "즐겨찾기 여부 (null이면 변경 안 함)", example = "true")
    private Boolean isFavorite;

    @Schema(description = "학습 상태 (UNSEEN / LEARNING / MASTERED, null이면 변경 안 함)",
            example = "LEARNING")
    private LearningStatus learningStatus;

    @Schema(description = "메모 (null이면 변경 안 함)", example = "동화에서 중요한 단어")
    private String memo;
}
