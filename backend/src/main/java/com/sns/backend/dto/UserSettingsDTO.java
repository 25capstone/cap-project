package com.sns.backend.dto;

import com.sns.backend.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

public class UserSettingsDTO {

    @Getter
    public static class ChangePasswordRequest {
        @NotBlank private String currentPassword;
        @NotBlank @Size(min = 8, max = 64) private String newPassword;
    }

    @Getter
    public static class UpdateProfileRequest {
        @Size(min = 1, max = 30) private String displayName; // null이면 변경 안 함
        private User.Visibility visibility;                  // PUBLIC | FOLLOWERS
    }

    @Getter
    public static class DeleteAccountRequest { // 로컬은 비번 확인
        private String password;
    }

    @Getter @Builder
    public static class ProfileResponse {
        private final Long userId;
        private final String loginId;
        private final String displayName;
        private final User.Visibility visibility;

        public static ProfileResponse from(User u) {
            return ProfileResponse.builder()
                    .userId(u.getUserId())
                    .loginId(u.getLoginId())
                    .displayName(u.getDisplayName())
                    .visibility(u.getVisibility())
                    .build();
        }
    }
}
