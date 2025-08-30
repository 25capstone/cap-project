package com.sns.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "hashtag",
        uniqueConstraints = @UniqueConstraint(name = "tag", columnNames = "tag")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Hashtag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hashtag_id")
    private Long hashtagId;

    @Column(name = "tag", nullable = false, length = 50)
    private String tag; // 저장 시 소문자 normalize 권장
}
