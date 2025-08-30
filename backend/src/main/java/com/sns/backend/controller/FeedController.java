package com.sns.backend.controller;

import com.sns.backend.common.ApiResponse;
import com.sns.backend.dto.FeedDTO;
import com.sns.backend.entity.Feed;
import com.sns.backend.security.CustomUserDetails;
import com.sns.backend.service.FeedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/feeds")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ApiResponse<FeedDTO.Response>> create(
            @Valid @RequestBody FeedDTO.Request dto,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long currentUserId = principal.getUserId();
        Feed created = feedService.create(dto, currentUserId);

        URI location = URI.create("/api/v1/feeds/" + created.getFeedId());
        ApiResponse<FeedDTO.Response> body =
                ApiResponse.success(FeedDTO.Response.fromEntity(created), "피드 작성 성공", 201);

        return ResponseEntity
                .created(location)
                .header(HttpHeaders.LOCATION, location.toString())
                .body(body);
    }

    // 페이지네이션 버전 (권장)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/visible")
    public ResponseEntity<ApiResponse<Page<FeedDTO.Response>>> getVisibleFeeds(
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long me = principal.getUserId();
        Page<Feed> page = feedService.getVisibleFeeds(me, pageable);
        Page<FeedDTO.Response> mapped = page.map(FeedDTO.Response::fromEntity);

        return ResponseEntity.ok(ApiResponse.success(mapped, "조회 성공", 200));
    }

    @GetMapping("/{feedId}")
    public ResponseEntity<ApiResponse<FeedDTO.Response>> get(
            @PathVariable Long feedId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long me = (principal != null) ? principal.getUserId() : null;

        return feedService.get(feedId, me)
                .map(FeedDTO.Response::fromEntity)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto, "조회 성공", 200)))
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(ApiResponse.failure("피드를 찾을 수 없습니다.", 404)));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{feedId}")
    public ResponseEntity<ApiResponse<FeedDTO.Response>> update(
            @PathVariable Long feedId,
            @Valid @RequestBody FeedDTO.Request dto,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long me = principal.getUserId();
        Feed updated = feedService.update(feedId, dto, me);

        return ResponseEntity.ok(ApiResponse.success(FeedDTO.Response.fromEntity(updated), "수정 성공", 200));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{feedId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long feedId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long me = principal.getUserId();
        feedService.delete(feedId, me);

        return ResponseEntity.status(204)
                .body(ApiResponse.success(null, "삭제 성공", 204));
    }
}