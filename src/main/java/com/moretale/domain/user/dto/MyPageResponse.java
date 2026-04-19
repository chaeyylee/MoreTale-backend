package com.moretale.domain.user.dto;

import com.moretale.domain.profile.dto.UserProfileResponse;
import com.moretale.domain.story.dto.RecentStoryResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "마이페이지 통합 응답 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPageResponse {

    @Schema(description = "계정 정보 (Google OAuth 기반, 수정 불가)")
    private AccountInfoResponse accountInfo;

    @Schema(description = "자녀 프로필 목록 (PUT /api/users/profile/{profileId}로 수정)")
    private List<UserProfileResponse> profiles;

    @Schema(description = "사용 현황 (꿀단지 + 무료 동화 잔여 횟수 + 누적 동화 수)")
    private UsageStatusResponse usageStatus;

    @Schema(description = "최근 생성 동화 목록 (최대 5건, 언어는 코드 또는 표시값으로 내려올 수 있음)")
    private List<RecentStoryResponse> recentStories;
}
