package com.sns.backend.service;

import com.sns.backend.dto.FeedDTO;
import com.sns.backend.entity.DailyRoute;
import com.sns.backend.entity.Feed;
import com.sns.backend.entity.User;
import com.sns.backend.repository.DailyRouteRepository;
import com.sns.backend.repository.FeedRepository;
import com.sns.backend.repository.FollowRepository;
import com.sns.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final DailyRouteRepository dailyRouteRepository;
    private final FollowRepository followRepository;

    /** 피드 생성 */
    @Transactional
    public Feed create(FeedDTO.Request dto, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Feed feed = new Feed();
        feed.setUser(user);

        // routeId가 있으면 소유자 검증까지 수행 (내가 만든 경로만 연결 허용)
        if (dto.routeId != null) {
            DailyRoute route = dailyRouteRepository.findById(dto.routeId)
                    .orElseThrow(() -> new IllegalArgumentException("Route not found"));
            if (!route.getUser().getUserId().equals(currentUserId)) {
                throw new AccessDeniedException("You cannot attach route of another user");
            }
            feed.setRoute(route);
        } else {
            feed.setRoute(null);
        }

        feed.setTitle(dto.title);
        feed.setContent(dto.content);

        return feedRepository.save(feed);
    }

    /** 내가 볼 수 있는 피드 목록 (나 + 내가 팔로우한 사용자들), 페이지네이션 */
    public Page<Feed> getVisibleFeeds(Long currentUserId, Pageable pageable) {
        return feedRepository.findVisibleFeeds(currentUserId, pageable);
    }


    /** 단건 조회 (비인증 접근 허용 시 me가 null일 수 있음. 필요 시 접근 정책은 여기서 추가) */
    public Optional<Feed> get(Long feedId, Long meUserIdOrNull) {
        Optional<Feed> opt = feedRepository.findById(feedId);
        if (opt.isEmpty()) return Optional.empty();

        Feed feed = opt.get();
        Long ownerId = feed.getUser().getUserId();

        // 로그인 안 한 경우 → PUBLIC만 허용
        if (meUserIdOrNull == null) {
            return isPublic(feed) ? Optional.of(feed) : Optional.empty();
        }

        Long me = meUserIdOrNull;

        // 본인 글은 항상 허용
        if (ownerId.equals(me)) return Optional.of(feed);

        // 공개 범위별 분기
        if (isPublic(feed)) {
            return Optional.of(feed);
        }

        if (isFollowersOnly(feed)) {
            // ✅ "맞팔" 정책 (네 JPQL과 동일)
            boolean meFollowsOwner = followRepository
                    .existsByFollower_UserIdAndFollowed_UserId(me, ownerId);
            boolean ownerFollowsMe = followRepository
                    .existsByFollower_UserIdAndFollowed_UserId(ownerId, me);

            if (meFollowsOwner && ownerFollowsMe) {
                return Optional.of(feed);
            } else {
                return Optional.empty(); // 팔로잉 관계 미충족 → 비공개 처리
            }
        }

        // PRIVATE 등 그 외 → 비공개
        return Optional.empty();
    }

    /** 수정 (작성자 본인만) */
    @Transactional
    public Feed update(Long feedId, FeedDTO.Request dto, Long currentUserId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("Feed not found"));

        if (!feed.getUser().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to update this feed");
        }

        // route 변경 시에도 소유자 검증
        if (dto.routeId != null) {
            DailyRoute route = dailyRouteRepository.findById(dto.routeId)
                    .orElseThrow(() -> new IllegalArgumentException("Route not found"));
            if (!route.getUser().getUserId().equals(currentUserId)) {
                throw new AccessDeniedException("You cannot attach route of another user");
            }
            feed.setRoute(route);
        } else {
            feed.setRoute(null);
        }

        feed.setTitle(dto.title);
        feed.setContent(dto.content);

        return feedRepository.save(feed);
    }

    /** 삭제 (작성자 본인만) */
    @Transactional
    public void delete(Long feedId, Long currentUserId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("Feed not found"));

        if (!feed.getUser().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to delete this feed");
        }

        feedRepository.delete(feed);
    }

    /** (필요 시) 기존 시그니처도 유지해주고 싶으면 그대로 남겨둠 */
    public List<Long> getFollowedUserIds(Long currentUserId) {
        return followRepository.findFollowedUserIdsByFollowerId(currentUserId);
    }

    private boolean isPublic(Feed feed) {
        // enum 이라면: return feed.getUser().getVisibility() == Visibility.PUBLIC;
        return "PUBLIC".equalsIgnoreCase(String.valueOf(feed.getUser().getVisibility()));
    }

    private boolean isFollowersOnly(Feed feed) {
        // enum 이라면: return feed.getUser().getVisibility() == Visibility.FOLLOWERS;
        return "FOLLOWERS".equalsIgnoreCase(String.valueOf(feed.getUser().getVisibility()));
    }
}