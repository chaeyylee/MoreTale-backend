package com.moretale.domain.story.service;

import com.moretale.domain.story.dto.VocabularyItem;
import com.moretale.domain.story.entity.Slide;
import com.moretale.domain.story.entity.StoryToken;

import java.util.List;

public interface StoryTokenService {

    /**
     * AI vocabulary 기반으로 SlideToken 목록 생성.
     *
     * vocabulary가 존재하면 AI가 지정한 핵심 단어를 그대로 사용한다.
     * vocabulary가 null이거나 비어있으면 빈 리스트를 반환한다.
     * (형태소 분석기 fallback 제거 — AI 파트가 단어 생성을 전담)
     *
     * @param slide          대상 슬라이드
     * @param vocabulary     AI가 생성한 핵심 단어 목록 (null 허용)
     * @param sourceLanguage 제1언어 코드 (예: "ko")
     * @param targetLanguage 제2언어 코드 (예: "vi")
     * @return 생성된 StoryToken 목록
     */
    List<StoryToken> generateTokensForSlide(
            Slide slide,
            List<VocabularyItem> vocabulary,
            String sourceLanguage,
            String targetLanguage
    );
}
