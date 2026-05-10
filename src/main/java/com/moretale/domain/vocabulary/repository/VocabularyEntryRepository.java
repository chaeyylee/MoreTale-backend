package com.moretale.domain.vocabulary.repository;

import com.moretale.domain.story.entity.Story;
import com.moretale.domain.vocabulary.entity.VocabularyEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VocabularyEntryRepository extends JpaRepository<VocabularyEntry, Long> {

    // 내 전체 단어장 조회 (페이징) - Pageable에 isFavorite DESC가 포함되어 들어옴
    Page<VocabularyEntry> findByUser_UserId(Long userId, Pageable pageable);

    // 특정 동화 기준 단어장 조회 (페이징)
    Page<VocabularyEntry> findByUser_UserIdAndStory_StoryId(Long userId, Long storyId, Pageable pageable);

    // 단어가 저장된 동화 목록 조회 (중복 제거)
    @Query("""
        SELECT DISTINCT v.story
        FROM VocabularyEntry v
        WHERE v.user.userId = :userId
        ORDER BY v.story.createdAt DESC
        """)
    List<Story> findDistinctStoriesByUserId(@Param("userId") Long userId);

    // 단일 항목 조회 (소유권 확인용)
    Optional<VocabularyEntry> findByVocabularyIdAndUser_UserId(Long vocabularyId, Long userId);

    // 중복 저장 여부 확인 (같은 사용자 + 같은 동화 + 같은 정규화 단어)
    boolean existsByUser_UserIdAndStory_StoryIdAndNormalizedWord(
            Long userId, Long storyId, String normalizedWord
    );

    // 특정 동화의 단어 수 조회
    long countByUser_UserIdAndStory_StoryId(Long userId, Long storyId);

    // Story 삭제 시 연관된 단어장 전체 삭제용
    void deleteAllByStory(Story story);

    /**
     * 통합 필터 조회 (즐겨찾기 + 키워드 + 동화 필터 조합)
     *
     * ─ keyword 처리 방식 변경 ────────────────────────────────────────────────
     *   기존: LIKE CONCAT('%', :keyword, '%') — PostgreSQL + Hibernate 6 조합에서
     *         keyword=null 시 파라미터 타입을 bytea로 추론하여 타입 오류 발생
     *
     *   변경: :keywordPattern 으로 받고, Service에서 null → null, 값 → "%값%" 변환
     *         IS NULL 분기와 LIKE 절의 파라미터를 분리하여 타입 충돌 방지
     * ─────────────────────────────────────────────────────────────────────────
     *
     * @param userId         사용자 ID (필수)
     * @param storyId        동화 ID 필터 (null이면 전체)
     * @param favorite       즐겨찾기 필터 (null이면 전체, true이면 즐겨찾기만)
     * @param keywordPattern LIKE 패턴 문자열 (null이면 전체, 값이면 "%keyword%")
     * @param pageable       페이징 + 정렬 (isFavorite DESC가 선행 적용된 Pageable)
     */
    @Query("""
    SELECT v FROM VocabularyEntry v
    WHERE v.user.userId = :userId
      AND (:storyId IS NULL OR v.story.storyId = :storyId)
      AND (:favorite IS NULL OR v.isFavorite = :favorite)
      AND (:keywordPattern IS NULL
            OR v.word LIKE :keywordPattern
            OR v.translation LIKE :keywordPattern)
""")
    Page<VocabularyEntry> findWithFilters(
            @Param("userId") Long userId,
            @Param("storyId") Long storyId,
            @Param("favorite") Boolean favorite,
            @Param("keywordPattern") String keywordPattern,
            Pageable pageable
    );

    // 즐겨찾기 단어 전체 조회 (페이징)
    Page<VocabularyEntry> findByUser_UserIdAndIsFavoriteTrue(Long userId, Pageable pageable);

    // 특정 동화 + 즐겨찾기 조회 (페이징)
    Page<VocabularyEntry> findByUser_UserIdAndStory_StoryIdAndIsFavoriteTrue(
            Long userId, Long storyId, Pageable pageable
    );

    // 단어 검색 (word 또는 translation 포함, 페이징)
    @Query("""
        SELECT v FROM VocabularyEntry v
        WHERE v.user.userId = :userId
          AND (
                v.word LIKE CONCAT('%', :keyword, '%')
                OR v.translation LIKE CONCAT('%', :keyword, '%')
              )
    """)
    Page<VocabularyEntry> searchByKeyword(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 특정 동화에서 단어 검색 (페이징)
    @Query("""
        SELECT v FROM VocabularyEntry v
        WHERE v.user.userId = :userId
          AND v.story.storyId = :storyId
          AND (
                v.word LIKE CONCAT('%', :keyword, '%')
                OR v.translation LIKE CONCAT('%', :keyword, '%')
              )
    """)
    Page<VocabularyEntry> searchByKeywordAndStory(
            @Param("userId") Long userId,
            @Param("storyId") Long storyId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
