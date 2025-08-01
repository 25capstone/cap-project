package com.sns.backend.service;

import com.sns.backend.dto.FeedDTO;
import com.sns.backend.entity.DailyRoute;
import com.sns.backend.entity.Feed;
import com.sns.backend.entity.User;
import com.sns.backend.repository.DailyRouteRepository;
import com.sns.backend.repository.FeedRepository;
import com.sns.backend.repository.FollowRepository;
import com.sns.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FeedService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final DailyRouteRepository dailyRouteRepository;
    private final FollowRepository followRepository;

    public FeedService(FeedRepository feedRepository, UserRepository userRepository, DailyRouteRepository dailyRouteRepository, FollowRepository followRepository) {
        this.feedRepository = feedRepository;
        this.userRepository = userRepository;
        this.dailyRouteRepository = dailyRouteRepository;
        this.followRepository = followRepository;
    }

    // 생성 시 userId는 로그인한 사용자 ID로 받음
    public Feed create(FeedDTO.Request dto, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Feed feed = new Feed();
        feed.setUser(user);

        if (dto.routeId != null) {
            DailyRoute route = dailyRouteRepository.findById(dto.routeId)
                    .orElseThrow(() -> new IllegalArgumentException("Route not found"));
            feed.setRoute(route);
        } else {
            feed.setRoute(null);
        }
        feed.setTitle(dto.title);
        feed.setContent(dto.content);

        return feedRepository.save(feed);
    }

    public List<Long> getFollowedUserIds(Long currentUserId) {
        return followRepository.findFollowedUserIdsByFollowerId(currentUserId);
    }

    public List<Feed> getVisibleFeeds(Long currentUserId, List<Long> followedUserIds) {
        return feedRepository.findVisibleFeeds(currentUserId, followedUserIds);
    }

    public Optional<Feed> get(Long feedId) {
        return feedRepository.findById(feedId);
    }

    // 수정 시 작성자와 로그인 사용자 일치 여부 체크
    public Feed update(Long feedId, FeedDTO.Request dto, Long currentUserId) {
        Feed feed = feedRepository.findById(feedId).orElseThrow(() -> new IllegalArgumentException("Feed not found"));

        if (!feed.getUser().getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("You do not have permission to update this feed");
        }

        feed.setTitle(dto.title);
        feed.setContent(dto.content);
        return feedRepository.save(feed);
    }

    // 삭제 시 작성자와 로그인 사용자 일치 여부 체크
    public void delete(Long feedId, Long currentUserId) {
        Feed feed = feedRepository.findById(feedId).orElseThrow(() -> new IllegalArgumentException("Feed not found"));

        if (!feed.getUser().getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("You do not have permission to delete this feed");
        }

        feedRepository.deleteById(feedId);
    }
}
