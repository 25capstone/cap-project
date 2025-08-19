package com.sns.backend.repository;

import com.sns.backend.entity.FollowRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRequestRepository extends JpaRepository<FollowRequest, Long> {

    // 같은 (requester, target, status) 조합의 최신 요청 1건 (멱등 처리용)
    Optional<FollowRequest> findTopByRequester_UserIdAndTarget_UserIdAndStatusOrderByRequestedAtDesc(
            Long requesterId, Long targetId, FollowRequest.Status status
    );

    // 내가 받은 PENDING 요청 목록
    List<FollowRequest> findAllByTarget_UserIdAndStatus(Long targetId, FollowRequest.Status status);

    // 내가 보낸 PENDING 요청 목록
    List<FollowRequest> findAllByRequester_UserIdAndStatus(Long requesterId, FollowRequest.Status status);

    // (옵션) 같은 쌍의 PENDING 요청이 이미 있는지 여부
    boolean existsByRequester_UserIdAndTarget_UserIdAndStatus(Long requesterId, Long targetId, FollowRequest.Status status);

    // (옵션) 요청 취소용 – 요청자 본인만 지우기
    void deleteByFollowRequestIdAndRequester_UserId(Long followRequestId, Long requesterId);
}
