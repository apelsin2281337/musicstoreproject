package org.apelsin.musicstore.controller;

import lombok.RequiredArgsConstructor;
import org.apelsin.musicstore.model.Review;
import org.apelsin.musicstore.repository.ReviewRepository;
import org.apelsin.musicstore.repository.TrackRepository;
import org.apelsin.musicstore.repository.AlbumRepository;
import org.apelsin.musicstore.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewRepository reviewRepository;
    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;

    @GetMapping("/track/{trackId}")
    public List<Review> getTrackReviews(@PathVariable Long trackId) {
        return reviewRepository.findByReviewTrack_TrackId(trackId);
    }

    @GetMapping("/album/{albumId}")
    public List<Review> getAlbumReviews(@PathVariable Long albumId) {
        return reviewRepository.findByReviewAlbum_AlbumId(albumId);
    }

    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody ReviewRequest request) {
        Review review = new Review();
        review.setReviewUser(userRepository.findById(request.userId).orElseThrow());
        review.setReviewRating(request.rating);
        review.setReviewComment(request.comment);
        review.setReviewDate(LocalDateTime.now());
        
        if (request.trackId != null) {
            review.setReviewTrack(trackRepository.findById(request.trackId).orElseThrow());
        }
        if (request.albumId != null) {
            review.setReviewAlbum(albumRepository.findById(request.albumId).orElseThrow());
        }
        
        reviewRepository.save(review);
        return ResponseEntity.ok("Отзыв добавлен");
    }

    public static class ReviewRequest {
        public Long userId;
        public Long trackId;
        public Long albumId;
        public Integer rating;
        public String comment;
    }
}
