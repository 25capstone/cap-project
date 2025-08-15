package com.sns.backend.controller;

import com.sns.backend.dto.FollowDTO;
import com.sns.backend.dto.FollowRequestDTO;
import com.sns.backend.security.CustomUserDetails;
import com.sns.backend.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follow")
public class FollowController {

    private final FollowService followService;

    // 팔로우 요청 보내기
    @PostMapping("/request/{targetId}")
    public ResponseEntity<FollowRequestDTO> sendFollowRequest(
            @PathVariable Long targetId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long requesterId = principal.getUserId(); // 로그인한 사용자
        FollowRequestDTO dto = followService.sendFollowRequest(requesterId, targetId);
        return ResponseEntity.ok(dto);
    }

    // 팔로우 요청 수락
    @PostMapping("/accept/{followRequestId}")
    public ResponseEntity<FollowDTO> acceptFollowRequest(
            @PathVariable Long followRequestId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        // 필요하다면 principal.getUserId()로 권한 체크 가능
        FollowDTO dto = followService.acceptFollowRequest(followRequestId);
        return ResponseEntity.ok(dto);
    }

    // 내가 받은 팔로우 요청 목록
    @GetMapping("/requests/received")
    public ResponseEntity<List<FollowRequestDTO>> getReceivedRequests(
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long targetId = principal.getUserId();
        return ResponseEntity.ok(followService.getReceivedFollowRequests(targetId));
    }

    // 내가 보낸 팔로우 요청 목록
    @GetMapping("/requests/sent")
    public ResponseEntity<List<FollowRequestDTO>> getSentRequests(
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long requesterId = principal.getUserId();
        return ResponseEntity.ok(followService.getSentFollowRequests(requesterId));
    }
}
