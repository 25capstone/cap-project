package com.sns.backend.service;

import com.sns.backend.dto.FollowDTO;
import com.sns.backend.dto.FollowRequestDTO;
import com.sns.backend.entity.Follow;
import com.sns.backend.entity.FollowRequest;
import com.sns.backend.entity.User;
import com.sns.backend.repository.FollowRepository;
import com.sns.backend.repository.FollowRequestRepository;
import com.sns.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;
    private final FollowRequestRepository followRequestRepository;
    private final UserRepository userRepository;

    /** 팔로우 요청 보내기 (멱등) */
    @Transactional
    public FollowRequestDTO sendFollowRequest(Long requesterId, Long targetId) {
        if (requesterId.equals(targetId)) {
            throw new IllegalArgumentException("You cannot send a request to yourself.");
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("Requester not found"));
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Target not found"));

        // 이미 서로 팔로우 된 상태면 요청 불가
        boolean reqFollowsTgt = followRepository
                .existsByFollower_UserIdAndFollowed_UserId(requesterId, targetId);
        boolean tgtFollowsReq = followRepository
                .existsByFollower_UserIdAndFollowed_UserId(targetId, requesterId);
        if (reqFollowsTgt && tgtFollowsReq) {
            throw new IllegalStateException("Already following each other.");
        }

        // 같은 쌍의 PENDING 요청이 이미 있으면 그걸 반환(멱등)
        FollowRequest pending = followRequestRepository
                .findTopByRequester_UserIdAndTarget_UserIdAndStatusOrderByRequestedAtDesc(
                        requesterId, targetId, FollowRequest.Status.PENDING
                )
                .orElse(null);
        if (pending != null) {
            return FollowRequestDTO.fromEntity(pending);
        }

        FollowRequest followRequest = FollowRequest.builder()
                .requester(requester)
                .target(target)
                .status(FollowRequest.Status.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        FollowRequest saved = followRequestRepository.save(followRequest);
        return FollowRequestDTO.fromEntity(saved);
    }

    /** 팔로우 요청 수락 (요청 '대상자'만 가능) */
    @Transactional
    public FollowDTO acceptFollowRequest(Long followRequestId, Long meUserId) {
        FollowRequest request = followRequestRepository.findById(followRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Follow request not found"));

        // 권한 체크: 이 요청의 대상자가 현재 사용자여야 함
        if (!request.getTarget().getUserId().equals(meUserId)) {
            throw new AccessDeniedException("You are not allowed to accept this request.");
        }

        // 이미 처리된 요청이면 멱등 처리
        if (request.getStatus() == FollowRequest.Status.ACCEPTED) {
            // 이미 ACCEPTED면 현재 팔로우 상태 중 하나를 DTO로 반환
            // 우선순위: requester -> target 방향
            Follow existed = followRepository
                    .findTopByFollower_UserIdAndFollowed_UserIdOrderByCreatedAtDesc(
                            request.getRequester().getUserId(),
                            request.getTarget().getUserId()
                    )
                    .orElse(null);
            if (existed != null) return mapToDto(existed);
        } else if (request.getStatus() == FollowRequest.Status.REJECTED) {
            throw new IllegalStateException("This request was already rejected.");
        }

        // 요청 상태 갱신
        request.setStatus(FollowRequest.Status.ACCEPTED);

        // 양방향 팔로우가 정책이라면 둘 다 생성.
        // 이미 있는 방향은 건너뛰어 DataIntegrityViolation 방지
        Follow follow1 = null;
        if (!followRepository.existsByFollower_UserIdAndFollowed_UserId(
                request.getRequester().getUserId(), request.getTarget().getUserId())) {
            follow1 = Follow.builder()
                    .follower(request.getRequester())
                    .followed(request.getTarget())
                    .createdAt(LocalDateTime.now())
                    .build();
            followRepository.save(follow1);
        }

        if (!followRepository.existsByFollower_UserIdAndFollowed_UserId(
                request.getTarget().getUserId(), request.getRequester().getUserId())) {
            Follow follow2 = Follow.builder()
                    .follower(request.getTarget())
                    .followed(request.getRequester())
                    .createdAt(LocalDateTime.now())
                    .build();
            followRepository.save(follow2);
        }

        // 반환은 요청자 -> 대상자 방향으로 우선
        if (follow1 == null) {
            // 이미 있던 경우 찾아서 반환
            Follow existed = followRepository
                    .findTopByFollower_UserIdAndFollowed_UserIdOrderByCreatedAtDesc(
                            request.getRequester().getUserId(),
                            request.getTarget().getUserId()
                    )
                    .orElseThrow(() -> new IllegalStateException("Follow link missing unexpectedly."));
            return mapToDto(existed);
        }
        return mapToDto(follow1);
    }

    /** 팔로우 요청 거절 (요청 '대상자'만 가능) */
    @Transactional
    public FollowRequestDTO rejectFollowRequest(Long followRequestId, Long meUserId) {
        FollowRequest request = followRequestRepository.findById(followRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Follow request not found"));

        if (!request.getTarget().getUserId().equals(meUserId)) {
            throw new AccessDeniedException("You are not allowed to reject this request.");
        }

        if (request.getStatus() == FollowRequest.Status.ACCEPTED) {
            throw new IllegalStateException("This request was already accepted.");
        }
        if (request.getStatus() == FollowRequest.Status.REJECTED) {
            return FollowRequestDTO.fromEntity(request); // 멱등
        }

        request.setStatus(FollowRequest.Status.REJECTED);
        return FollowRequestDTO.fromEntity(request);
    }

    /** (옵션) 내가 보낸 팔로우 요청 취소 */
    @Transactional
    public void cancelFollowRequest(Long followRequestId, Long meUserId) {
        FollowRequest request = followRequestRepository.findById(followRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Follow request not found"));

        if (!request.getRequester().getUserId().equals(meUserId)) {
            throw new AccessDeniedException("You are not allowed to cancel this request.");
        }
        if (request.getStatus() != FollowRequest.Status.PENDING) {
            throw new IllegalStateException("Only pending requests can be cancelled.");
        }
        followRequestRepository.delete(request);
    }

    /** 내가 팔로우한 사람 ID 리스트 */
    public List<Long> getFollowedUserIds(Long followerId) {
        return followRepository.findFollowedUserIdsByFollowerId(followerId);
    }

    /** 특정 사용자끼리 팔로우 관계 존재 여부 */
    public boolean isFollowing(Long followerId, Long followedId) {
        return followRepository.existsByFollower_UserIdAndFollowed_UserId(followerId, followedId);
    }

    /** 내가 받은 팔로우 요청 목록 (PENDING) */
    public List<FollowRequestDTO> getReceivedFollowRequests(Long targetId) {
        return followRequestRepository
                .findAllByTarget_UserIdAndStatus(targetId, FollowRequest.Status.PENDING)
                .stream().map(FollowRequestDTO::fromEntity).toList();
    }

    /** 내가 보낸 팔로우 요청 목록 (PENDING) */
    public List<FollowRequestDTO> getSentFollowRequests(Long requesterId) {
        return followRequestRepository
                .findAllByRequester_UserIdAndStatus(requesterId, FollowRequest.Status.PENDING)
                .stream().map(FollowRequestDTO::fromEntity).toList();
    }

    // ---- private mappers ----
    private FollowDTO mapToDto(Follow f) {
        return FollowDTO.builder()
                .followId(f.getFollowId())
                .followerId(f.getFollower().getUserId())
                .followerDisplayname(f.getFollower().getDisplayName())
                .followedId(f.getFollowed().getUserId())
                .followedDisplayname(f.getFollowed().getDisplayName())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
