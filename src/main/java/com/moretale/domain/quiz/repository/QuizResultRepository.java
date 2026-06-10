package com.moretale.domain.quiz.repository;

import com.moretale.domain.quiz.entity.Quiz;
import com.moretale.domain.quiz.entity.QuizResult;
import com.moretale.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    // 사용자 + 퀴즈로 가장 최근 결과 조회
    Optional<QuizResult> findTopByUserAndQuizOrderBySubmittedAtDesc(User user, Quiz quiz);

    // 사용자 + 퀴즈로 100점 달성 여부 확인
    boolean existsByUserAndQuizAndIsPerfectTrue(User user, Quiz quiz);

    // 사용자의 퀴즈 응시 이력 전체 조회
    List<QuizResult> findByUserOrderBySubmittedAtDesc(User user);

    // 특정 퀴즈에서 사용자의 응시 횟수
    @Query("SELECT COUNT(r) FROM QuizResult r WHERE r.user = :user AND r.quiz = :quiz")
    int countByUserAndQuiz(@Param("user") User user, @Param("quiz") Quiz quiz);
}
