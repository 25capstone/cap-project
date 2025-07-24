package com.sns.backend.service;

import com.sns.backend.entity.Feed;
import com.sns.backend.repository.FeedRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FeedService {

    private final FeedRepository feedRepository;

    public FeedService(FeedRepository feedRepository) {
        this.feedRepository = feedRepository;
    }

    // CREATE
    public Feed createFeed(Feed feed) {
        feed.setCreatedAt(LocalDateTime.now());
        feed.setUpdatedAt(LocalDateTime.now());
        return feedRepository.save(feed);
    }

    // READ: 전체 피드
    public List<Feed> getAllFeeds() {
        return feedRepository.findAll();
    }

    // READ: 피드 ID로 조회
    public Optional<Feed> getFeedById(Integer id) {
        return feedRepository.findById(id);
    }

    // UPDATE
    public Feed updateFeed(Integer id, Feed updatedFeed) {
        Feed existing = feedRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feed not found: " + id));

        existing.setContent(updatedFeed.getContent());
        existing.setImageUrl(updatedFeed.getImageUrl());
        existing.setUpdatedAt(LocalDateTime.now());

        return feedRepository.save(existing);
    }

    // DELETE
    public void deleteFeed(Integer id) {
        feedRepository.deleteById(id);
    }

    // 사용자 ID로 조회 (선택 기능)
    public List<Feed> getFeedsByUserId(Integer userId) {
        return feedRepository.findByUserId(userId);
    }
}
