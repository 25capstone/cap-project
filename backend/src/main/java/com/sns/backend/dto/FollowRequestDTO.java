package com.sns.backend.dto;

import com.sns.backend.entity.FollowRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class FollowRequestDTO {

    private Long followRequestId;
    private Long requesterId;
    private String requesterDisplayName;
    private Long targetId;
    private String targetDisplayname;
    private FollowRequest.Status status;
    private LocalDateTime requestedAt;

    // Entity → DTO 변환
    public static FollowRequestDTO fromEntity(FollowRequest followRequest) {
        return FollowRequestDTO.builder()
                .followRequestId(followRequest.getFollowRequestId())
                .requesterId(followRequest.getRequester().getUserId())
                .requesterDisplayName(followRequest.getRequester().getDisplayName())
                .targetId(followRequest.getTarget().getUserId())
                .targetDisplayname(followRequest.getTarget().getDisplayName())
                .status(followRequest.getStatus())
                .requestedAt(followRequest.getRequestedAt())
                .build();
    }
}
