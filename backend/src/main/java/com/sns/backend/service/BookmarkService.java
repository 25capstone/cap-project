package com.sns.backend.service;

import com.sns.backend.dto.BookmarkDTO;
import com.sns.backend.entity.Bookmark;
import com.sns.backend.entity.Feed;
import com.sns.backend.entity.User;
import com.sns.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    /** 북마크 생성(멱등) */
    @Transactional
    public BookmarkDTO.StatusResponse bookmark(Long me, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("Feed not found"));

        if (!canView(feed, me)) {
            throw new AccessDeniedException("You cannot bookmark this feed.");
        }

        User user = userRepository.findById(me)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!bookmarkRepository.existsByUser_UserIdAndFeed_FeedId(me, feedId)) {
            try {
                bookmarkRepository.save(Bookmark.builder().user(user).feed(feed).build());
            } catch (DataIntegrityViolationException ignore) { /* 동시성 중복은 무시 */ }
        }

        long cnt = bookmarkRepository.countByFeed_FeedId(feedId);
        return BookmarkDTO.StatusResponse.builder()
                .feedId(feedId).bookmarked(true).bookmarkCount(cnt).build();
    }

    /** 북마크 취소(멱등) */
    @Transactional
    public BookmarkDTO.StatusResponse unbookmark(Long me, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("Feed not found"));

        if (!canView(feed, me)) {
            throw new AccessDeniedException("You cannot unbookmark this feed.");
        }

        bookmarkRepository.deleteByUser_UserIdAndFeed_FeedId(me, feedId);
        long cnt = bookmarkRepository.countByFeed_FeedId(feedId);
        return BookmarkDTO.StatusResponse.builder()
                .feedId(feedId).bookmarked(false).bookmarkCount(cnt).build();
    }

    /** 내 북마크 상태 + 카운트 */
    public BookmarkDTO.StatusResponse getStatus(Long me, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("Feed not found"));

        if (!canView(feed, me)) {
            throw new AccessDeniedException("You cannot view this feed.");
        }

        boolean bookmarked = bookmarkRepository.existsByUser_UserIdAndFeed_FeedId(me, feedId);
        long cnt = bookmarkRepository.countByFeed_FeedId(feedId);
        return BookmarkDTO.StatusResponse.builder()
                .feedId(feedId).bookmarked(bookmarked).bookmarkCount(cnt).build();
    }

    /** 내가 북마크한 피드 목록 */
    public Page<BookmarkDTO.ListItem> listMine(Long me, Pageable pageable) {
        return bookmarkRepository.findByUser_UserIdOrderByCreatedAtDesc(me, pageable)
                .map(BookmarkDTO.ListItem::fromEntity);
    }

    /** (선택) 피드 북마크 수 */
    public long countForFeed(Long me, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("Feed not found"));
        if (!canView(feed, me)) throw new AccessDeniedException("You cannot view this feed.");
        return bookmarkRepository.countByFeed_FeedId(feedId);
    }

    // ---- visibility helper (맞팔 정책) ----
    private boolean canView(Feed feed, Long me) {
        Long ownerId = feed.getUser().getUserId();

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
            return meFollowsOwner && ownerFollowsMe;
        }
        return false;
    }
}
