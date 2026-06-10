package com.moretale.global.security.oauth;

import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        // attributes 전체 출력 제거 -> provider만 로깅 (민감정보 보호)
        log.info("OAuth2 Login - provider={}", registrationId);

        // Google에서 받은 정보 추출
        String providerId = oauth2User.getAttribute("sub");
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        // email null 체크 (OAuth2 제공자가 이메일을 반환하지 않는 경우 방어)
        if (email == null) {
            log.warn("OAuth2 로그인 실패 - provider={}에서 이메일을 제공하지 않음", registrationId);
            throw new OAuth2AuthenticationException("EMAIL_NOT_PROVIDED");
        }

        User user = userRepository.findByProviderAndProviderId(registrationId, providerId)
                .map(existingUser -> {
                    // 기존 사용자 로그 - 이메일 마스킹 처리
                    log.info("OAuth2 기존 사용자 로그인 - provider={}, userId={}",
                            registrationId, existingUser.getUserId());
                    return existingUser;
                })
                .orElseGet(() -> createUser(registrationId, providerId, email, name));

        return new CustomOAuth2User(user, attributes);
    }

    private User createUser(String provider, String providerId, String email, String name) {
        User newUser = User.builder()
                .email(email)
                .nickname(name != null ? name : email.split("@")[0])
                .provider(provider)
                .providerId(providerId)
                .role(User.Role.USER)
                .build();

        // 신규 사용자 생성 시 email 직접 노출 제거
        log.info("OAuth2 신규 사용자 생성 - provider={}", provider);
        return userRepository.save(newUser);
    }
}
