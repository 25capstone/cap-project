package com.sns.backend.dto;

import com.sns.backend.entity.Comment;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class CommentDTO {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank
        @Size(max = 2000)
        private String content;
    }

    @Getter @Setter
    public static class UpdateRequest {
        @NotBlank
        @Size(max = 2000)
        private String content;
    }

    @Getter @Builder
    public static class Response {
        private final Long commentId;
        private final Long feedId;
        private final Long userId;
        private final String userDisplayName;
        private final String content;
        private final boolean mine;          // 내가 쓴 댓글인지
        private final LocalDateTime createdAt;
        private final LocalDateTime updatedAt;

        public static Response fromEntity(Comment c, Long meUserId) {
            boolean mine = meUserId != null && c.getUser().getUserId().equals(meUserId);
            return Response.builder()
                    .commentId(c.getCommentId())
                    .feedId(c.getFeed().getFeedId())
                    .userId(c.getUser().getUserId())
                    .userDisplayName(c.getUser().getDisplayName())
                    .content(c.getContent())
                    .mine(mine)
                    .createdAt(c.getCreatedAt())
                    .updatedAt(c.getUpdatedAt())
                    .build();
        }
    }
}
