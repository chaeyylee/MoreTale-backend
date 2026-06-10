package com.moretale.domain.honeyjar.controller;

import com.moretale.domain.honeyjar.dto.HoneyJarHistoryResponse;
import com.moretale.domain.honeyjar.dto.HoneyJarResponse;
import com.moretale.domain.honeyjar.service.HoneyJarService;
import com.moretale.global.common.ApiResponse;
import com.moretale.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "HoneyJar", description = "꿀단지 보상 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/honey-jar")
@RequiredArgsConstructor
public class HoneyJarController {

    private final HoneyJarService honeyJarService;
    // UserRepository 의존성 제거 - Service에서 User 조회를 담당

    // 꿀단지 현황 조회
    // GET /api/honey-jar
    @Operation(
            summary = "꿀단지 현황 조회",
            description = """
                    현재 사용자의 꿀단지 보유 현황을 조회합니다.
                    
                    **응답 정보**
                    - `count`: 현재 보유 꿀단지 수
                    - `canGenerateFree`: 동화 무료 생성 가능 여부 (10개 이상)
                    - `remainingForFree`: 무료 생성까지 남은 꿀단지 수
                    """
    )
    @GetMapping
    public ApiResponse<HoneyJarResponse> getHoneyJar(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("꿀단지 조회 요청 - userId={}", userPrincipal.getUserId());

        // Service 내부에서 userId → User 조회 처리
        HoneyJarResponse response = honeyJarService.getHoneyJarByUserId(userPrincipal.getUserId());
        return ApiResponse.success(response, "꿀단지 조회 성공");
    }

    // 꿀단지 이력 조회
    // GET /api/honey-jar/history
    @Operation(
            summary = "꿀단지 이력 조회",
            description = """
                    현재 사용자의 꿀단지 획득/사용 이력을 최신순으로 조회합니다.
                    
                    **응답 정보**
                    - `actionType`: 변동 유형
                    - `amount`: 변동 수량 (획득: 양수, 사용: 음수)
                    - `reason`: 변동 사유
                    - `balanceAfter`: 변동 후 잔액
                    - `storyId`: 관련 동화 ID
                    - `createdAt`: 이력 생성 시각
                    """
    )
    @GetMapping("/history")
    public ApiResponse<List<HoneyJarHistoryResponse>> getHoneyJarHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("꿀단지 이력 조회 요청 - userId={}", userPrincipal.getUserId());

        // Controller에서 직접 User 조회하던 코드 제거
        // Service 내부에서 userId → User 조회 처리
        List<HoneyJarHistoryResponse> response =
                honeyJarService.getHoneyJarHistoryByUserId(userPrincipal.getUserId());
        return ApiResponse.success(response, "꿀단지 이력 조회 성공");
    }
}
