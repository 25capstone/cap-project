package com.sns.backend.service;

import com.sns.backend.dto.CommentDTO;
import com.sns.backend.entity.Comment;
import com.sns.backend.entity.Feed;
import com.sns.backend.entity.User;
import com.sns.backend.repository.CommentRepository;
import com.sns.backend.repository.FeedRepository;
import com.sns.backend.repository.FollowRepository;
import com.sns.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    /** 댓글 작성 */
    @Transactional
    public Comment create(Long feedId, Long meUserId, CommentDTO.CreateRequest req) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("Feed not found"));

        // 가시성 체크: 이 피드를 내가 볼 수 있어야 댓글 가능
        if (!canView(feed, meUserId)) {
            throw new AccessDeniedException("You cannot comment this feed.");
        }

        User me = userRepository.findById(meUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Comment c = Comment.builder()
                .feed(feed)
                .user(me)
                .content(req.getContent())
                .build();

        return commentRepository.save(c);
    }

    /** 댓글 목록 (가시성 체크 포함) */
    public Page<Comment> list(Long feedId, Long meUserId, Pageable pageable) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("Feed not found"));

        if (!canView(feed, meUserId)) {
            throw new AccessDeniedException("You cannot view this feed's comments.");
        }

        return commentRepository.findByFeed_FeedIdOrderByCreatedAtAsc(feedId, pageable);
    }

    /** 댓글 수정 (작성자 본인만) */
    @Transactional
    public Comment update(Long commentId, Long meUserId, CommentDTO.UpdateRequest req) {
        Comment c = commentRepository.findWithFeedAndUserByCommentId(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (!c.getUser().getUserId().equals(meUserId)) {
            throw new AccessDeniedException("You do not have permission to edit this comment.");
        }

        // 피드 가시성도 한 번 더 방어적으로 점검(정책에 따라 생략 가능)
        if (!canView(c.getFeed(), meUserId)) {
            throw new AccessDeniedException("You cannot view this feed.");
        }

        c.setContent(req.getContent());
        return c; // dirty checking으로 업데이트
    }

    /** 댓글 삭제 (작성자 or 피드 작성자) */
    @Transactional
    public void delete(Long commentId, Long meUserId) {
        Comment c = commentRepository.findWithFeedAndUserByCommentId(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        boolean isAuthor = c.getUser().getUserId().equals(meUserId);
        boolean isFeedOwner = c.getFeed().getUser().getUserId().equals(meUserId);

        if (!isAuthor && !isFeedOwner) {
            throw new AccessDeniedException("You do not have permission to delete this comment.");
        }

        commentRepository.delete(c);
    }

    // ---- visibility helper ----
    private boolean canView(Feed feed, Long me) {
        Long ownerId = feed.getUser().getUserId();

        // 비로그인 사용자는 PUBLIC만
        if (me == null) {
            return "PUBLIC".equalsIgnoreCase(String.valueOf(feed.getUser().getVisibility()));
        }

        if (ownerId.equals(me)) return true;

        String vis = String.valueOf(feed.getUser().getVisibility());
        if ("PUBLIC".equalsIgnoreCase(vis)) return true;

        if ("FOLLOWERS".equalsIgnoreCase(vis)) {
            boolean meFollowsOwner = followRepository
                    .existsByFollower_UserIdAndFollowed_UserId(me, ownerId);
            boolean ownerFollowsMe = followRepository
                    .existsByFollower_UserIdAndFollowed_UserId(ownerId, me);
            return meFollowsOwner && ownerFollowsMe; // 맞팔 정책
        }

        return false; // PRIVATE 등
    }

    /** 댓글 수 조회 (가시성 체크 포함) */
    public long countForFeed(Long feedId, Long meUserId) {
        var feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("Feed not found"));

        if (!canView(feed, meUserId)) {
            throw new AccessDeniedException("You cannot view this feed's comments.");
        }

        return commentRepository.countByFeed_FeedId(feedId);
    }
}
