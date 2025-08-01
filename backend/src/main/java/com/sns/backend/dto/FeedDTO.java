package com.sns.backend.dto;

import com.sns.backend.entity.Feed;
import lombok.Data;

import java.time.LocalDateTime;

public class FeedDTO {

    public static class Request {
        public Long userId;
        public Long routeId;
        public String title;
        public String content;
    }

    public static class Response {
        public Long feedId;
        public Long userId;
        public Long routeId;
        public String title;
        public String content;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;

        public static Response fromEntity(Feed feed) {
            Response dto = new Response();
            dto.feedId = feed.getFeedId();
            dto.userId = feed.getUser().getUserId();
            dto.routeId = feed.getRoute() != null ? feed.getRoute().getRouteId() : null;  // null 체크 추가
            dto.title = feed.getTitle();
            dto.content = feed.getContent();
            dto.createdAt = feed.getCreatedAt();
            dto.updatedAt = feed.getUpdatedAt();
            return dto;
        }

    }
}

