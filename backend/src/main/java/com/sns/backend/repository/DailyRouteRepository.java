package com.sns.backend.repository;

import com.sns.backend.entity.DailyRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DailyRouteRepository extends JpaRepository<DailyRoute, Long> {
    List<DailyRoute> findByUser_UserId(Long userId);
}
