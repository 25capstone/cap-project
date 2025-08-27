// com.sns.backend.controller.AuthController
package com.sns.backend.controller;

import com.sns.backend.common.ApiResponse;
import com.sns.backend.dto.LoginDTO;
import com.sns.backend.dto.SignupDTO;
import com.sns.backend.entity.User;
import com.sns.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Map<String,Object>>> signup(@RequestBody SignupDTO request) {
        User user = authService.signup(request);
        return ResponseEntity.ok(
                ApiResponse.success(Map.of(
                        "message","회원가입 성공",
                        "loginId", user.getLoginId(),
                        "userId", user.getUserId()
                ), 200)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String,String>>> login(@RequestBody LoginDTO.Request request) {
        String token = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success(Map.of("token", token), 200)
        );
    }
}
