package com.sns.backend.dto;

import lombok.Getter;
import lombok.Setter;

public class LoginDTO {

    @Getter
    @Setter
    public static class Request {
        private String loginId;
        private String password;
    }

    // 응답용 필드
    @Getter
    public static class Response {
        private String token;

        public Response(String token) {
            this.token = token;
        }
    }
}
