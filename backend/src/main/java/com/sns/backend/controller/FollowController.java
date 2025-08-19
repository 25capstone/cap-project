package com.sns.backend.controller;

import com.sns.backend.common.ApiResponse;
import com.sns.backend.dto.FollowDTO;
import com.sns.backend.dto.FollowRequestDTO;
import com.sns.backend.security.CustomUserDetails;
import com.sns.backend.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class FollowController {

    private final FollowService followService;

    /** 팔로우 요청 보내기 */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/users/{targetId}/follow-requests")
    public ResponseEntity<ApiResponse<FollowRequestDTO>> sendFollowRequest(
            @PathVariable Long targetId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long requesterId = principal.getUserId();
        FollowRequestDTO dto = followService.sendFollowRequest(requesterId, targetId);
        return ResponseEntity.ok(ApiResponse.success(dto, "팔로우 요청 전송 성공", 200));
    }

    /** 팔로우 요청 수락 (요청 대상자만 가능) */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/follow-requests/{followRequestId}/accept")
    public ResponseEntity<ApiResponse<FollowDTO>> acceptFollowRequest(
            @PathVariable Long followRequestId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long me = principal.getUserId();
        FollowDTO dto = followService.acceptFollowRequest(followRequestId, me);
        return ResponseEntity.ok(ApiResponse.success(dto, "팔로우 요청 수락 완료", 200));
    }

    /** 🔹 팔로우 요청 거절 (요청 대상자만 가능) */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/follow-requests/{followRequestId}/reject")
    public ResponseEntity<ApiResponse<FollowRequestDTO>> rejectFollowRequest(
            @PathVariable Long followRequestId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long me = principal.getUserId();
        FollowRequestDTO dto = followService.rejectFollowRequest(followRequestId, me);
        return ResponseEntity.ok(ApiResponse.success(dto, "팔로우 요청 거절 완료", 200));
    }

    /** 🔹 내가 보낸 팔로우 요청 취소 (요청자만 가능) */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/follow-requests/{followRequestId}")
    public ResponseEntity<ApiResponse<Void>> cancelFollowRequest(
            @PathVariable Long followRequestId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long me = principal.getUserId();
        followService.cancelFollowRequest(followRequestId, me);
        return ResponseEntity.ok(ApiResponse.success(null, "팔로우 요청 취소 완료", 200));
    }

    /** 내가 팔로우한 사용자들의 ID 목록 */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/users/me/following")
    public ResponseEntity<ApiResponse<List<Long>>> getMyFollowing(
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long followerId = principal.getUserId();
        List<Long> ids = followService.getFollowedUserIds(followerId);
        return ResponseEntity.ok(ApiResponse.success(ids, "팔로잉 목록 조회 성공", 200));
    }

    /** 내가 받은 팔로우 요청 목록 */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/follow-requests/received")
    public ResponseEntity<ApiResponse<List<FollowRequestDTO>>> getReceivedRequests(
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long targetId = principal.getUserId();
        List<FollowRequestDTO> list = followService.getReceivedFollowRequests(targetId);
        return ResponseEntity.ok(ApiResponse.success(list, "받은 요청 조회 성공", 200));
    }

    /** 내가 보낸 팔로우 요청 목록 */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/follow-requests/sent")
    public ResponseEntity<ApiResponse<List<FollowRequestDTO>>> getSentRequests(
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long requesterId = principal.getUserId();
        List<FollowRequestDTO> list = followService.getSentFollowRequests(requesterId);
        return ResponseEntity.ok(ApiResponse.success(list, "보낸 요청 조회 성공", 200));
    }
}
