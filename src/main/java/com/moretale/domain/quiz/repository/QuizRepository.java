package com.moretale.domain.quiz.repository;

import com.moretale.domain.quiz.entity.Quiz;
import com.moretale.domain.story.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Quiz 엔티티에 대한 데이터 액세스 레포지토리
@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    /**
     * 동화(Story)를 기준으로 퀴즈 세트를 조회합니다.
     * MultipleBagFetchException 해결을 위해 qq.options에 대한 FETCH JOIN을 제거했습니다.
     * 질문(questions) 리스트는 FETCH JOIN으로 가져오고, 각 질문의 보기(options)는
     * application.yml의 default_batch_fetch_size 설정을 통해 배치 조회됩니다.
     */
    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN FETCH q.questions qq " +
            "WHERE q.story = :story")
    Optional<Quiz> findByStoryWithQuestions(@Param("story") Story story);

    /**
     * 퀴즈 ID를 기준으로 퀴즈 세트를 조회합니다. (채점 시 사용)
     * MultipleBagFetchException 방지를 위해 qq.options FETCH JOIN을 제거했습니다.
     */
    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN FETCH q.questions qq " +
            "WHERE q.quizId = :quizId")
    Optional<Quiz> findByIdWithQuestions(@Param("quizId") Long quizId);

    // 특정 동화에 이미 생성된 퀴즈가 있는지 확인
    boolean existsByStory(Story story);

    // 동화 엔티티를 기준으로 퀴즈를 조회 (페치 조인 없는 단순 조회)
    Optional<Quiz> findByStory(Story story);
}
