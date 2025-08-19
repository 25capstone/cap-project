package com.sns.backend.repository;

import com.sns.backend.entity.FeedLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {
    boolean existsByFeed_FeedIdAndUser_UserId(Long feedId, Long userId);
    Optional<FeedLike> findByFeed_FeedIdAndUser_UserId(Long feedId, Long userId);
    long countByFeed_FeedId(Long feedId);
    void deleteByFeed_FeedIdAndUser_UserId(Long feedId, Long userId);
}
