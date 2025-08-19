package com.sns.backend.repository;

import com.sns.backend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 댓글 목록 (작성자 user 를 같이 로딩하여 N+1 완화)
    @EntityGraph(attributePaths = {"user"})
    Page<Comment> findByFeed_FeedIdOrderByCreatedAtAsc(Long feedId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "feed", "feed.user"})
    Optional<Comment> findWithFeedAndUserByCommentId(Long commentId);

    long countByFeed_FeedId(Long feedId);
}
