package com.moretale.domain.honeyjar.repository;

import com.moretale.domain.honeyjar.entity.HoneyJarHistory;
import com.moretale.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoneyJarHistoryRepository extends JpaRepository<HoneyJarHistory, Long> {

    // 사용자 꿀단지 이력 조회 (최신순)
    List<HoneyJarHistory> findByUserOrderByCreatedAtDesc(User user);
}
