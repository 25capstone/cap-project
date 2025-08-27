package com.sns.backend.service;

import com.sns.backend.dto.UserSettingsDTO;
import com.sns.backend.entity.User;
import com.sns.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePassword(Long me, UserSettingsDTO.ChangePasswordRequest req) {
        User user = userRepository.findById(me)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getProvider() != User.Provider.LOCAL) {
            throw new IllegalArgumentException("OAuth 계정은 로컬 비밀번호를 변경할 수 없습니다.");
        }
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
    }

    @Transactional
    public UserSettingsDTO.ProfileResponse updateProfile(Long me, UserSettingsDTO.UpdateProfileRequest req) {
        User user = userRepository.findById(me)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (req.getDisplayName() != null && !req.getDisplayName().isBlank()) {
            user.setDisplayName(req.getDisplayName().trim());
        }
        if (req.getVisibility() != null) {
            // ENUM: PUBLIC | FOLLOWERS 만 허용됨
            user.setVisibility(req.getVisibility());
        }
        return UserSettingsDTO.ProfileResponse.from(user);
    }

    /** JWT 로그아웃: 서버 상태 없음(클라에서 토큰 삭제). 필요 시 블랙리스트 연동 */
    public void logout(String accessToken) {
        // ex) blacklistService.add(accessToken, ttl);
    }

    @Transactional
    public void deleteAccount(Long me, UserSettingsDTO.DeleteAccountRequest req) {
        User user = userRepository.findById(me)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getProvider() == User.Provider.LOCAL) {
            if (req == null || req.getPassword() == null ||
                    !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
                throw new IllegalArgumentException("비밀번호 확인이 필요합니다.");
            }
        }
        userRepository.delete(user);
    }
}
