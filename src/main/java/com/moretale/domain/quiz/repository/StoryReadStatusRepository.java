package com.moretale.domain.quiz.repository;

import com.moretale.domain.quiz.entity.StoryReadStatus;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoryReadStatusRepository extends JpaRepository<StoryReadStatus, Long> {

    // 사용자 + 동화로 완독 상태 조회
    Optional<StoryReadStatus> findByUserAndStory(User user, Story story);

    // 완독 상태 존재 여부 확인
    boolean existsByUserAndStory(User user, Story story);
}
