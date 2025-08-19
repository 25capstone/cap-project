package com.sns.backend.repository;

import com.sns.backend.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    /** 내가 팔로우하는 사용자 ID 목록 */
    @Query("select f.followed.userId from Follow f where f.follower.userId = :followerId")
    List<Long> findFollowedUserIdsByFollowerId(@Param("followerId") Long followerId);

    /** 특정 사용자끼리 팔로우 존재 여부 */
    boolean existsByFollower_UserIdAndFollowed_UserId(Long followerId, Long followedId);

    /** (멱등 처리/재응답용) 특정 팔로우 링크 최근 1건 */
    Optional<Follow> findTopByFollower_UserIdAndFollowed_UserIdOrderByCreatedAtDesc(Long followerId, Long followedId);

    /** 언팔로우용(필요 시) */
    void deleteByFollower_UserIdAndFollowed_UserId(Long followerId, Long followedId);

    /** 카운트(선택) */
    long countByFollower_UserId(Long followerId);  // 내가 팔로우하는 수
    long countByFollowed_UserId(Long followedId);  // 나를 팔로우하는 수
}
