package com.moretale.domain.vocabulary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 단어장 조회 필터 조건
 * - favorite=true : 즐겨찾기 단어만 조회
 * - keyword       : 단어(word) 또는 번역어(translation) 포함 검색
 * - storyId       : 특정 동화 기준 필터
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "단어장 조회 필터 조건 DTO")
public class VocabularySearchCondition {

    @Schema(description = "특정 동화 기준 필터 (미입력 시 전체)", example = "5")
    private Long storyId;

    @Schema(description = "즐겨찾기 여부 필터 (true면 즐겨찾기만, 미입력 시 전체)", example = "true")
    private Boolean favorite;

    @Schema(description = "단어 원문 또는 번역어 검색 키워드", example = "사자")
    private String keyword;
}
