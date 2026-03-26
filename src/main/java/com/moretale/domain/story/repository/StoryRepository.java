package com.moretale.domain.story.repository;

import com.moretale.domain.story.entity.Story;
import com.moretale.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {

    // 특정 사용자가 만든 동화 목록 조회
    List<Story> findByUserOrderByCreatedAtDesc(User user);

    // 공개된 동화 목록 조회
    List<Story> findByIsPublicTrueOrderByCreatedAtDesc();

    // 특정 동화 ID와 사용자로 조회 (권한 체크용)
    Optional<Story> findByStoryIdAndUser(Long storyId, User user);

    // sl.tokens에 대한 Fetch Join을 제거하여 MultipleBagFetchException을 방지
    @Query("""
        SELECT DISTINCT s FROM Story s
        LEFT JOIN FETCH s.slides sl
        WHERE s.storyId = :storyId
    """)
    Optional<Story> findByIdWithSlides(@Param("storyId") Long storyId);

    // 사용자별 동화 개수
    long countByUser(User user);
}
