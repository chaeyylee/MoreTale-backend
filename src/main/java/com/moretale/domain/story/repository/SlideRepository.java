package com.moretale.domain.story.repository;

import com.moretale.domain.story.entity.Slide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlideRepository extends JpaRepository<Slide, Long> {

    List<Slide> findByStoryStoryIdOrderByOrderAsc(Long storyId);

    @Query("SELECT s FROM Slide s WHERE s.story.storyId = :storyId ORDER BY s.order ASC")
    List<Slide> findByStoryIdOrderByOrder(@Param("storyId") Long storyId);

    // 토큰까지 함께 fetch (상세 조회용)
    @Query("""
        SELECT DISTINCT s FROM Slide s
        LEFT JOIN FETCH s.tokens t
        WHERE s.story.storyId = :storyId
        ORDER BY s.order ASC
    """)
    List<Slide> findByStoryIdWithTokens(@Param("storyId") Long storyId);

    @Query("""
        SELECT s FROM Slide s
        WHERE s.story.storyId = :storyId
          AND (s.audioUrlKr IS NULL OR s.audioUrlNative IS NULL)
    """)
    List<Slide> findSlidesWithoutTTS(@Param("storyId") Long storyId);

    // 특정 동화에 속한 모든 슬라이드 삭제
    void deleteByStoryStoryId(Long storyId);
}
