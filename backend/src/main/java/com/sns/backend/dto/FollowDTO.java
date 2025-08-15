package com.sns.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowDTO {

    private Long followId;              // follow_id
    private Long followerId;            // follower_id
    private String followerDisplayname;    // 팔로워 닉네임
    private Long followedId;            // following_id
    private String followedDisplayname;    // 팔로잉 닉네임
    private LocalDateTime createdAt;    // created_at
}
