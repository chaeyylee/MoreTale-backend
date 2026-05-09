package com.moretale.domain.story.controller;

import com.moretale.domain.story.dto.*;
import com.moretale.domain.story.service.StoryService;
import com.moretale.global.common.ApiResponse;
import com.moretale.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Story", description = "동화 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    @Operation(
            summary = "동화 생성 초기값 조회",
            description = """
                    온보딩 데이터를 기반으로 동화 생성 폼의 초기값을 반환합니다.

                    **응답 필드**
                    - profileId : 프로필 ID
                    - childName : 아이 이름
                    - firstLanguage / secondLanguage : 언어 설정
                    - ageGroup / childAge : 연령 그룹
                    - storyPreference : 이야기 선호도
                    - recommendedTaleTitle : 추천 전래동화 제목
                    """
    )
    @GetMapping("/init")
    public ApiResponse<StoryInitResponse> getStoryInitData(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "프로필 ID (미입력 시 첫 번째 프로필 사용)")
            @RequestParam(name = "profileId", required = false) Long profileId
    ) {
        log.info("동화 초기값 조회 요청 - userId={}, profileId={}",
                userPrincipal.getUserId(), profileId);
        return ApiResponse.success(
                storyService.getStoryInitData(userPrincipal.getUserId(), profileId),
                "동화 생성 초기값 조회 성공"
        );
    }

    @Operation(
            summary = "자동 동화 생성",
            description = """
                    온보딩 데이터를 기반으로 추천 전래동화를 자동 생성합니다.

                    - 프로필의 storyPreference에 맞는 전래동화 자동 선택
                    - 별도 프롬프트 입력 없이 온보딩 정보만으로 생성
                    - 생성 후 `/api/stories` (POST)로 저장 필요
                    """
    )
    @PostMapping("/auto-generate")
    public ApiResponse<StoryGenerateResponse> autoGenerateStory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "프로필 ID (미입력 시 첫 번째 프로필 사용)")
            @RequestParam(name = "profileId", required = false) Long profileId
    ) {
        log.info("자동 동화 생성 요청 - userId={}, profileId={}",
                userPrincipal.getUserId(), profileId);
        return ApiResponse.success(
                storyService.autoGenerateStory(userPrincipal.getUserId(), profileId),
                "동화 자동 생성 완료"
        );
    }

    @Operation(
            summary = "동화 생성",
            description = """
                    사용자 프롬프트를 기반으로 이중언어 동화를 생성합니다.

                    - 생성된 동화는 임시 상태이며, `/api/stories` (POST)로 저장해야 합니다.
                    - primaryLanguage / secondaryLanguage 언어쌍으로 이중언어 슬라이드 생성
                    - ageGroup, proficiency 기반으로 난이도 자동 조정
                    """
    )
    @PostMapping("/generate")
    public ApiResponse<StoryGenerateResponse> generateStory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody StoryGenerateRequest request
    ) {
        log.info("동화 생성 요청 - userId={}, prompt={}",
                userPrincipal.getUserId(), request.getPrompt());
        return ApiResponse.success(
                storyService.generateStory(userPrincipal.getUserId(), request),
                "동화 생성 완료"
        );
    }

    @Operation(
            summary = "동화 저장",
            description = """
                    생성된 동화를 데이터베이스에 저장합니다.

                    - `/api/stories/generate` 또는 `/api/stories/auto-generate` 응답값을 그대로 사용
                    - 슬라이드 순서(order)는 0부터 시작
                    - 저장 시 단어 토큰(StoryToken) 자동 분석 및 연결
                    """
    )
    @PostMapping
    public ApiResponse<StoryResponse> saveStory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody StorySaveRequest request
    ) {
        log.info("동화 저장 요청 - userId={}, title={}",
                userPrincipal.getUserId(), request.getTitle());
        return ApiResponse.success(
                storyService.saveStory(userPrincipal.getUserId(), request),
                "동화 저장 완료"
        );
    }

    @Operation(
            summary = "동화 상세 조회",
            description = """
                    특정 동화의 상세 정보를 조회합니다.

                    - 슬라이드 목록 및 각 슬라이드의 단어 토큰 포함
                    - 본인 소유 동화 또는 공개(isPublic=true) 동화만 조회 가능
                    """
    )
    @GetMapping("/{storyId}")
    public ApiResponse<StoryResponse> getStoryDetail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "동화 ID") @PathVariable(name = "storyId") Long storyId
    ) {
        return ApiResponse.success(
                storyService.getStoryDetail(userPrincipal.getUserId(), storyId)
        );
    }

    @Operation(
            summary = "내 동화 목록 조회",
            description = "현재 사용자가 생성한 모든 동화를 조회합니다. 썸네일(첫 슬라이드 이미지) 포함."
    )
    @GetMapping("/my")
    public ApiResponse<List<StoryListResponse>> getMyStories(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ApiResponse.success(storyService.getMyStories(userPrincipal.getUserId()));
    }

    @Operation(
            summary = "공개 동화 목록 조회",
            description = "공개 설정(isPublic=true)된 모든 동화를 조회합니다."
    )
    @GetMapping("/public")
    public ApiResponse<List<StoryListResponse>> getPublicStories() {
        return ApiResponse.success(storyService.getPublicStories());
    }

    @Operation(
            summary = "동화 공유 설정",
            description = """
                    동화의 공개/비공개 설정을 변경합니다.

                    - `isPublic: true` → 공개 (공개 동화 목록에 노출)
                    - `isPublic: false` → 비공개
                    - 본인 소유 동화만 변경 가능
                    """
    )
    @PatchMapping("/{storyId}/share")
    public ApiResponse<Void> updateStoryShareStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "동화 ID") @PathVariable(name = "storyId") Long storyId,
            @Valid @RequestBody StoryShareRequest request
    ) {
        storyService.updateStoryShareStatus(userPrincipal.getUserId(), storyId, request);
        return ApiResponse.success(null, "공유 설정 변경 완료");
    }

    @Operation(
            summary = "동화 삭제",
            description = """
                    특정 동화를 삭제합니다.

                    - 본인 소유 동화만 삭제 가능
                    - 연관 슬라이드, 단어 토큰 함께 삭제
                    - 도서관에서 삭제 시 `/api/library/{storyId}` (DELETE) 사용 권장
                    """
    )
    @DeleteMapping("/{storyId}")
    public ApiResponse<Void> deleteStory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "동화 ID") @PathVariable(name = "storyId") Long storyId
    ) {
        storyService.deleteStory(userPrincipal.getUserId(), storyId);
        return ApiResponse.success(null, "동화 삭제 완료");
    }
}
