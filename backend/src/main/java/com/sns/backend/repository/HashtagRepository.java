package com.sns.backend.repository;

import com.sns.backend.entity.Hashtag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Optional<Hashtag> findByTag(String tag);
    boolean existsByTag(String tag);

    interface TrendingTag {
        String getTag();
        long getUses();
    }

    @Query("""
        select h.tag as tag, count(fh) as uses
        from FeedHashtag fh join fh.hashtag h
        group by h.tag
        order by uses desc
    """)
    Page<TrendingTag> findTrending(Pageable pageable);

    @Query("""
        select h from Hashtag h
        where lower(h.tag) like lower(concat(:prefix, '%'))
        order by h.tag asc
    """)
    Page<Hashtag> autocomplete(@Param("prefix") String prefix, Pageable pageable);
}
