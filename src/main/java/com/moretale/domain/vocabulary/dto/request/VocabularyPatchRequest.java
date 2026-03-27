package com.moretale.domain.vocabulary.dto.request;

import com.moretale.domain.vocabulary.entity.VocabularyEntry.LearningStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VocabularyPatchRequest {

    // null이면 변경 안 함
    private Boolean isFavorite;
    private LearningStatus learningStatus;
    private String memo;
}
