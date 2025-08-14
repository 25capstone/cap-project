package com.sns.backend.security;

import com.sns.backend.entity.User;
import com.sns.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email;
        String displayName;
        String profileImageUrl;

        if ("kakao".equals(registrationId)) {
            Object kakaoAccountObj = attributes.get("kakao_account");
            if (!(kakaoAccountObj instanceof Map)) {
                throw new OAuth2AuthenticationException("kakao_account 정보가 올바르지 않습니다.");
            }
            Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoAccountObj;

            email = (String) kakaoAccount.get("email");

            Object profileObj = kakaoAccount.get("profile");
            if (!(profileObj instanceof Map)) {
                throw new OAuth2AuthenticationException("kakao profile 정보가 올바르지 않습니다.");
            }
            Map<String, Object> profile = (Map<String, Object>) profileObj;

            displayName = (String) profile.get("nickname");
            profileImageUrl = (String) profile.get("profile_image_url");

        } else if ("google".equals(registrationId)) {
            email = (String) attributes.get("email");
            displayName = (String) attributes.get("name");
            profileImageUrl = (String) attributes.get("picture");
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth 제공자입니다.");
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = new User();
            user.setLoginId(email);
            user.setEmail(email);
            user.setDisplayName(displayName != null ? displayName : "NoName");
            user.setProfileImageUrl(profileImageUrl);
            user.setVisibility(User.Visibility.PUBLIC); // enum 직접 참조
            user.setProvider(User.Provider.valueOf(registrationId.toUpperCase()));
            userRepository.save(user);
        } else {
            boolean needUpdate = false;
            if (!displayName.equals(user.getDisplayName())) {
                user.setDisplayName(displayName);
                needUpdate = true;
            }
            if (profileImageUrl != null && !profileImageUrl.equals(user.getProfileImageUrl())) {
                user.setProfileImageUrl(profileImageUrl);
                needUpdate = true;
            }
            if (!registrationId.toUpperCase().equals(user.getProvider().name())) {
                user.setProvider(User.Provider.valueOf(registrationId.toUpperCase()));
                needUpdate = true;
            }
            if (needUpdate) {
                userRepository.save(user);
            }
        }

        // CustomOAuth2User 생성자 수정 반영
        return new CustomOAuth2User(user, attributes);
    }
}
