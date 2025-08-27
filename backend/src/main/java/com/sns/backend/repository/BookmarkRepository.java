package com.sns.backend.repository;

import com.sns.backend.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUser_UserIdAndFeed_FeedId(Long userId, Long feedId);

    Optional<Bookmark> findByUser_UserIdAndFeed_FeedId(Long userId, Long feedId);

    void deleteByUser_UserIdAndFeed_FeedId(Long userId, Long feedId);

    long countByFeed_FeedId(Long feedId);

    @EntityGraph(attributePaths = {"feed", "feed.user"})
    Page<Bookmark> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
