package com.moretale.domain.profile.controller;

import com.moretale.domain.profile.dto.*;
import com.moretale.domain.profile.service.UserProfileService;
import com.moretale.global.common.ApiResponse;
import com.moretale.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Profile", description = "사용자 프로필 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/users/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @Operation(
            summary = "온보딩 프로필 생성",
            description = """
                    최초 로그인 후 단계별 질문에 따라 프로필을 생성합니다.

                    **온보딩 단계**
                    1. 아이 이름 / 나이 그룹 입력
                    2. 제1언어 / 제2언어 선택 (OTHER 선택 시 customFirstLanguage 필수)
                    3. 언어 숙련도 입력 (EGG → LARVA → PUPA → BEE)
                    4. 가족 구조 선택
                    5. 이야기 선호도 선택

                    **언어 코드 (firstLanguage / secondLanguage)**
                    - `KO` 한국어, `EN` 영어, `JA` 일본어, `ZH` 중국어, `ES` 스페인어, `VI` 베트남어, `OTHER` 기타
                    """
    )
    @PostMapping("/onboarding")
    public ApiResponse<OnboardingProfileResponse> createOnboardingProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @org.springframework.web.bind.annotation.RequestBody OnboardingProfileRequest request
    ) {
        log.info("온보딩 프로필 생성 요청 - userId: {}", userPrincipal.getUserId());
        return ApiResponse.success(
                userProfileService.createOnboardingProfile(userPrincipal.getUserId(), request),
                "프로필 설정이 완료되었습니다!"
        );
    }

    @Operation(
            summary = "프로필 생성",
            description = """
                    사용자 자녀 프로필을 생성합니다. (1:N 지원)

                    - 한 계정에 여러 자녀 프로필 생성 가능
                    - 온보딩 이후 추가 프로필 등록 시 사용
                    """
    )
    @PostMapping
    public ApiResponse<UserProfileResponse> createProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @org.springframework.web.bind.annotation.RequestBody UserProfileRequest request
    ) {
        log.info("프로필 생성 요청 - userId: {}", userPrincipal.getUserId());
        return ApiResponse.success(
                userProfileService.createProfile(userPrincipal.getUserId(), request),
                "프로필이 생성되었습니다."
        );
    }

    @Operation(
            summary = "전체 프로필 목록 조회",
            description = "현재 로그인한 사용자의 모든 자녀 프로필 목록을 조회합니다."
    )
    @GetMapping("/list")
    public ApiResponse<List<UserProfileResponse>> getAllProfiles(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("전체 프로필 목록 조회 요청 - userId: {}", userPrincipal.getUserId());
        return ApiResponse.success(
                userProfileService.getAllProfiles(userPrincipal.getUserId())
        );
    }

    @Operation(
            summary = "특정 프로필 상세 조회",
            description = "프로필 고유 ID(profileId)를 기준으로 상세 정보를 조회합니다."
    )
    @GetMapping("/{profileId}")
    public ApiResponse<UserProfileResponse> getProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "프로필 ID") @PathVariable("profileId") Long profileId
    ) {
        log.info("프로필 상세 조회 요청 - userId: {}, profileId: {}",
                userPrincipal.getUserId(), profileId);
        return ApiResponse.success(userProfileService.getProfile(profileId));
    }

    @Operation(
            summary = "프로필 수정",
            description = """
                    특정 자녀 프로필 정보 전체를 수정합니다.

                    - PUT 방식으로 전체 필드 교체
                    - 부분 수정은 PATCH `/api/users/profile/{profileId}/language` 사용
                    """
    )
    @PutMapping("/{profileId}")
    public ApiResponse<UserProfileResponse> updateProfile(
            @Parameter(description = "프로필 ID") @PathVariable("profileId") Long profileId,
            @Valid @org.springframework.web.bind.annotation.RequestBody UserProfileRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("프로필 수정 요청 - userId: {}, profileId: {}",
                userPrincipal.getUserId(), profileId);
        return ApiResponse.success(
                userProfileService.updateProfile(userPrincipal.getUserId(), profileId, request),
                "프로필이 수정되었습니다."
        );
    }

    @Operation(
            summary = "언어 설정 수정",
            description = """
                    특정 프로필의 이중언어 설정만 변경합니다.

                    - firstLanguage / secondLanguage Enum 값 변경
                    - OTHER 선택 시 customFirstLanguage / customSecondLanguage 필수
                    - 일반 언어(KO, EN, JA, ZH, ES, VI) 선택 시 custom 값은 null 사용
                    """,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "일반 언어 예시",
                                            value = """
                                                    {
                                                      "firstLanguage": "KO",
                                                      "customFirstLanguage": null,
                                                      "secondLanguage": "VI",
                                                      "customSecondLanguage": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "OTHER 언어 예시",
                                            value = """
                                                    {
                                                      "firstLanguage": "KO",
                                                      "customFirstLanguage": null,
                                                      "secondLanguage": "OTHER",
                                                      "customSecondLanguage": "태국어"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    @PatchMapping("/{profileId}/language")
    public ApiResponse<UserProfileResponse> updateLanguage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "프로필 ID") @PathVariable("profileId") Long profileId,
            @Valid @org.springframework.web.bind.annotation.RequestBody LanguageUpdateRequest request
    ) {
        log.info("언어 설정 수정 요청 - userId: {}, profileId: {}",
                userPrincipal.getUserId(), profileId);
        return ApiResponse.success(
                userProfileService.updateLanguage(profileId, request),
                "언어 설정이 변경되었습니다."
        );
    }

    @Operation(
            summary = "프로필 존재 여부",
            description = """
                    최소 하나 이상의 자녀 프로필이 설정되어 있는지 확인합니다.

                    - `true`: 온보딩 완료, 홈 화면으로 이동
                    - `false`: 온보딩 미완료, 온보딩 화면으로 이동
                    """
    )
    @GetMapping("/exists")
    public ApiResponse<Boolean> hasProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ApiResponse.success(userProfileService.hasProfile(userPrincipal.getUserId()));
    }

    @Operation(
            summary = "프로필 삭제",
            description = "특정 자녀 프로필을 삭제합니다. 본인 소유 프로필만 삭제 가능합니다."
    )
    @DeleteMapping("/{profileId}")
    public ApiResponse<Void> deleteProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "프로필 ID") @PathVariable("profileId") Long profileId
    ) {
        log.info("프로필 삭제 요청 - userId: {}, profileId: {}",
                userPrincipal.getUserId(), profileId);
        userProfileService.deleteProfile(profileId);
        return ApiResponse.success(null, "프로필이 삭제되었습니다.");
    }
}
