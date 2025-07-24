package com.sns.backend.controller;

import com.sns.backend.dto.FeedDTO.FeedRequestDTO;
import com.sns.backend.dto.FeedDTO.FeedResponseDTO;
import com.sns.backend.entity.Feed;
import com.sns.backend.service.FeedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/feeds")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<FeedResponseDTO> createFeed(@RequestBody FeedRequestDTO requestDTO) {
        Feed savedFeed = feedService.createFeed(requestDTO.toEntity());
        return ResponseEntity.ok(FeedResponseDTO.fromEntity(savedFeed));
    }

    // READ - All
    @GetMapping
    public ResponseEntity<List<FeedResponseDTO>> getAllFeeds() {
        List<FeedResponseDTO> feeds = feedService.getAllFeeds()
                .stream()
                .map(FeedResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(feeds);
    }

    // READ - Single
    @GetMapping("/{feedId}")
    public ResponseEntity<FeedResponseDTO> getFeed(@PathVariable Integer feedId) {
        return feedService.getFeedById(feedId)
                .map(FeedResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE
    @PutMapping("/{feedId}")
    public ResponseEntity<FeedResponseDTO> updateFeed(@PathVariable Integer feedId, @RequestBody FeedRequestDTO requestDTO) {
        Feed updatedFeed = requestDTO.toEntity();  // 새 값
        Feed result = feedService.updateFeed(feedId, updatedFeed);  // 기존 ID로 업데이트
        return ResponseEntity.ok(FeedResponseDTO.fromEntity(result));
    }

    // DELETE
    @DeleteMapping("/{feedId}")
    public ResponseEntity<Void> deleteFeed(@PathVariable Integer feedId) {
        feedService.deleteFeed(feedId);
        return ResponseEntity.noContent().build();
    }

    // (Optional) 사용자별 피드 목록
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FeedResponseDTO>> getFeedsByUser(@PathVariable Integer userId) {
        List<FeedResponseDTO> feeds = feedService.getFeedsByUserId(userId)
                .stream()
                .map(FeedResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(feeds);
    }
}
