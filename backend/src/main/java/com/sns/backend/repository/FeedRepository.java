package com.sns.backend.repository;

import com.sns.backend.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedRepository extends JpaRepository<Feed, Long> {

    @Query("""
    SELECT f FROM Feed f
    WHERE
        f.user.visibility = 'PUBLIC'
        OR f.user.userId = :currentUserId
        OR (
            f.user.visibility = 'FOLLOWERS'
            AND EXISTS (
                SELECT 1 FROM Follow fl1
                WHERE fl1.follower.userId = :currentUserId
                  AND fl1.followed.userId = f.user.userId
            )
            AND EXISTS (
                SELECT 1 FROM Follow fl2
                WHERE fl2.follower.userId = f.user.userId
                  AND fl2.followed.userId = :currentUserId
            )
        )
""")
    List<Feed> findVisibleFeeds(
            @Param("currentUserId") Long currentUserId);
}
