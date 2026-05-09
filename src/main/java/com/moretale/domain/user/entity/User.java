package com.moretale.domain.user.entity;

import com.moretale.domain.profile.entity.UserProfile;
import com.moretale.domain.story.entity.Story;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// 사용자 정보 저장 User 엔티티
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    // 사용자 고유 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    // 사용자 이메일 (OAuth 로그인 기준, 중복X)
    @Column(nullable = false, unique = true)
    private String email;

    // 사용자 닉네임
    @Column(length = 50)
    private String nickname;

    // 사용자 권한
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    // 계정 생성 시각
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // OAuth2 제공자 정보
    @Column(name = "provider")
    private String provider;

    // OAuth2 사용자 고유 ID
    @Column(name = "provider_id")
    private String providerId;

    // 1:N 관계 설정: 한 명의 사용자는 여러 개의 자녀 프로필을 가질 수 있음
    // mappedBy: UserProfile 엔티티에 있는 'user' 필드에 의해 매핑됨
    // orphanRemoval = true: User가 삭제되거나 리스트에서 제거된 프로필은 DB에서도 삭제됨
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserProfile> profiles = new ArrayList<>();

    // 1:N 관계 설정: 한 명의 사용자는 여러 개의 동화를 생성할 수 있음
    // CascadeType.ALL: User 삭제 시 연관된 모든 Story 데이터도 함께 삭제
    // orphanRemoval = true: User의 stories 리스트에서 제거된 Story 엔티티는 DB에서도 삭제
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Story> stories = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // "ROLE_USER" 또는 "ROLE_ADMIN" 형태로 권한 반환
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getPassword() {
        return null; // OAuth2 전용 계정이므로 비밀번호 없음
    }

    @Override
    public String getUsername() {
        return this.email; // 이메일을 식별자로 사용
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

    // 사용자 권한 enum
    public enum Role {
        USER, ADMIN
    }
}
