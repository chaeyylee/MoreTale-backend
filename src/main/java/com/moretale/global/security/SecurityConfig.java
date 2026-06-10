package com.moretale.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moretale.global.common.ApiResponse;
import com.moretale.global.config.MoreTaleProperties;
import com.moretale.global.security.jwt.JwtAuthenticationFilter;
import com.moretale.global.security.oauth.CustomOAuth2UserService;
import com.moretale.global.security.oauth.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;
    private final MoreTaleProperties moreTaleProperties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 로그인 없이 접근 가능
                        .requestMatchers(
                                "/", "/login",
                                "/oauth2/**", "/login/oauth2/**",

                                // Swagger / OpenAPI
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",

                                // Spring Boot 기본 에러 페이지
                                "/error",

                                // AI internal callback
                                "/internal/ai/story/callbacks",
                                "/internal/ai/story/callbacks/**"
                        ).permitAll()

                        // 생성된 오디오 파일 접근 (공개 동화 재생 등)
                        .requestMatchers("/uploads/**").permitAll()

                        // ── 인증 필요 API ────────────────────────────────────────

                        // 사용자 계정 & 프로필
                        .requestMatchers("/api/users/**").authenticated()

                        // 동화 관리
                        .requestMatchers("/api/stories/**").authenticated()

                        // 도서관
                        .requestMatchers("/api/library/**").authenticated()

                        // TTS 생성 API (AI 비용 발생 → 인증 필수)
                        .requestMatchers("/api/tts/**").authenticated()

                        // 단어장
                        .requestMatchers("/api/vocabulary/**").authenticated()

                        // 퀴즈 & 꿀단지
                        .requestMatchers("/api/quiz/**").authenticated()
                        .requestMatchers("/api/honey-jar/**").authenticated()

                        // ADMIN 전용
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ── 그 외 모든 요청은 인증 필요 ─────────────────────────
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                )
                .exceptionHandling(exceptions -> exceptions

                        // 401 - 인증되지 않은 요청
                        .authenticationEntryPoint((req, res, ex) -> {
                            if (req.getRequestURI().startsWith("/api/")) {

                                ApiResponse<Void> body = ApiResponse.error(
                                        "UNAUTHORIZED",
                                        "인증이 필요합니다. 로그인 후 다시 시도해주세요."
                                );

                                res.setStatus(401);
                                res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                res.setCharacterEncoding(StandardCharsets.UTF_8.name());

                                res.getWriter().write(
                                        objectMapper.writeValueAsString(body)
                                );

                            } else {
                                res.sendRedirect("/oauth2/authorization/google");
                            }
                        })

                        // 403 - 권한 부족
                        .accessDeniedHandler((req, res, ex) -> {

                            ApiResponse<Void> body = ApiResponse.error(
                                    "FORBIDDEN",
                                    "접근 권한이 없습니다. 관리자 권한이 필요합니다."
                            );

                            res.setStatus(403);
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            res.setCharacterEncoding(StandardCharsets.UTF_8.name());

                            res.getWriter().write(
                                    objectMapper.writeValueAsString(body)
                            );
                        })
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of(
                "https://moretale.vercel.app",
                "https://*.vercel.app",
                "http://localhost:5173",
                "http://localhost:3000"
        ));

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private List<String> resolveAllowedOrigins() {
        return moreTaleProperties.getCors().getAllowedOrigins().stream()
                .flatMap(origin -> Arrays.stream(origin.split(",")))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
    }
}
