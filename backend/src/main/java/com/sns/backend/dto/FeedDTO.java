package com.sns.backend.dto;

import com.sns.backend.entity.Feed;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class FeedDTO {

    public static class Request {
        public Long userId;        // (참고) 실제로는 principal에서 가져오므로 서버에서 무시하는 걸 권장
        public Long routeId;
        public String title;
        public String content;
        /** 선택: 클라이언트가 직접 태그를 보낼 때 사용. 없으면 서버가 본문에서 추출 가능 */
        public List<String> hashtags;
    }

    public static class Response {
        public Long feedId;
        public Long userId;                     // 작성자 ID
        public Long routeId;
        public String title;
        public String content;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;

        /** ← 추가 필드들 */
        public String authorDisplayName;        // 작성자 표시명
        public String authorProfileImageUrl;    // 작성자 프로필 이미지(있으면)
        public List<String> hashtags;           // 해시태그 목록(소문자 정규화 권장)

        public long likeCount;                  // 좋아요 수
        public long bookmarkCount;              // 북마크 수
        public long commentCount;               // 댓글 수

        public boolean likedByMe;               // 내가 좋아요 눌렀는지
        public boolean bookmarkedByMe;          // 내가 북마크 했는지

        /** 기존 호환용: 추가 필드는 기본값으로 채움 */
        public static Response fromEntity(Feed feed) {
            Response dto = new Response();
            dto.feedId = feed.getFeedId();
            dto.userId = feed.getUser().getUserId();
            dto.routeId = (feed.getRoute() != null) ? feed.getRoute().getRouteId() : null;
            dto.title = feed.getTitle();
            dto.content = feed.getContent();
            dto.createdAt = feed.getCreatedAt();
            dto.updatedAt = feed.getUpdatedAt();

            // 추가 필드 기본값
            dto.authorDisplayName = feed.getUser().getDisplayName();
            dto.authorProfileImageUrl = feed.getUser().getProfileImageUrl(); // 엔티티에 없으면 제거
            dto.hashtags = Collections.emptyList();

            dto.likeCount = 0L;
            dto.bookmarkCount = 0L;
            dto.commentCount = 0L;
            dto.likedByMe = false;
            dto.bookmarkedByMe = false;
            return dto;
        }

        /**
         * 확장 팩토리: 해시태그/카운트/내 상태까지 채워 반환
         */
        public static Response fromEntity(
                Feed feed,
                List<String> hashtags,
                long likeCount,
                long bookmarkCount,
                long commentCount,
                boolean likedByMe,
                boolean bookmarkedByMe
        ) {
            Response dto = fromEntity(feed);
            dto.hashtags = (hashtags != null) ? hashtags : Collections.emptyList();
            dto.likeCount = likeCount;
            dto.bookmarkCount = bookmarkCount;
            dto.commentCount = commentCount;
            dto.likedByMe = likedByMe;
            dto.bookmarkedByMe = bookmarkedByMe;
            return dto;
        }
    }
}
