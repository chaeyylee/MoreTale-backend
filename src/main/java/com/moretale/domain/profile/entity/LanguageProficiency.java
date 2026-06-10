package com.moretale.domain.profile.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "언어 숙련도 (EGG: 기초 → BEE: 고급)")
@Getter
@RequiredArgsConstructor
public enum LanguageProficiency {
    EGG("알 (기초)", 0),
    LARVA("애벌레 (초급)", 1),
    PUPA("번데기 (중급)", 2),
    BEE("꿀벌 (고급)", 3);

    private final String description;
    private final int level;

    public static LanguageProficiency fromLegacy(String legacy) {
        return switch (legacy.toUpperCase()) {
            case "CATERPILLAR" -> LARVA;
            case "CHRYSALIS" -> PUPA;
            case "BUTTERFLY" -> BEE;
            default -> LARVA;
        };
    }
}
