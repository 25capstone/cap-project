package com.sns.backend.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class FeedLikeDTO {
    private Long feedId;
    private boolean liked;   // 내가 현재 좋아요한 상태인지
    private long likeCount;  // 해당 피드 총 좋아요 수
}
