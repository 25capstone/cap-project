package com.sns.backend.controller;

import com.sns.backend.common.ApiResponse;
import com.sns.backend.dto.LoginDTO;
import com.sns.backend.dto.SignupDTO;
import com.sns.backend.entity.User;
import com.sns.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> signup(@Valid @RequestBody SignupDTO request) {
        User user = userService.signup(request);

        URI location = URI.create("/api/v1/users/" + user.getUserId());
        Map<String, Object> data = Map.of(
                "userId", user.getUserId(),
                "loginId", user.getLoginId()
        );

        ApiResponse<Map<String, Object>> body =
                ApiResponse.success(data, "회원가입 성공", 201);

        return ResponseEntity
                .created(location)
                .header(HttpHeaders.LOCATION, location.toString())
                .body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@Valid @RequestBody LoginDTO.Request request) {
        String token = userService.login(request);

        Map<String, String> data = Map.of("token", token);

        ApiResponse<Map<String, String>> body =
                ApiResponse.success(data, "로그인 성공", 200);

        return ResponseEntity.ok(body);
    }
}
