package com.sns.backend.repository;

import com.sns.backend.entity.FollowRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRequestRepository extends JpaRepository<FollowRequest, Long> {
    List<FollowRequest> findAllByTarget_UserIdAndStatus(Long targetId, FollowRequest.Status status);

    List<FollowRequest> findAllByRequester_UserIdAndStatus(Long requesterId, FollowRequest.Status status);
}
