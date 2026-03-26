package com.moretale.domain.story.service;

import com.moretale.domain.story.dto.TokenEnrichRequest;
import com.moretale.domain.story.dto.TokenEnrichResponse;

import java.util.List;

public interface AITokenService {

    // 하이라이트 단어들에 대해 번역어와 뜻 설명을 일괄 생성
    // AI API를 호출하여 sourceLanguage → targetLanguage 번역과 정의 반환
    List<TokenEnrichResponse> enrichTokens(
            List<TokenEnrichRequest> tokens,
            String sourceLanguage,
            String targetLanguage
    );
}
