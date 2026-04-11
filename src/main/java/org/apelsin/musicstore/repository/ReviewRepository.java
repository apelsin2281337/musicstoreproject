package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Все отзывы на конкретный трек
    List<Review> findByReviewTrack_TrackId(Long trackId);

    // Все отзывы на альбом
    List<Review> findByReviewAlbum_AlbumId(Long albumId);
}