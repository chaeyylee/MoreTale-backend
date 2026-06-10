package com.moretale.domain.quiz.service;

import com.moretale.domain.honeyjar.service.HoneyJarService;
import com.moretale.domain.profile.entity.*;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.quiz.dto.QuizResultResponse;
import com.moretale.domain.quiz.dto.QuizSubmitRequest;
import com.moretale.domain.quiz.entity.*;
import com.moretale.domain.quiz.repository.*;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.repository.StoryRepository;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuizService 단위 테스트")
class QuizServiceTest {

    @Mock QuizRepository quizRepository;
    @Mock QuizResultRepository quizResultRepository;
    @Mock StoryReadStatusRepository storyReadStatusRepository;
    @Mock StoryRepository storyRepository;
    @Mock UserRepository userRepository;
    @Mock UserProfileRepository userProfileRepository;
    @Mock AIQuizService aiQuizService;
    @Mock HoneyJarService honeyJarService;

    @InjectMocks QuizService quizService;

    private User testUser;
    private Story testStory;
    private UserProfile testProfile;

    @BeforeEach
    void setUp() {
        testUser = User.builder().userId(1L).email("test@example.com").build();

        testProfile = UserProfile.builder()
                .profileId(10L)
                .user(testUser)
                .childName("민준")
                .ageGroup(AgeGroup.AGE_5_6)
                .childAge(5)
                .firstLanguage(Language.KO)
                .firstLanguageProficiency(LanguageProficiency.BEE)
                .secondLanguage(Language.VI)
                .secondLanguageProficiency(LanguageProficiency.LARVA)
                .firstLanguageListening(LanguageProficiency.BEE)
                .firstLanguageSpeaking(LanguageProficiency.PUPA)
                .secondLanguageListening(LanguageProficiency.LARVA)
                .secondLanguageSpeaking(LanguageProficiency.EGG)
                .familyStructure(FamilyStructure.TWO_PARENTS)
                .storyPreference(StoryPreference.FUN_ADVENTURE)
                .build();

        testStory = Story.builder()
                .storyId(5L)
                .title("흥부와 놀부")
                .user(testUser)
                .primaryLanguage("ko")
                .secondaryLanguage("vi")
                .isPublic(false)
                .build();
    }

    @Test
    @DisplayName("퀴즈 조회 - 기존 퀴즈 있으면 바로 반환")
    void getOrGenerateQuiz_existingQuiz_returnsCached() {
        Quiz existingQuiz = Quiz.builder()
                .quizId(1L)
                .story(testStory)
                .language("ko")
                .difficulty(QuizDifficulty.NORMAL)
                .totalQuestions(5)
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(storyRepository.findByIdWithSlides(5L)).willReturn(Optional.of(testStory));
        given(quizRepository.findByStoryWithQuestions(testStory))
                .willReturn(Optional.of(existingQuiz));

        var response = quizService.getOrGenerateQuiz(1L, 5L);

        assertThat(response.getQuizId()).isEqualTo(1L);
        // aiQuizService는 호출되지 않아야 함
    }

    @Test
    @DisplayName("퀴즈 조회 - 비공개 타인 동화 접근 → STORY_ACCESS_DENIED")
    void getOrGenerateQuiz_privateStory_accessDenied() {
        User otherUser = User.builder().userId(2L).build();
        Story privateStory = Story.builder()
                .storyId(99L)
                .user(otherUser)
                .isPublic(false)
                .primaryLanguage("ko")
                .secondaryLanguage("vi")
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(storyRepository.findByIdWithSlides(99L)).willReturn(Optional.of(privateStory));

        assertThatThrownBy(() -> quizService.getOrGenerateQuiz(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.STORY_ACCESS_DENIED);
    }

    @Test
    @DisplayName("퀴즈 제출 - 100점 달성 + 꿀단지 첫 보상")
    void submitQuiz_perfectScore_earnHoneyJar() {
        QuizQuestion q1 = buildQuestion(1L, 1, "1");
        QuizQuestion q2 = buildQuestion(2L, 2, "2");

        Quiz quiz = Quiz.builder()
                .quizId(1L)
                .story(testStory)
                .language("ko")
                .difficulty(QuizDifficulty.NORMAL)
                .totalQuestions(2)
                .build();
        quiz.addQuestion(q1);
        quiz.addQuestion(q2);

        StoryReadStatus readStatus = StoryReadStatus.builder()
                .user(testUser)
                .story(testStory)
                .quizHoneyJarRewarded(false)
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(quizRepository.findByIdWithQuestions(1L)).willReturn(Optional.of(quiz));
        given(storyReadStatusRepository.findByUserAndStory(testUser, testStory))
                .willReturn(Optional.of(readStatus));
        given(quizResultRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(honeyJarService.addHoneyJarAndCheckAutoUse(any(), any(), any()))
                .willReturn(false);
        given(honeyJarService.getOrCreateHoneyJar(testUser))
                .willReturn(com.moretale.domain.honeyjar.entity.HoneyJar.builder()
                        .user(testUser).count(1).totalEarned(1).totalUsed(0).build());

        QuizSubmitRequest request = QuizSubmitRequest.builder()
                .quizId(1L)
                .answers(List.of(
                        new QuizSubmitRequest.AnswerDto(1L, "1"),
                        new QuizSubmitRequest.AnswerDto(2L, "2")
                ))
                .build();

        QuizResultResponse response = quizService.submitQuiz(1L, request);

        assertThat(response.getScore()).isEqualTo(100);
        assertThat(response.getIsPerfect()).isTrue();
        assertThat(response.getHoneyJarReward().getEarnedHoneyJars()).isEqualTo(1);
        verify(honeyJarService).addHoneyJarAndCheckAutoUse(any(), any(), any());
    }

    @Test
    @DisplayName("퀴즈 제출 - 100점 미달 → 꿀단지 미지급")
    void submitQuiz_notPerfect_noHoneyJar() {
        QuizQuestion q1 = buildQuestion(1L, 1, "1");
        QuizQuestion q2 = buildQuestion(2L, 2, "2");

        Quiz quiz = Quiz.builder()
                .quizId(1L)
                .story(testStory)
                .language("ko")
                .difficulty(QuizDifficulty.NORMAL)
                .totalQuestions(2)
                .build();
        quiz.addQuestion(q1);
        quiz.addQuestion(q2);

        StoryReadStatus readStatus = StoryReadStatus.builder()
                .user(testUser)
                .story(testStory)
                .quizHoneyJarRewarded(false)
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(quizRepository.findByIdWithQuestions(1L)).willReturn(Optional.of(quiz));
        given(storyReadStatusRepository.findByUserAndStory(testUser, testStory))
                .willReturn(Optional.of(readStatus));
        given(quizResultRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(honeyJarService.getOrCreateHoneyJar(testUser))
                .willReturn(com.moretale.domain.honeyjar.entity.HoneyJar.builder()
                        .user(testUser).count(0).totalEarned(0).totalUsed(0).build());

        QuizSubmitRequest request = QuizSubmitRequest.builder()
                .quizId(1L)
                .answers(List.of(
                        new QuizSubmitRequest.AnswerDto(1L, "1"),  // 정답
                        new QuizSubmitRequest.AnswerDto(2L, "3")   // 오답
                ))
                .build();

        QuizResultResponse response = quizService.submitQuiz(1L, request);

        assertThat(response.getScore()).isEqualTo(50);
        assertThat(response.getIsPerfect()).isFalse();
        assertThat(response.getHoneyJarReward().getEarnedHoneyJars()).isEqualTo(0);
    }

    @Test
    @DisplayName("퀴즈 제출 - 이미 보상 받은 경우 중복 지급 안 함")
    void submitQuiz_alreadyRewarded_noDoubleReward() {
        QuizQuestion q1 = buildQuestion(1L, 1, "1");

        Quiz quiz = Quiz.builder()
                .quizId(1L)
                .story(testStory)
                .language("ko")
                .difficulty(QuizDifficulty.NORMAL)
                .totalQuestions(1)
                .build();
        quiz.addQuestion(q1);

        StoryReadStatus readStatus = StoryReadStatus.builder()
                .user(testUser)
                .story(testStory)
                .quizHoneyJarRewarded(true) // 이미 받음
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(quizRepository.findByIdWithQuestions(1L)).willReturn(Optional.of(quiz));
        given(storyReadStatusRepository.findByUserAndStory(testUser, testStory))
                .willReturn(Optional.of(readStatus));
        given(quizResultRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(honeyJarService.getOrCreateHoneyJar(testUser))
                .willReturn(com.moretale.domain.honeyjar.entity.HoneyJar.builder()
                        .user(testUser).count(3).totalEarned(3).totalUsed(0).build());

        QuizSubmitRequest request = QuizSubmitRequest.builder()
                .quizId(1L)
                .answers(List.of(new QuizSubmitRequest.AnswerDto(1L, "1")))
                .build();

        QuizResultResponse response = quizService.submitQuiz(1L, request);

        assertThat(response.getHoneyJarReward().getEarnedHoneyJars()).isEqualTo(0);
    }

    @Test
    @DisplayName("동화 완독 - 최초 완독 시 꿀단지 +1 지급")
    void completeStory_firstTime_earnHoneyJar() {
        StoryReadStatus readStatus = StoryReadStatus.builder()
                .user(testUser)
                .story(testStory)
                .readHoneyJarRewarded(false)
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(storyRepository.findByIdWithSlides(5L)).willReturn(Optional.of(testStory));
        given(storyReadStatusRepository.findByUserAndStory(testUser, testStory))
                .willReturn(Optional.of(readStatus));
        given(storyReadStatusRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(honeyJarService.addHoneyJarAndCheckAutoUse(any(), any(), any())).willReturn(false);
        given(honeyJarService.getOrCreateHoneyJar(testUser))
                .willReturn(com.moretale.domain.honeyjar.entity.HoneyJar.builder()
                        .user(testUser).count(1).totalEarned(1).totalUsed(0).build());

        QuizResultResponse.HoneyJarRewardInfo reward =
                quizService.completeStory(1L, 5L);

        assertThat(reward.getEarnedHoneyJars()).isEqualTo(1);
        verify(honeyJarService).addHoneyJarAndCheckAutoUse(any(), any(), eq(5L));
    }

    @Test
    @DisplayName("동화 완독 - 이미 완독 보상 받음 → 중복 지급 없음")
    void completeStory_alreadyRewarded_noDoubleReward() {
        StoryReadStatus readStatus = StoryReadStatus.builder()
                .user(testUser)
                .story(testStory)
                .readHoneyJarRewarded(true)
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(storyRepository.findByIdWithSlides(5L)).willReturn(Optional.of(testStory));
        given(storyReadStatusRepository.findByUserAndStory(testUser, testStory))
                .willReturn(Optional.of(readStatus));
        given(honeyJarService.getOrCreateHoneyJar(testUser))
                .willReturn(com.moretale.domain.honeyjar.entity.HoneyJar.builder()
                        .user(testUser).count(2).totalEarned(2).totalUsed(0).build());

        QuizResultResponse.HoneyJarRewardInfo reward =
                quizService.completeStory(1L, 5L);

        assertThat(reward.getEarnedHoneyJars()).isEqualTo(0);
    }

    private QuizQuestion buildQuestion(Long id, int order, String correctAnswer) {
        return QuizQuestion.builder()
                .questionId(id)
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .evaluationType(EvaluationType.STORY)
                .questionOrder(order)
                .questionText("문제 " + order)
                .correctAnswer(correctAnswer)
                .explanation("해설 " + order)
                .build();
    }
}
