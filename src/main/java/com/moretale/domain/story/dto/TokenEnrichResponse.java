package com.moretale.domain.story.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenEnrichResponse {

    private String word;
    private String translation;   // 번역어
    private String definition;    // 뜻 설명 (한국어)
}
