package com.moretale.domain.quiz.controller;

import com.moretale.domain.quiz.dto.*;
import com.moretale.domain.quiz.service.QuizService;
import com.moretale.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Quiz", description = "퀴즈 & 꿀단지 보상 API")
@Slf4j
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    /**
     * 퀴즈 조회 (없으면 자동 생성)
     * GET /api/quiz?storyId={storyId}
     *
     * - 동화 1권당 퀴즈 1세트 자동 생성
     * - 이미 생성된 경우 기존 퀴즈 반환
     * - 응답에 correctAnswer, explanation 포함
     * - 프론트에서 선택 즉시 정답/오답 피드백 처리 가능
     * - 보상 로직은 기존과 동일하게 submit 시점에서만 실행
     */
    @Operation(
            summary = "퀴즈 조회",
            description = """
                    동화에 연결된 퀴즈를 조회합니다.
                    퀴즈가 없으면 AI가 자동으로 생성합니다.
                    
                    - 각 문제에 `correctAnswer`와 `explanation`이 포함됩니다.
                    - 프론트에서 선택지 클릭 즉시 정답(초록) / 오답(빨강 + 정답 강조) 표시 가능
                    - 최종 채점 및 꿀단지 보상은 submit 시점에서만 처리됩니다.
                    
                    **correctAnswer 형식**
                    - 선다형(MULTIPLE_CHOICE): `"1"` ~ `"4"` (선택한 보기 번호와 비교)
                    - T/F형(TRUE_FALSE): `"TRUE"` 또는 `"FALSE"`
                    
                    **난이도 결정**
                    - 사용자 프로필(연령/언어수준) 기반으로 자동 결정
                    - 문제 언어는 동화의 primaryLanguage 기준
                    """
    )
    @GetMapping
    public ApiResponse<QuizResponse> getQuiz(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "동화 ID")
            @RequestParam(name = "storyId") Long storyId
    ) {
        log.info("퀴즈 조회 요청 - email={}, storyId={}", userDetails.getUsername(), storyId);

        QuizResponse response = quizService.getOrGenerateQuiz(userDetails.getUsername(), storyId);
        return ApiResponse.success(response, "퀴즈 조회 성공");
    }

    /**
     * 퀴즈 제출 및 채점
     * POST /api/quiz/submit
     *
     * - 서버에서 정답 판별 및 점수 계산 (클라이언트 의존 없음)
     * - 100점 달성 시 꿀단지 +1 자동 지급
     * - 동화 1권당 퀴즈 보상은 최초 1회만 지급
     */
    @Operation(
            summary = "퀴즈 제출",
            description = """
                    퀴즈 답안을 제출하고 채점 결과를 받습니다.
                    
                    **채점 규칙**
                    - 선다형: 보기 번호("1"~"4") 제출
                    - T/F형: "TRUE" 또는 "FALSE" 제출
                    - 점수 = (정답 수 / 총 문제 수) × 100
                    - 채점은 서버에서 독립적으로 수행 (클라이언트 correctAnswer와 무관)
                    
                    **꿀단지 보상**
                    - 100점 달성 시 꿀단지 +1 (동화 1권당 최초 1회)
                    - 꿀단지 20개 달성 시 동화 1권 무료 생성 자동 처리
                    
                    **결과 응답 (submit 후 종합 결과 화면)**
                    - 전체 점수, 정답 수, 100점 여부
                    - 문항별 제출 답안 / 정답 / 정오 여부 / 해설
                    - 꿀단지 보상 정보
                    """
    )
    @PostMapping("/submit")
    public ApiResponse<QuizResultResponse> submitQuiz(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody QuizSubmitRequest request
    ) {
        log.info("퀴즈 제출 요청 - email={}, quizId={}", userDetails.getUsername(), request.getQuizId());

        QuizResultResponse response = quizService.submitQuiz(userDetails.getUsername(), request);
        return ApiResponse.success(response, "퀴즈 채점 완료");
    }

    /**
     * 동화 완독 처리
     * POST /api/quiz/story-complete
     *
     * - 동화 완독 시 꿀단지 +1 지급
     * - 중복 지급 방지 (동화 1권당 최초 1회)
     */
    @Operation(
            summary = "동화 완독 처리",
            description = """
                    동화를 끝까지 읽었을 때 호출합니다.
                    
                    **꿀단지 보상**
                    - 완독 시 꿀단지 +1 (동화 1권당 최초 1회)
                    - 꿀단지 20개 달성 시 동화 1권 무료 생성 자동 처리
                    """
    )
    @PostMapping("/story-complete")
    public ApiResponse<QuizResultResponse.HoneyJarRewardInfo> completeStory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StoryCompleteRequest request
    ) {
        log.info("동화 완독 요청 - email={}, storyId={}", userDetails.getUsername(), request.getStoryId());

        QuizResultResponse.HoneyJarRewardInfo rewardInfo =
                quizService.completeStory(userDetails.getUsername(), request.getStoryId());

        return ApiResponse.success(rewardInfo, "동화 완독 처리 완료");
    }
}
