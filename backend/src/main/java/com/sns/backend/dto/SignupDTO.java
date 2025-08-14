package com.sns.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupDTO {
    private String loginId;
    private String email;
    private String password;
    private String displayName;
}
