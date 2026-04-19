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
public class TokenEnrichResponse {

    private String word;
    private String translation;   // 번역어
    private String definition;    // 뜻 설명 (한국어)
}
