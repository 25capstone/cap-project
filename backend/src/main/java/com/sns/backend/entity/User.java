package com.sns.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", nullable = false, unique = true, length = 100)
    private String loginId;

    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "enum('PUBLIC','FOLLOWERS') default 'PUBLIC'")
    private Visibility visibility = Visibility.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "enum('LOCAL','KAKAO','GOOGLE') default 'LOCAL'")
    private Provider provider = Provider.LOCAL;

    public enum Visibility {
        PUBLIC,
        FOLLOWERS
    }

    public enum Provider {
        LOCAL,
        KAKAO,
        GOOGLE
    }
}
