package com.moretale.domain.user.dto;

import com.moretale.domain.user.entity.User;
import lombok.*;

import java.time.LocalDateTime;

// 계정 정보 응답 DTO (read-only)
// Google OAuth 기반이므로 이메일 수정 불가
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountInfoResponse {

    private Long userId;

    // Google 이메일 (read-only)
    private String email;

    // 닉네임
    private String nickname;

    // OAuth 제공자 (google)
    private String provider;

    // 지역
    private String region;

    // 가입일
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
