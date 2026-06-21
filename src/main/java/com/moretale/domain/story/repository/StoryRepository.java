package com.moretale.domain.story.repository;

import com.moretale.domain.story.entity.Story;
import com.moretale.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {

    // 특정 사용자의 동화 목록 조회 (최신순)
    List<Story> findAllByUserOrderByCreatedAtDesc(User user);

    // userId 기반 동화 목록 조회 (최신순)
    @Query("SELECT s FROM Story s WHERE s.user.userId = :userId ORDER BY s.createdAt DESC")
    List<Story> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // 공개된 동화 목록 조회 (최신순)
    List<Story> findByIsPublicTrueOrderByCreatedAtDesc();

    // storyId + userId 기반 소유권 확인 조회
    @Query("SELECT s FROM Story s WHERE s.storyId = :storyId AND s.user.userId = :userId")
    Optional<Story> findByStoryIdAndUserId(
            @Param("storyId") Long storyId,
            @Param("userId") Long userId
    );

    // 슬라이드 Fetch Join (상세 조회용)
    @Query("""
        SELECT DISTINCT s FROM Story s
        LEFT JOIN FETCH s.slides sl
        WHERE s.storyId = :storyId
    """)
    Optional<Story> findByIdWithSlides(@Param("storyId") Long storyId);

    // 마이페이지용: 최근 생성 동화 N건 조회 (슬라이드 함께 패치)
    @Query("""
        SELECT DISTINCT s FROM Story s
        LEFT JOIN FETCH s.slides sl
        WHERE s.user = :user
        ORDER BY s.createdAt DESC
    """)
    List<Story> findRecentStoriesWithSlides(@Param("user") User user, Pageable pageable);

    // 특정 사용자의 전체 동화 수 조회
    long countByUser(User user);

    // 특정 사용자의 모든 동화 삭제 (회원 탈퇴용)
    void deleteAllByUser(User user);

    /**
     * 도서관 목록 조회 1단계 - story_id만 페이징 조회 (N+1 + 메모리 페이징 방지 핵심)
     *
     * ─ 기존 문제 ──────────────────────────────────────────────────────────────
     *   findByUserIdWithSlides(): LEFT JOIN FETCH s.slides + Pageable 조합
     *   → Hibernate가 전체 결과를 메모리에 올린 후 페이징 (HHH90003004 경고)
     *   → OOM 위험 + 불필요한 대량 데이터 로딩
     *
     * ─ 해결 방법 ──────────────────────────────────────────────────────────────
     *   1차 쿼리(findIdsByUserId): story_id만 OFFSET/LIMIT으로 정확히 페이징
     *   2차 쿼리(fetchByIdsWithSlides): IN 절로 slides까지 JOIN FETCH 일괄 조회
     *
     * ─ countQuery 분리 이유 ───────────────────────────────────────────────────
     *   fetch join 없이 COUNT만 하면 불필요한 JOIN 제거 → 성능 향상
     * ─────────────────────────────────────────────────────────────────────────
     */
    @Query(
            value = """
            SELECT s.storyId FROM Story s
            WHERE s.user.userId = :userId
            """,
            countQuery = """
            SELECT COUNT(s) FROM Story s
            WHERE s.user.userId = :userId
            """
    )
    Page<Long> findIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 도서관 목록 조회 2단계 - ID 목록으로 slides까지 JOIN FETCH 일괄 조회
     *
     * ─ IN 절 크기 = 한 페이지 크기(기본 20)이므로 성능 문제 없음
     * ─ DISTINCT: story - slide 1:N 조인으로 중복 story 행이 생길 수 있으므로 필수
     * ─ 순서 보장: IN 절은 DB 반환 순서 미보장 → Service에서 원래 ID 순서대로 재정렬
     */
    @Query("""
        SELECT DISTINCT s FROM Story s
        LEFT JOIN FETCH s.slides sl
        WHERE s.storyId IN :ids
    """)
    List<Story> fetchByIdsWithSlides(@Param("ids") List<Long> ids);

    // 도서관 목록 조회 - 슬라이드 없이 (경량 버전, 필요 시 사용)
    @Query("""
        SELECT s FROM Story s
        WHERE s.user.userId = :userId
    """)
    Page<Story> findByUserId(@Param("userId") Long userId, Pageable pageable);

    // 공개 동화 목록 조회 - Pageable 정렬 지원
    @Query("""
        SELECT s FROM Story s
        WHERE s.isPublic = true
    """)
    Page<Story> findPublicStories(Pageable pageable);

    /**
     * /api/stories/init 용 - recommendedTaleTitle 기준 조회
     * profile 객체의 profileId로 조회 (s.profileId → s.profile.profileId)
     */
    @Query("""
        SELECT s.storyId FROM Story s
        WHERE s.user.userId = :userId
          AND s.profile.profileId = :profileId
          AND s.title = :title
        ORDER BY s.createdAt DESC
    """)
    List<Long> findLatestStoryIdByProfileAndTitle(
            @Param("userId") Long userId,
            @Param("profileId") Long profileId,
            @Param("title") String title,
            Pageable pageable
    );

    /**
     * /api/stories/init 용 — fallback (profile_id = NULL 기존 데이터 대응)
     *
     * userId + title 기반으로 추천 전래동화와 일치하는 가장 최근 storyId 1건 조회
     * findLatestStoryIdByProfileAndTitle() 결과가 비어있을 때만 호출
     */
    @Query("""
        SELECT s.storyId FROM Story s
        WHERE s.user.userId = :userId
          AND s.title = :title
        ORDER BY s.createdAt DESC
    """)
    List<Long> findLatestStoryIdByUserAndTitle(
            @Param("userId") Long userId,
            @Param("title") String title,
            Pageable pageable
    );

    /**
     * /api/stories/init 용 — 공개 전래동화 fallback
     *
     * 현재 사용자에게 추천 전래동화가 없을 때,
     * 공개 처리된 동일 제목의 전래동화를 조회한다.
     */
    @Query("""
        SELECT s.storyId FROM Story s
        WHERE s.isPublic = true
          AND s.title = :title
        ORDER BY s.createdAt DESC
    """)
    List<Long> findLatestPublicStoryIdByTitle(
            @Param("title") String title,
            Pageable pageable
    );

    /**
     * 기존 메서드 - 사용하지 않지만 하위 호환을 위해 유지
     * @deprecated LibraryService.getLibrary()에서 더 이상 사용하지 않음
     */
    @Deprecated
    @Query(
            value = """
            SELECT DISTINCT s FROM Story s
            LEFT JOIN FETCH s.slides sl
            WHERE s.user.userId = :userId
            """,
            countQuery = """
            SELECT COUNT(s) FROM Story s
            WHERE s.user.userId = :userId
            """
    )
    Page<Story> findByUserIdWithSlides(@Param("userId") Long userId, Pageable pageable);
}
