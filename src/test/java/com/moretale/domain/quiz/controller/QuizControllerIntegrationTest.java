package com.moretale.domain.quiz.controller;

import com.moretale.domain.honeyjar.entity.HoneyJar;
import com.moretale.domain.honeyjar.repository.HoneyJarRepository;
import com.moretale.domain.profile.entity.UserProfile;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.quiz.dto.QuizSubmitRequest;
import com.moretale.domain.quiz.dto.StoryCompleteRequest;
import com.moretale.domain.quiz.entity.*;
import com.moretale.domain.quiz.repository.QuizRepository;
import com.moretale.domain.quiz.repository.StoryReadStatusRepository;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.repository.StoryRepository;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.global.security.UserPrincipal;
import com.moretale.support.IntegrationTestSupport;
import com.moretale.support.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Quiz Controller 통합 테스트")
class QuizControllerIntegrationTest extends IntegrationTestSupport {

    @Autowired UserRepository userRepository;
    @Autowired UserProfileRepository userProfileRepository;
    @Autowired StoryRepository storyRepository;
    @Autowired QuizRepository quizRepository;
    @Autowired StoryReadStatusRepository storyReadStatusRepository;
    @Autowired HoneyJarRepository honeyJarRepository;

    private User testUser;
    private UserProfile testProfile;
    private Story testStory;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(TestFixture.createUser("quiz-test@example.com"));
        testProfile = userProfileRepository.save(TestFixture.createProfile(testUser));
        testStory = storyRepository.save(
                TestFixture.createStory(testUser, testProfile, "흥부와 놀부"));
        setAuthentication(testUser);
    }

    // ────────────────── POST /api/quiz/submit ──────────────────

    @Test
    @DisplayName("퀴즈 제출 - 100점 → 꿀단지 1개 지급")
    void submitQuiz_perfectScore_earnHoneyJar() throws Exception {
        Quiz quiz = saveQuizWithQuestions(testStory, 2);

        List<QuizSubmitRequest.AnswerDto> answers = quiz.getQuestions().stream()
                .map(q -> new QuizSubmitRequest.AnswerDto(
                        q.getQuestionId(), q.getCorrectAnswer()))
                .toList();

        QuizSubmitRequest request = QuizSubmitRequest.builder()
                .quizId(quiz.getQuizId())
                .answers(answers)
                .build();

        mockMvc.perform(post("/api/quiz/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.score").value(100))
                .andExpect(jsonPath("$.data.isPerfect").value(true))
                .andExpect(jsonPath("$.data.honeyJarReward.earnedHoneyJars").value(1))
                .andExpect(jsonPath("$.data.resultMessage").value("🎉 완벽해요! 모든 문제를 맞혔어요!"));

        HoneyJar honeyJar = honeyJarRepository.findByUser(testUser).orElseThrow();
        assertThat(honeyJar.getCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("퀴즈 제출 - 전부 오답 → 0점, 꿀단지 미지급")
    void submitQuiz_zeroScore_noHoneyJar() throws Exception {
        Quiz quiz = saveQuizWithQuestions(testStory, 2);

        List<QuizSubmitRequest.AnswerDto> wrongAnswers = quiz.getQuestions().stream()
                .map(q -> new QuizSubmitRequest.AnswerDto(q.getQuestionId(), "9"))
                .toList();

        QuizSubmitRequest request = QuizSubmitRequest.builder()
                .quizId(quiz.getQuizId())
                .answers(wrongAnswers)
                .build();

        mockMvc.perform(post("/api/quiz/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.score").value(0))
                .andExpect(jsonPath("$.data.isPerfect").value(false))
                .andExpect(jsonPath("$.data.honeyJarReward.earnedHoneyJars").value(0));
    }

    @Test
    @DisplayName("퀴즈 제출 - 문항별 정오 결과 포함 응답")
    void submitQuiz_returnsAnswerResults() throws Exception {
        Quiz quiz = saveQuizWithQuestions(testStory, 2);

        QuizSubmitRequest.AnswerDto correct = new QuizSubmitRequest.AnswerDto(
                quiz.getQuestions().get(0).getQuestionId(),
                quiz.getQuestions().get(0).getCorrectAnswer());
        QuizSubmitRequest.AnswerDto wrong = new QuizSubmitRequest.AnswerDto(
                quiz.getQuestions().get(1).getQuestionId(), "9");

        QuizSubmitRequest request = QuizSubmitRequest.builder()
                .quizId(quiz.getQuizId())
                .answers(List.of(correct, wrong))
                .build();

        mockMvc.perform(post("/api/quiz/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.answerResults", hasSize(2)))
                .andExpect(jsonPath("$.data.answerResults[0].isCorrect").value(true))
                .andExpect(jsonPath("$.data.answerResults[1].isCorrect").value(false));
    }

    @Test
    @DisplayName("퀴즈 제출 - 퀴즈 없음 → 404")
    void submitQuiz_quizNotFound_404() throws Exception {
        QuizSubmitRequest request = QuizSubmitRequest.builder()
                .quizId(9999L)
                .answers(List.of(new QuizSubmitRequest.AnswerDto(1L, "1")))
                .build();

        mockMvc.perform(post("/api/quiz/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("Q001"));
    }

    // ────────────────── POST /api/quiz/story-complete ──────────────────

    @Test
    @DisplayName("동화 완독 - 최초 완독 → 꿀단지 +1")
    void completeStory_firstTime_earnHoneyJar() throws Exception {
        StoryCompleteRequest request = new StoryCompleteRequest(testStory.getStoryId());

        mockMvc.perform(post("/api/quiz/story-complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.earnedHoneyJars").value(1))
                .andExpect(jsonPath("$.data.currentHoneyJarCount").value(1));

        HoneyJar honeyJar = honeyJarRepository.findByUser(testUser).orElseThrow();
        assertThat(honeyJar.getCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("동화 완독 - 이미 완독 보상 받음 → 중복 지급 없음")
    void completeStory_alreadyRewarded_noDouble() throws Exception {
        StoryReadStatus readStatus = StoryReadStatus.builder()
                .user(testUser).story(testStory)
                .isCompleted(true).readHoneyJarRewarded(true).build();
        storyReadStatusRepository.save(readStatus);

        HoneyJar honeyJar = HoneyJar.builder()
                .user(testUser).count(2).totalEarned(2).totalUsed(0).build();
        honeyJarRepository.save(honeyJar);

        StoryCompleteRequest request = new StoryCompleteRequest(testStory.getStoryId());

        mockMvc.perform(post("/api/quiz/story-complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.earnedHoneyJars").value(0))
                .andExpect(jsonPath("$.data.currentHoneyJarCount").value(2));
    }

    @Test
    @DisplayName("동화 완독 - 9개 보유 후 완독 → 10개 달성 자동 차감")
    void completeStory_reaches10_autoDeduct() throws Exception {
        HoneyJar honeyJar = HoneyJar.builder()
                .user(testUser).count(9).totalEarned(9).totalUsed(0).build();
        honeyJarRepository.save(honeyJar);

        StoryCompleteRequest request = new StoryCompleteRequest(testStory.getStoryId());

        mockMvc.perform(post("/api/quiz/story-complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.autoUsedForFreeGeneration").value(true));

        HoneyJar updated = honeyJarRepository.findByUser(testUser).orElseThrow();
        assertThat(updated.getCount()).isEqualTo(0);
    }

    // ────────────────── 헬퍼 ──────────────────

    private Quiz saveQuizWithQuestions(Story story, int count) {
        Quiz quiz = Quiz.builder()
                .story(story).language("ko")
                .difficulty(QuizDifficulty.NORMAL)
                .totalQuestions(count)
                .build();

        for (int i = 1; i <= count; i++) {
            QuizQuestion q = QuizQuestion.builder()
                    .questionType(QuestionType.MULTIPLE_CHOICE)
                    .evaluationType(EvaluationType.STORY)
                    .questionOrder(i)
                    .questionText("문제 " + i)
                    .correctAnswer(String.valueOf(i))
                    .explanation("해설 " + i)
                    .build();
            quiz.addQuestion(q);
        }

        return quizRepository.save(quiz);
    }

    private void setAuthentication(User user) {
        UserPrincipal principal = UserPrincipal.create(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()));
    }
}
