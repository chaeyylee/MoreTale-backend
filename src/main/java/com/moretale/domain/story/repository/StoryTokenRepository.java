package com.moretale.domain.story.repository;

import com.moretale.domain.story.entity.StoryToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryTokenRepository extends JpaRepository<StoryToken, Long> {

    // 슬라이드 ID로 토큰 목록 조회 (순서대로)
    List<StoryToken> findBySlideSlideIdOrderByTokenOrderAsc(Long slideId);

    // 특정 슬라이드의 하이라이트 토큰만 조회
    List<StoryToken> findBySlideSlideIdAndHighlightTrueOrderByTokenOrderAsc(Long slideId);

    // 슬라이드 ID 목록으로 토큰 일괄 조회 (N+1 방지)
    @Query("""
        SELECT t FROM StoryToken t
        WHERE t.slide.slideId IN :slideIds
        ORDER BY t.slide.slideId, t.tokenOrder ASC
    """)
    List<StoryToken> findBySlideIds(@Param("slideIds") List<Long> slideIds);

    // 슬라이드 토큰 전체 삭제
    void deleteBySlideSlideId(Long slideId);
}
