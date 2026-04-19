package com.moretale.domain.story.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenEnrichRequest {

    // 정규화된 단어 (예: "사자")
    private String word;

    // 문맥 (해당 슬라이드 전체 문장 - 번역 품질 향상용)
    private String context;
}
