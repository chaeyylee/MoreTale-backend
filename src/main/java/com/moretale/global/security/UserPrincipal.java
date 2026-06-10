package com.moretale.global.security;

import com.moretale.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

// Spring Security 인증을 위한 UserPrincipal 클래스
@Getter
public class UserPrincipal implements OAuth2User, UserDetails {

    private final Long userId;
    private final String email;
    private final String nickname;
    private final Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    public UserPrincipal(Long userId, String email, String nickname,
                         Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.authorities = authorities;
    }

    // User 엔티티로부터 UserPrincipal 생성
    public static UserPrincipal create(User user) {
        return new UserPrincipal(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                )
        );
    }

    // User 엔티티와 OAuth2 속성으로부터 UserPrincipal 생성
    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    // UserDetails 구현
    @Override
    public String getPassword() {
        return null; // OAuth2 로그인이므로 비밀번호 없음
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // OAuth2User 구현
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
