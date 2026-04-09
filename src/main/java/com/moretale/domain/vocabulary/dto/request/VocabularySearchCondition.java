package com.moretale.domain.vocabulary.dto.request;

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
public class VocabularySearchCondition {

    private Long storyId;           // null이면 전체
    private Boolean favorite;       // null이면 전체, true이면 즐겨찾기만
    private String keyword;         // null 또는 blank이면 검색 안 함
}
