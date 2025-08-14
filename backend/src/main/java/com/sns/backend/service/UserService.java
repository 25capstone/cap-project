package com.sns.backend.service;

import com.sns.backend.dto.LoginDTO;
import com.sns.backend.dto.SignupDTO;
import com.sns.backend.entity.User;
import com.sns.backend.repository.UserRepository;
import com.sns.backend.security.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;  // JWT 토큰 생성기

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    public User signup(SignupDTO request) {
        if (userRepository.findByLoginId(request.getLoginId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 로그인 ID입니다.");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        User user = new User();
        user.setLoginId(request.getLoginId());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        user.setProvider(User.Provider.LOCAL);
        user.setVisibility(User.Visibility.PUBLIC);

        return userRepository.save(user);
    }

    public String login(LoginDTO.Request request) {
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다."));

        if (!user.getProvider().equals(User.Provider.LOCAL)) {
            throw new IllegalArgumentException("OAuth 계정은 로컬 로그인을 사용할 수 없습니다.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // JWT 토큰 생성 및 반환
        return jwtProvider.createToken(user.getLoginId(), user.getUserId());
    }
}
