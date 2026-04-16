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

    // 사용자로 꿀단지 조회
    Optional<HoneyJar> findByUser(User user);

    // 동시성 제어를 위한 비관적 락 조회 (꿀단지 차감 시 사용)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM HoneyJar h WHERE h.user = :user")
    Optional<HoneyJar> findByUserWithLock(@Param("user") User user);

    // 회원 탈퇴 시 꿀단지 삭제
    void deleteByUser(User user);
}
