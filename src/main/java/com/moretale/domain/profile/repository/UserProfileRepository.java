package com.moretale.domain.profile.repository;

import com.moretale.domain.profile.entity.UserProfile;
import com.moretale.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // User 엔티티 기반 조회 (동화 기능용)

    // 가장 최근에 생성된 프로필 1개 조회
    Optional<UserProfile> findFirstByUserOrderByCreatedAtDesc(User user);

    // 해당 사용자의 모든 프로필 조회
    List<UserProfile> findAllByUser(User user);

    // 해당 사용자의 프로필 존재 여부 확인
    boolean existsByUser(User user);

    // 해당 사용자의 모든 프로필 삭제
    void deleteByUser(User user);

    // userId 기반 조회 (프로필 관리용)

    // 사용자 ID로 모든 프로필 조회
    List<UserProfile> findAllByUser_UserId(Long userId);

    // // 특정 사용자의 프로필 존재 여부를 확인
    boolean existsByUser_UserId(Long userId);

    // 특정 사용자가 동일한 이름의 자녀를 이미 등록했는지 확인
    boolean existsByUser_UserIdAndChildName(Long userId, String childName);

    // 특정 프로필 ID와 사용자 ID가 일치하는 프로필을 조회 (보안 강화용)
    Optional<UserProfile> findByProfileIdAndUser_UserId(Long profileId, Long userId);
}
