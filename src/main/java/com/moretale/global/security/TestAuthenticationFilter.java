package com.moretale.global.security;

import com.moretale.domain.profile.entity.UserProfile;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// 개발 및 테스트용 인증 필터
// 모든 요청에 대해 'test@example.com' 사용자로 자동 인증을 수행
// DB에 해당 사용자와 기본 프로필이 없으면 자동으로 생성
@Slf4j
// @Component // 개발/테스트용이라 필요할 때만 활성화
@RequiredArgsConstructor
public class TestAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 이미 인증 정보가 존재하는 경우 필터 통과
        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
                !"anonymousUser".equals(SecurityContextHolder.getContext().getAuthentication().getPrincipal())) {

            filterChain.doFilter(request, response);
            return;
        }

        try {
            String testEmail = "test@example.com";

            // 2. 테스트 사용자 조회 및 자동 생성
            User testUser = userRepository.findByEmail(testEmail)
                    .orElseGet(() -> {
                        User newUser = User.builder()
                                .email(testEmail)
                                .nickname("테스트사용자")
                                .role(User.Role.USER)
                                .build();
                        log.info("✅ 테스트 사용자 자동 생성 완료 - email: {}", testEmail);
                        return userRepository.save(newUser);
                    });

            // 3. 테스트용 프로필 조회 및 자동 생성
            if (!userProfileRepository.existsByUser(testUser)) {
                UserProfile testProfile = UserProfile.builder()
                        .user(testUser)
                        .childName("민준")
                        .childAge(6)
                        .childNationality("KR")
                        .parentCountry("베트남")
                        .primaryLanguage("ko")
                        .secondaryLanguage("vi")
                        .build();
                userProfileRepository.save(testProfile);
                log.info("✅ 테스트용 프로필 자동 생성 완료 - childName: 민준");
            }

            // 4. OAuth2 인증 객체 빌드
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", "test-user-123");
            attributes.put("email", testEmail);
            attributes.put("name", "테스트 사용자");
            attributes.put("picture", "https://example.com/picture.jpg");
            attributes.put("email_verified", true);

            OAuth2User oAuth2User = new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                    attributes,
                    "email"
            );

            OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                    oAuth2User,
                    oAuth2User.getAuthorities(),
                    "google"
            );

            // 5. SecurityContext에 인증 정보 강제 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("✅ 테스트 인증 설정 완료 - email: {}", testEmail);

        } catch (Exception e) {
            log.error("❌ 테스트 인증 설정 중 오류 발생", e);
        }

        filterChain.doFilter(request, response);
    }
}
