package com.moretale.domain.story.service;

import com.moretale.domain.story.entity.Slide;
import com.moretale.domain.story.entity.StoryToken;

import java.util.List;

public interface StoryTokenService {

    /**
     * 슬라이드 텍스트를 토큰화하고 하이라이트 + enrichment 후 StoryToken 목록을 반환
     * 동화 저장 시점에 호출
     *
     * @param slide          대상 슬라이드
     * @param sourceLanguage 원문 언어 코드 (예: "ko")
     * @param targetLanguage 번역 대상 언어 코드 (예: "vi")
     * @return 생성된 StoryToken 목록
     */
    List<StoryToken> generateTokensForSlide(
            Slide slide,
            String sourceLanguage,
            String targetLanguage
    );
}
