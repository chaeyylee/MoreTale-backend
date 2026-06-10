package com.moretale.global.security.oauth;

import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserProfileRepository userProfileRepository;

    @Value("${app.frontend-url:http://localhost:8080}")
    private String frontendUrl;

    private static final String ONBOARDING_PATH = "/onboarding";
    private static final String HOME_PATH = "/";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // 1. CustomOAuth2User에서 사용자 정보 추출
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        Long userId = oAuth2User.getUserId();
        String email = oAuth2User.getEmail();

        // 2. JWT 토큰 생성
        String token = jwtTokenProvider.generateTokenFromUserId(userId);

        log.info("OAuth2 로그인 성공 - userId: {}, email: {}", userId, email);
        log.info("JWT Token 생성 완료");
        log.info("JWT Token: {}", token);

        // 3. 프로필 존재 여부 확인
        boolean hasProfile = userProfileRepository.existsByUser_UserId(userId);

        // 4. 리다이렉트 URL 결정
        String redirectPath;
        if (hasProfile) {
            // 프로필이 있으면 메인 홈으로
            redirectPath = HOME_PATH;
            log.info("기존 사용자 - 메인 홈으로 이동: userId={}", userId);
        } else {
            // 프로필이 없으면 온보딩 페이지로
            redirectPath = ONBOARDING_PATH;
            log.info("신규 사용자 - 온보딩 페이지로 이동: userId={}", userId);
        }

        // 5. 리다이렉트 URL 생성 (token, userId, hasProfile 전달)
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + redirectPath)
                .queryParam("token", token)
                .queryParam("userId", userId)
                .queryParam("hasProfile", hasProfile)
                .build()
                .toUriString();

        log.info("리다이렉트 실행 URL: {}", targetUrl);

        // 6. 리다이렉트 수행
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
