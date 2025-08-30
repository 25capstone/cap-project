package com.sns.backend.repository;

import com.sns.backend.entity.FeedHashtag;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedHashtagRepository extends JpaRepository<FeedHashtag, Long> {

    @EntityGraph(attributePaths = {"hashtag"})
    List<FeedHashtag> findAllByFeed_FeedId(Long feedId);

    void deleteByFeed_FeedId(Long feedId);

    long countByHashtag_Tag(String tag);
}
