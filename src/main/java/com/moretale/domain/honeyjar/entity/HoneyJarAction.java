package com.moretale.domain.honeyjar.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HoneyJarAction {
    EARN_STORY_COMPLETE("동화 완독 보상"),    // 동화 완독 시 +1
    EARN_QUIZ_PERFECT("퀴즈 100점 보상"),     // 퀴즈 100점 시 +1
    USE_FREE_GENERATION("동화 무료 생성 사용"); // 동화 무료 생성 -20

    private final String description;
}
