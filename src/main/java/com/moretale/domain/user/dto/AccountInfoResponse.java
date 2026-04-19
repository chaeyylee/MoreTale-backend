package com.moretale.domain.user.dto;

import com.moretale.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "마이페이지 계정 정보 응답 DTO (read-only)")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountInfoResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "Google 이메일 (변경 불가)", example = "user@gmail.com")
    private String email;

    @Schema(description = "닉네임", example = "홍길동")
    private String nickname;

    @Schema(description = "OAuth 제공자", example = "google")
    private String provider;

    @Schema(description = "지역 정보", example = "서울")
    private String region;

    @Schema(description = "가입일시 (ISO 8601)", example = "2024-01-01T00:00:00Z")
    private LocalDateTime createdAt;

    public static AccountInfoResponse fromEntity(User user) {
        return AccountInfoResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .provider(user.getProvider())
                .region(user.getRegion())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
