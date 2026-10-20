package com.example.mocktest.repository;

import com.example.mocktest.model.UserSectionEntitlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSectionEntitlementRepository
        extends JpaRepository<UserSectionEntitlement, Long> {

    boolean existsByUserIdAndSectionId(Long userId, Long sectionId);

    boolean existsByUserIdAndSectionIdIn(Long userId, List<Long> sectionIds);

    List<UserSectionEntitlement> findByUserIdOrderByGrantedAtDesc(Long userId);
}
