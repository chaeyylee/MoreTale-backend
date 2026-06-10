package com.moretale.domain.honeyjar.repository;

import com.moretale.domain.honeyjar.entity.HoneyJar;
import com.moretale.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface HoneyJarRepository extends JpaRepository<HoneyJar, Long> {

    // User 엔티티 기반 조회 (HoneyJarService 내부에서 User 객체가 있을 때 사용)
    Optional<HoneyJar> findByUser(User user);

    // 동시성 제어를 위한 비관적 락 조회 - User 엔티티 기반
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM HoneyJar h WHERE h.user = :user")
    Optional<HoneyJar> findByUserWithLock(@Param("user") User user);

    // userId 기반 조회 - User 엔티티 조회 없이 직접 사용 가능
    @Query("SELECT h FROM HoneyJar h WHERE h.user.userId = :userId")
    Optional<HoneyJar> findByUserId(@Param("userId") Long userId);

    // userId 기반 비관적 락 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM HoneyJar h WHERE h.user.userId = :userId")
    Optional<HoneyJar> findByUserIdWithLock(@Param("userId") Long userId);

    // 회원 탈퇴 시 꿀단지 삭제
    void deleteByUser(User user);
}
