package com.moretale.global.security;

import com.moretale.global.security.jwt.JwtAuthenticationFilter;
import com.moretale.global.security.oauth.CustomOAuth2UserService;
import com.moretale.global.security.oauth.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 로그인 없이 접근 가능
                        .requestMatchers(
                                "/", "/login",
                                "/index.html", "/login.html",
                                "/css/**", "/js/**", "/images/**",
                                "/oauth2/**", "/login/oauth2/**",

                                // Swagger / OpenAPI
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // 공개 API
                        .requestMatchers("/api/translate/**").permitAll()                                  // AI 사투리 도사 API
                        .requestMatchers(HttpMethod.GET, "/api/dictionary/**").permitAll()                 // 방언 사전 조회는 비회원 가능
                        .requestMatchers("/error").permitAll()                                             // Spring Boot 에러 페이지

                        // TTS API 테스트용 (개발 환경에서만 permitAll, 운영에서는 authenticated로 변경)
                        .requestMatchers("/api/tts/**").permitAll()                                        // TTS API 테스트

                        // 업로드된 파일 접근 허용 (TTS 오디오 파일 다운로드용)
                        .requestMatchers("/uploads/**").permitAll()

                        // 동화 생성 API (인증 필요)
                        .requestMatchers("/api/stories/**").authenticated()

                        // 단어장 API (인증 필요)
                        .requestMatchers("/api/vocabulary/**").authenticated()

                        // 로그인 필요
                        .requestMatchers(HttpMethod.POST, "/api/dictionary/*/bookmark").authenticated()    // 북마크 추가
                        .requestMatchers(HttpMethod.DELETE, "/api/dictionary/*/bookmark").authenticated()  // 북마크 제거

                        // 퀴즈 API (인증 필요)
                        .requestMatchers("/api/quiz/**").authenticated()

                        // 꿀단지 API (인증 필요)
                        .requestMatchers("/api/honey-jar/**").authenticated()

                        // 마이페이지 & 회원 탈퇴 (인증 필요)
                        .requestMatchers(HttpMethod.GET, "/api/users/mypage").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/delete").authenticated()

                        // ADMIN 권한 필요
                        .requestMatchers(HttpMethod.PATCH, "/api/dictionary/*/verify").hasRole("ADMIN")    // 방언 검증
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")                                 // 관리자 경로

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler) // 로그인 성공 시 JWT 발급
                )
                .exceptionHandling(exceptions -> exceptions
                        // 인증 실패 시
                        .authenticationEntryPoint((req, res, ex) -> {
                            if (req.getRequestURI().startsWith("/api/")) {
                                res.setStatus(401);
                                res.setContentType("application/json");
                                res.getWriter().write("{\"message\": \"Unauthorized\"}");
                            } else {
                                res.sendRedirect("/oauth2/authorization/google");
                            }
                        })

                        // 권한 부족 (ex. 일반 사용자가 관리자 API 접근)
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(403);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"message\": \"Forbidden: 관리자 권한 필요\"}");
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
