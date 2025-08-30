package com.sns.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "feed_hashtag",
        uniqueConstraints = @UniqueConstraint(name = "uniq_feed_tag", columnNames = {"feed_id", "hashtag_id"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeedHashtag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feed_hashtag_id")
    private Long feedHashtagId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feed_id", nullable = false, foreignKey = @ForeignKey(name = "feed_hashtag_ibfk_2"))
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hashtag_id", nullable = false, foreignKey = @ForeignKey(name = "feed_hashtag_ibfk_1"))
    private Hashtag hashtag;
}
