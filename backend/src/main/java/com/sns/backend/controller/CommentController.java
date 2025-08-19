package com.sns.backend.controller;

import com.sns.backend.common.ApiResponse;
import com.sns.backend.dto.CommentDTO;
import com.sns.backend.entity.Comment;
import com.sns.backend.security.CustomUserDetails;
import com.sns.backend.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CommentController {

    private final CommentService commentService;

    /** 댓글 작성 */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/feeds/{feedId}/comments")
    public ResponseEntity<ApiResponse<CommentDTO.Response>> create(
            @PathVariable Long feedId,
            @Valid @RequestBody CommentDTO.CreateRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long me = principal.getUserId();
        Comment saved = commentService.create(feedId, me, req);
        return ResponseEntity.ok(
                ApiResponse.success(CommentDTO.Response.fromEntity(saved, me), "댓글 작성 성공", 200)
        );
    }

    /** 댓글 목록 */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/feeds/{feedId}/comments")
    public ResponseEntity<ApiResponse<Page<CommentDTO.Response>>> list(
            @PathVariable Long feedId,
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long me = principal.getUserId();
        Page<CommentDTO.Response> page = commentService.list(feedId, me, pageable)
                .map(c -> CommentDTO.Response.fromEntity(c, me));

        return ResponseEntity.ok(ApiResponse.success(page, "댓글 목록 조회 성공", 200));
    }

    /** 댓글 수정 (작성자) */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentDTO.Response>> update(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentDTO.UpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long me = principal.getUserId();
        Comment updated = commentService.update(commentId, me, req);
        return ResponseEntity.ok(
                ApiResponse.success(CommentDTO.Response.fromEntity(updated, me), "댓글 수정 성공", 200)
        );
    }

    /** 댓글 삭제 (작성자 or 피드 작성자) */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long me = principal.getUserId();
        commentService.delete(commentId, me);
        return ResponseEntity.ok(ApiResponse.success(null, "댓글 삭제 성공", 200));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/feeds/{feedId}/comments/count")
    public ResponseEntity<ApiResponse<Long>> count(
            @PathVariable Long feedId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long me = principal.getUserId();
        long count = commentService.countForFeed(feedId, me);
        return ResponseEntity.ok(ApiResponse.success(count, "댓글 수 조회 성공", 200));
    }

}
