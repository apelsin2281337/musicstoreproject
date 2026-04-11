package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query(value = "SELECT * FROM reviews WHERE track_id = :trackId", nativeQuery = true)
    List<Review> findByReviewTrack_TrackId(@Param("trackId") Long trackId);

    @Query(value = "SELECT * FROM reviews WHERE album_id = :albumId", nativeQuery = true)
    List<Review> findByReviewAlbum_AlbumId(@Param("albumId") Long albumId);

    @Query(value = "SELECT * FROM reviews WHERE review_id = :reviewId", nativeQuery = true)
    Optional<Review> findByReviewId(@Param("reviewId") Long reviewId);
}
