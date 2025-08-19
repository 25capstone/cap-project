package com.sns.backend.service;

import com.sns.backend.dto.FeedLikeDTO;
import com.sns.backend.entity.Feed;
import com.sns.backend.entity.FeedLike;
import com.sns.backend.entity.User;
import com.sns.backend.repository.FeedLikeRepository;
import com.sns.backend.repository.FeedRepository;
import com.sns.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedLikeService {

    private final FeedLikeRepository feedLikeRepository;
    private final FeedRepository feedRepository; // 존재한다고 가정
    private final UserRepository userRepository; // 존재한다고 가정

    @Transactional
    public FeedLikeDTO like(Long userId, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 피드입니다: " + feedId));

        // ✅ 자기 피드 좋아요 방지
        if (feed.getUser().getUserId().equals(userId)) {
            throw new IllegalStateException("자신의 피드에는 좋아요를 누를 수 없습니다.");
        }
        // 이미 좋아요면 idempotent하게 현재 상태만 반환
        if (feedLikeRepository.existsByFeed_FeedIdAndUser_UserId(feedId, userId)) {
            long count = feedLikeRepository.countByFeed_FeedId(feedId);
            return FeedLikeDTO.builder()
                    .feedId(feedId)
                    .liked(true)
                    .likeCount(count)
                    .build();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + userId));

        FeedLike like = FeedLike.builder()
                .feed(feed)
                .user(user)
                .build();

        feedLikeRepository.save(like);

        long count = feedLikeRepository.countByFeed_FeedId(feedId);
        return FeedLikeDTO.builder()
                .feedId(feedId)
                .liked(true)
                .likeCount(count)
                .build();
    }

    @Transactional
    public FeedLikeDTO unlike(Long userId, Long feedId) {
        // 없으면 역시 idempotent하게 처리
        feedLikeRepository.deleteByFeed_FeedIdAndUser_UserId(feedId, userId);
        long count = feedLikeRepository.countByFeed_FeedId(feedId);
        return FeedLikeDTO.builder()
                .feedId(feedId)
                .liked(false)
                .likeCount(count)
                .build();
    }

    @Transactional(readOnly = true)
    public FeedLikeDTO getStatus(Long userId, Long feedId) {
        boolean liked = feedLikeRepository.existsByFeed_FeedIdAndUser_UserId(feedId, userId);
        long count = feedLikeRepository.countByFeed_FeedId(feedId);
        return FeedLikeDTO.builder()
                .feedId(feedId)
                .liked(liked)
                .likeCount(count)
                .build();
    }
}
