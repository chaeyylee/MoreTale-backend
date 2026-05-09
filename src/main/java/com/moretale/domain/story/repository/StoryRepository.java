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
     * 도서관 목록 조회 - Pageable 정렬 파라미터 지원
     * 프론트 정렬 UI 매핑:
     *   최신순    → sort=createdAt,desc
     *   오래된순  → sort=createdAt,asc
     *   가나다순  → sort=title,asc
     */
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

    // 도서관 목록 조회 - 슬라이드 없이 (경량 버전)
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
}
