package com.moretale.domain.story.service;

import java.util.List;

public interface TokenizationService {

    /**
     * 한국어 텍스트를 어절 단위로 토큰화
     * 예: "민준이는 사자를 봤어요." → ["민준이는", "사자를", "봤어요"]
     */
    List<String> tokenize(String text);

    /**
     * 어절에서 조사/어미를 제거하여 기본형으로 정규화
     * 예: "사자를" → "사자", "봤어요" → "봤어요" (동사는 그대로)
     */
    String normalize(String token);

    /**
     * 하이라이트 대상 단어를 선정
     * 전체 토큰 목록 중 명사 위주의 핵심 단어를 최대 maxCount개 선정
     */
    List<String> selectHighlightWords(List<String> normalizedTokens, int maxCount);
}
