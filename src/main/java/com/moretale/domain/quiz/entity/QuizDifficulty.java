package com.moretale.domain.quiz.entity;

import com.moretale.domain.profile.entity.AgeGroup;
import com.moretale.domain.profile.entity.LanguageProficiency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "퀴즈 난이도 (연령 + 언어 숙련도 기반 자동 결정)")
@Getter
@RequiredArgsConstructor
public enum QuizDifficulty {
    EASY("쉬움", 5),      // 0~4세, EGG/LARVA
    NORMAL("보통", 7),    // 5~8세, LARVA/PUPA
    HARD("어려움", 10);   // 9세 이상, PUPA/BEE

    private final String description;
    private final int questionCount; // 난이도별 문제 수

    // 연령 그룹 + 언어 숙련도 → 난이도 결정
    public static QuizDifficulty from(AgeGroup ageGroup, LanguageProficiency proficiency) {
        int ageScore = ageGroup.getRepresentativeAge();
        int langScore = proficiency.getLevel();

        // 복합 점수: 나이 가중치 60% + 언어 숙련도 40%
        double compositeScore = ageScore * 0.6 + langScore * 3 * 0.4;

        if (compositeScore <= 3.0) return EASY;
        if (compositeScore <= 6.0) return NORMAL;
        return HARD;
    }
}
