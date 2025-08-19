package com.sns.backend.repository;

import com.sns.backend.entity.Feed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface FeedRepository extends JpaRepository<Feed, Long> {

    @EntityGraph(attributePaths = {"user", "route"})
    @Query(
            value = """
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
        ORDER BY f.createdAt DESC
        """,
            countQuery = """
        SELECT COUNT(f) FROM Feed f
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
        """
    )
    Page<Feed> findVisibleFeeds(@Param("currentUserId") Long currentUserId, Pageable pageable);
}
