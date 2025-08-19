package com.sns.backend.controller;

import com.sns.backend.common.ApiResponse;
import com.sns.backend.dto.FeedLikeDTO;
import com.sns.backend.security.CustomUserDetails;
import com.sns.backend.service.FeedLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/feeds")
@RequiredArgsConstructor
public class FeedLikeController {

    private final FeedLikeService feedLikeService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{feedId}/likes")
    public ResponseEntity<ApiResponse<FeedLikeDTO>> like(
            @PathVariable Long feedId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long userId = principal.getUserId();
        FeedLikeDTO dto = feedLikeService.like(userId, feedId);
        return ResponseEntity.ok(ApiResponse.success(dto, "좋아요 성공", 200));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{feedId}/likes")
    public ResponseEntity<ApiResponse<FeedLikeDTO>> unlike(
            @PathVariable Long feedId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long userId = principal.getUserId();
        FeedLikeDTO dto = feedLikeService.unlike(userId, feedId);
        return ResponseEntity.ok(ApiResponse.success(dto, "좋아요 취소 성공", 200));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{feedId}/likes/me")
    public ResponseEntity<ApiResponse<FeedLikeDTO>> myLikeStatus(
            @PathVariable Long feedId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long userId = principal.getUserId();
        FeedLikeDTO dto = feedLikeService.getStatus(userId, feedId);
        return ResponseEntity.ok(ApiResponse.success(dto, 200));
    }
}
