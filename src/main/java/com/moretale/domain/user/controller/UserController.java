package com.moretale.domain.user.controller;

import com.moretale.domain.user.dto.MyPageResponse;
import com.moretale.domain.user.dto.RegionRequest;
import com.moretale.domain.user.dto.UserResponse;
import com.moretale.domain.user.service.UserService;
import com.moretale.global.common.ApiResponse;
import com.moretale.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 계정 관리 API")
public class UserController {

    private final UserService userService;

    // 현재 로그인한 사용자 정보 조회
    // GET /api/users/me
    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 기본 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("사용자 정보 조회 요청 - userId: {}", userPrincipal.getUserId());

        UserResponse response = userService.getUserInfo(userPrincipal.getUserId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 사용자 지역 설정
    // PATCH /api/users/me/region
    @PatchMapping("/me/region")
    @Operation(summary = "지역 설정", description = "사용자의 지역 정보를 설정합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> updateRegion(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody RegionRequest request) {

        log.info("사용자 지역 설정 요청 - userId: {}, region: {}", userPrincipal.getUserId(), request.getRegion());
        UserResponse response = userService.updateRegion(userPrincipal.getUserId(), request.getRegion());
        return ResponseEntity.ok(ApiResponse.success(response, "지역이 설정되었습니다."));
    }

    // 마이페이지 통합 조회
    /**
     * 마이페이지 통합 조회
     * GET /api/users/mypage
     *
     * <p>응답 구조:
     * <ul>
     *   <li>accountInfo  : 계정 정보 (이메일, 닉네임, 가입일) - read-only</li>
     *   <li>profiles     : 자녀 프로필 목록 (온보딩 정보 포함, 수정 가능)</li>
     *   <li>usageStatus  : 꿀단지 수 / 무료 동화 잔여 횟수 / 누적 동화 수</li>
     *   <li>recentStories: 최근 생성 동화 5건 (썸네일 포함)</li>
     * </ul>
     */
    @GetMapping("/mypage")
    @Operation(
            summary = "마이페이지 조회",
            description = """
                    사용자의 마이페이지 통합 정보를 조회합니다.
                    
                    **응답 구조**
                    - `accountInfo`: 계정 정보 (Google 이메일 - read only)
                    - `profiles`: 자녀 프로필 목록 (온보딩 정보 포함, PUT /api/users/profile/{profileId}로 수정 가능)
                    - `usageStatus.honeyJarCount`: 현재 꿀단지 보유 수
                    - `usageStatus.canGenerateFreeStory`: 무료 동화 생성 가능 여부
                    - `usageStatus.remainingHoneyJarForFree`: 무료 생성까지 남은 꿀단지 수
                    - `usageStatus.totalStoriesCreated`: 누적 생성 동화 수
                    - `recentStories`: 최근 생성 동화 5건
                    """
    )
    public ResponseEntity<ApiResponse<MyPageResponse>> getMyPage(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("마이페이지 조회 요청 - userId: {}", userPrincipal.getUserId());
        MyPageResponse response = userService.getMyPage(userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 회원 탈퇴
    /**
     * 회원 탈퇴
     * DELETE /api/users/delete
     *
     * <p>처리 순서 (연관 데이터 정책):
     * <ol>
     *   <li>꿀단지 이력(HoneyJarHistory) 삭제</li>
     *   <li>꿀단지(HoneyJar) 삭제</li>
     *   <li>동화/슬라이드/토큰(Story → Slide → StoryToken) 삭제</li>
     *   <li>자녀 프로필(UserProfile) 삭제 (User Cascade)</li>
     *   <li>사용자(User) 삭제</li>
     * </ol>
     */
    @DeleteMapping("/delete")
    @Operation(
            summary = "회원 탈퇴",
            description = """
                    현재 로그인한 사용자의 계정을 탈퇴 처리합니다.
                    
                    **삭제 처리 정책**
                    - 꿀단지 이력, 꿀단지, 동화, 슬라이드, 토큰, 자녀 프로필 모두 삭제
                    - 삭제된 데이터는 복구 불가
                    """
    )
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("회원 탈퇴 요청 - userId: {}", userPrincipal.getUserId());
        userService.deleteUser(userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "회원 탈퇴가 완료되었습니다."));
    }
}
