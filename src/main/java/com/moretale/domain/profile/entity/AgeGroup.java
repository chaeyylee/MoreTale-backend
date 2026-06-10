package com.moretale.domain.profile.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "나이 그룹 (AGE_0_2 ~ AGE_10_PLUS)")
@Getter
@RequiredArgsConstructor
public enum AgeGroup {
    AGE_0_2("0-2세", 1),
    AGE_3_4("3-4세", 3),
    AGE_5_6("5-6세", 5),
    AGE_7_8("7-8세", 7),
    AGE_9_10("9-10세", 9),
    AGE_10_PLUS("10세 이상", 10);

    private final String description;
    private final int representativeAge;
}
