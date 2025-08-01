package com.sns.backend.repository;

import com.sns.backend.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    // 로그인한 사용자가 팔로우하고 있는 유저 ID 리스트
    @Query("SELECT f.followed.userId FROM Follow f WHERE f.follower.userId = :followerId")
    List<Long> findFollowedUserIdsByFollowerId(@Param("followerId") Long followerId);


    // 특정 사용자끼리의 팔로우 존재 여부
    boolean existsByFollower_UserIdAndFollowed_UserId(Long followerId, Long followedId);
}
