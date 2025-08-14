package com.sns.backend.controller;

import com.sns.backend.dto.FeedDTO;
import com.sns.backend.entity.Feed;
import com.sns.backend.service.FeedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sns.backend.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/feeds")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }
    //public Long currentUserId = 1651615L; // 테스트용!! 아이디 지정한거임

    @PostMapping
    public ResponseEntity<FeedDTO.Response> create(@RequestBody FeedDTO.Request dto,
                                                   @AuthenticationPrincipal CustomUserDetails principal) {

        Long currentUserId = principal.getUserId();
        Feed created = feedService.create(dto, currentUserId);
        return ResponseEntity.ok(FeedDTO.Response.fromEntity(created));
    }

    @GetMapping("/visible")
    public ResponseEntity<List<FeedDTO.Response>> getVisibleFeeds(
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long currentUserId = principal.getUserId();
        List<Long> followedUserIds = feedService.getFollowedUserIds(currentUserId);
        List<Feed> feeds = feedService.getVisibleFeeds(currentUserId, followedUserIds);

        List<FeedDTO.Response> result = feeds.stream()
                .map(FeedDTO.Response::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }


    @GetMapping("/{feedId}")
    public ResponseEntity<FeedDTO.Response> get(@PathVariable Long feedId) {
        return feedService.get(feedId)
                .map(FeedDTO.Response::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{feedId}")
    public ResponseEntity<FeedDTO.Response> update(@PathVariable Long feedId,
                                                   @RequestBody FeedDTO.Request dto,
                                                   @AuthenticationPrincipal CustomUserDetails principal) {
        Long currentUserId = principal.getUserId();
        Feed updated = feedService.update(feedId, dto, currentUserId);
        return ResponseEntity.ok(FeedDTO.Response.fromEntity(updated));
    }

    @DeleteMapping("/{feedId}")
    public ResponseEntity<Void> delete(@PathVariable Long feedId,
                                       @AuthenticationPrincipal CustomUserDetails principal) {
        Long currentUserId = principal.getUserId();
        feedService.delete(feedId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
