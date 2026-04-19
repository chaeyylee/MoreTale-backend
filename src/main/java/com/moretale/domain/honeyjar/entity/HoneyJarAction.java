package com.moretale.domain.honeyjar.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "꿀단지 변동 유형 (EARN: 획득, USE: 사용)")
@Getter
@RequiredArgsConstructor
public enum HoneyJarAction {
    EARN_STORY_COMPLETE("동화 완독 보상"),    // 동화 완독 시 +1
    EARN_QUIZ_PERFECT("퀴즈 100점 보상"),     // 퀴즈 100점 시 +1
    USE_FREE_GENERATION("동화 무료 생성 사용"); // 동화 무료 생성 -20

    private final String description;
}
