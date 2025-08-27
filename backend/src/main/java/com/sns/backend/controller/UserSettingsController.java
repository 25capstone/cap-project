package com.sns.backend.controller;

import com.sns.backend.common.ApiResponse;
import com.sns.backend.dto.UserSettingsDTO;
import com.sns.backend.security.CustomUserDetails;
import com.sns.backend.service.UserSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me")
public class UserSettingsController {

    private final UserSettingsService settingsService;

    /** 닉네임/공개범위 변경 */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping
    public ResponseEntity<ApiResponse<UserSettingsDTO.ProfileResponse>> updateProfile(
            @Valid @RequestBody UserSettingsDTO.UpdateProfileRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {

        var resp = settingsService.updateProfile(principal.getUserId(), req);
        return ResponseEntity.ok(ApiResponse.success(resp, "프로필 수정 성공", 200));
    }

    /** 비밀번호 변경 (LOCAL 전용) */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody UserSettingsDTO.ChangePasswordRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {

        settingsService.changePassword(principal.getUserId(), req);
        return ResponseEntity.ok(ApiResponse.success(null, "비밀번호 변경 성공", 200));
    }

    /** 로그아웃 (JWT는 클라 토큰 폐기, 필요 시 서버 블랙리스트 연동) */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        String token = (authorization != null && authorization.startsWith("Bearer "))
                ? authorization.substring(7) : null;
        if (token != null) settingsService.logout(token);
        return ResponseEntity.ok(ApiResponse.success(null, "로그아웃 완료", 200));
    }

    /** 회원탈퇴 (LOCAL은 비밀번호 확인) */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @RequestBody(required = false) UserSettingsDTO.DeleteAccountRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {

        settingsService.deleteAccount(principal.getUserId(), req);
        return ResponseEntity.ok(ApiResponse.success(null, "회원탈퇴 완료", 200));
    }
}
