package com.moretale.domain.user.repository;

import com.moretale.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// User 엔티티에 대한 JPA Repository 인터페이스
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);
    // OAuth provider + providerId로 사용자 조회
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
    // 이메일 중복 여부 확인
    boolean existsByEmail(String email);
}
