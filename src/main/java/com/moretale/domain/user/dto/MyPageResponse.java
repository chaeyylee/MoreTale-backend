package com.moretale.domain.user.dto;

import com.moretale.domain.honeyjar.dto.HoneyJarResponse;
import com.moretale.domain.profile.dto.UserProfileResponse;
import com.moretale.domain.story.dto.RecentStoryResponse;
import lombok.*;

import java.util.List;

/**
 * 마이페이지 통합 응답 DTO
 * - 계정 정보 (read-only)
 * - 자녀 프로필 목록
 * - 사용 현황 (꿀단지, 무료 동화 잔여 횟수)
 * - 최근 생성 동화 목록
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPageResponse {

    // 계정 정보 (Google OAuth 기반, 수정 불가)
    private AccountInfoResponse accountInfo;

    // 자녀 프로필 목록 (온보딩 기준, 수정 가능)
    private List<UserProfileResponse> profiles;

    // 사용 현황 (꿀단지 + 무료 동화 잔여 횟수)
    private UsageStatusResponse usageStatus;

    // 최근 생성 동화 목록 (최대 5건)
    private List<RecentStoryResponse> recentStories;
}
