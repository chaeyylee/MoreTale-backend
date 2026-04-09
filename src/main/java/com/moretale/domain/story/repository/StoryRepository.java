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

    // 특정 사용자가 만든 동화 목록 조회 (최신순 고정 - 내부 사용)
    List<Story> findByUserOrderByCreatedAtDesc(User user);

    // 공개된 동화 목록 조회 (최신순 고정)
    List<Story> findByIsPublicTrueOrderByCreatedAtDesc();

    // 특정 동화 ID와 사용자로 조회 (권한 체크용)
    Optional<Story> findByStoryIdAndUser(Long storyId, User user);

    // 슬라이드 Fetch Join (상세 조회용 - MultipleBagFetchException 방지)
    @Query("""
        SELECT DISTINCT s FROM Story s
        LEFT JOIN FETCH s.slides sl
        WHERE s.storyId = :storyId
    """)
    Optional<Story> findByIdWithSlides(@Param("storyId") Long storyId);

    // 사용자별 동화 개수
    long countByUser(User user);

    /**
     * 도서관 목록 조회 - Pageable 정렬 파라미터 지원
     * 프론트 정렬 UI 매핑:
     *   최신순    → sort=createdAt,desc
     *   오래된순  → sort=createdAt,asc
     *   가나다순  → sort=title,asc
     *
     * 슬라이드를 LEFT JOIN FETCH로 함께 가져와 thumbnail 추출 가능
     * COUNT 쿼리는 별도로 분리하여 성능 최적화
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

    // 도서관 목록 조회 - 슬라이드 없이 (경량 버전, 썸네일 불필요 시)
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
