package com.sns.backend.dto;

import com.sns.backend.entity.Feed;
import lombok.Data;

import java.time.LocalDateTime;

public class FeedDTO {

    @Data
    public static class FeedRequestDTO {
        private String content;
        private String imageUrl;
        private Integer userId;

        public Feed toEntity() {
            Feed feed = new Feed();
            feed.setContent(this.content);
            feed.setImageUrl(this.imageUrl);
            feed.setUserId(this.userId);
            feed.setCreatedAt(LocalDateTime.now());
            feed.setUpdatedAt(LocalDateTime.now());
            return feed;
        }
    }

    @Data
    public static class FeedResponseDTO {
        private Integer feedId;
        private String content;
        private String imageUrl;
        private Integer userId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static FeedResponseDTO fromEntity(Feed feed) {
            FeedResponseDTO dto = new FeedResponseDTO();
            dto.setFeedId(feed.getFeedId());
            dto.setContent(feed.getContent());
            dto.setImageUrl(feed.getImageUrl());
            dto.setUserId(feed.getUserId());
            dto.setCreatedAt(feed.getCreatedAt());
            dto.setUpdatedAt(feed.getUpdatedAt());
            return dto;
        }
    }
}
