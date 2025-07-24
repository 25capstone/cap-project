package com.sns.backend.repository;

import com.sns.backend.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedRepository extends JpaRepository<Feed, Integer> {
    List<Feed> findByUserId(Integer userId);
}