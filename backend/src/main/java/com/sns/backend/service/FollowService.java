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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final FollowRequestRepository followRequestRepository;
    private final UserRepository userRepository;

    // 팔로우 요청 보내기
    @Transactional
    public FollowRequestDTO sendFollowRequest(Long requesterId, Long targetId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("Requester not found"));
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Target not found"));

        FollowRequest followRequest = FollowRequest.builder()
                .requester(requester)
                .target(target)
                .status(FollowRequest.Status.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        FollowRequest saved = followRequestRepository.save(followRequest);

        return mapToDto(saved);
    }

    // 팔로우 요청 수락
    @Transactional
    public FollowDTO acceptFollowRequest(Long followRequestId) {
        FollowRequest request = followRequestRepository.findById(followRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Follow request not found"));

        request.setStatus(FollowRequest.Status.ACCEPTED);

        // 팔로우 테이블에 2번 저장 (서로 팔로워/팔로잉)
        Follow follow1 = Follow.builder()
                .follower(request.getRequester())
                .followed(request.getTarget())
                .createdAt(LocalDateTime.now())
                .build();

        Follow follow2 = Follow.builder()
                .follower(request.getTarget())
                .followed(request.getRequester())
                .createdAt(LocalDateTime.now())
                .build();

        followRepository.save(follow1);
        followRepository.save(follow2);

        return mapToDto(follow1);
    }

    // 내가 팔로우한 사람 ID 리스트
    public List<Long> getFollowedUserIds(Long followerId) {
        return followRepository.findFollowedUserIdsByFollowerId(followerId);
    }

    // 특정 사용자끼리 팔로우 관계 존재 여부
    public boolean isFollowing(Long followerId, Long followedId) {
        return followRepository.existsByFollower_UserIdAndFollowed_UserId(followerId, followedId);
    }

    // 내가 받은 팔로우 요청 목록
    public List<FollowRequestDTO> getReceivedFollowRequests(Long targetId) {
        List<FollowRequest> requests = followRequestRepository.findAllByTarget_UserIdAndStatus(targetId, FollowRequest.Status.PENDING);
        return requests.stream()
                .map(FollowRequestDTO::fromEntity)
                .toList();
    }

    // 내가 보낸 팔로우 요청 목록
    public List<FollowRequestDTO> getSentFollowRequests(Long requesterId) {
        List<FollowRequest> requests = followRequestRepository.findAllByRequester_UserIdAndStatus(requesterId, FollowRequest.Status.PENDING);
        return requests.stream()
                .map(FollowRequestDTO::fromEntity)
                .toList();
    }


    // FollowRequest -> DTO
    private FollowRequestDTO mapToDto(FollowRequest fr) {
        return FollowRequestDTO.builder()
                .followRequestId(fr.getFollowRequestId())
                .requesterId(fr.getRequester().getUserId())
                .requesterDisplayName(fr.getRequester().getDisplayName())
                .targetId(fr.getTarget().getUserId())
                .targetDisplayname(fr.getTarget().getDisplayName())
                .status(fr.getStatus())
                .requestedAt(fr.getRequestedAt())
                .build();
    }

    // Follow -> DTO
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
