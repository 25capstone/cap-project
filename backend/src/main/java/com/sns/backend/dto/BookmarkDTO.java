package com.sns.backend.dto;

import com.sns.backend.entity.Bookmark;
import lombok.*;

import java.time.LocalDateTime;

public class BookmarkDTO {

    @Getter @Builder
    public static class StatusResponse {
        private final Long feedId;
        private final boolean bookmarked;
        private final long bookmarkCount;
    }

    @Getter @Builder
    public static class ListItem {
        private final Long bookmarkId;
        private final Long feedId;
        private final String title;
        private final Long authorId;
        private final String authorDisplayName;
        private final LocalDateTime bookmarkedAt;

        public static ListItem fromEntity(Bookmark b) {
            return ListItem.builder()
                    .bookmarkId(b.getBookmarkId())
                    .feedId(b.getFeed().getFeedId())
                    .title(b.getFeed().getTitle())
                    .authorId(b.getFeed().getUser().getUserId())
                    .authorDisplayName(b.getFeed().getUser().getDisplayName())
                    .bookmarkedAt(b.getCreatedAt())
                    .build();
        }
    }
}
