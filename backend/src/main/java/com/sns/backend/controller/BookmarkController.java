package com.sns.backend.controller;

import com.sns.backend.common.ApiResponse;
import com.sns.backend.dto.BookmarkDTO;
import com.sns.backend.security.CustomUserDetails;
import com.sns.backend.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /** 북마크 */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/feeds/{feedId}/bookmarks")
    public ResponseEntity<ApiResponse<BookmarkDTO.StatusResponse>> bookmark(
            @PathVariable Long feedId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        var dto = bookmarkService.bookmark(principal.getUserId(), feedId);
        return ResponseEntity.ok(ApiResponse.success(dto, "북마크 성공", 200));
    }

    /** 북마크 취소 */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/feeds/{feedId}/bookmarks")
    public ResponseEntity<ApiResponse<BookmarkDTO.StatusResponse>> unbookmark(
            @PathVariable Long feedId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        var dto = bookmarkService.unbookmark(principal.getUserId(), feedId);
        return ResponseEntity.ok(ApiResponse.success(dto, "북마크 취소 성공", 200));
    }

    /** 내 북마크 상태 */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/feeds/{feedId}/bookmarks/me")
    public ResponseEntity<ApiResponse<BookmarkDTO.StatusResponse>> myStatus(
            @PathVariable Long feedId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        var dto = bookmarkService.getStatus(principal.getUserId(), feedId);
        return ResponseEntity.ok(ApiResponse.success(dto, 200));
    }

    /** 내가 북마크한 피드 목록 */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/users/me/bookmarks")
    public ResponseEntity<ApiResponse<Page<BookmarkDTO.ListItem>>> listMine(
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Page<BookmarkDTO.ListItem> page = bookmarkService.listMine(principal.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "내 북마크 목록 조회 성공", 200));
    }

    /** (선택) 피드의 북마크 수 */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/feeds/{feedId}/bookmarks/count")
    public ResponseEntity<ApiResponse<Long>> count(
            @PathVariable Long feedId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        long count = bookmarkService.countForFeed(principal.getUserId(), feedId);
        return ResponseEntity.ok(ApiResponse.success(count, "북마크 수 조회 성공", 200));
    }
}
