package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.Review;
import org.apelsin.musicstore.model.User;
import org.apelsin.musicstore.model.Track;
import org.apelsin.musicstore.model.Album;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ReviewRepository {

    private final DataSource dataSource;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;

    public ReviewRepository(DataSource dataSource, UserRepository userRepository, 
                           TrackRepository trackRepository, AlbumRepository albumRepository) {
        this.dataSource = dataSource;
        this.userRepository = userRepository;
        this.trackRepository = trackRepository;
        this.albumRepository = albumRepository;
    }

    private Review mapResultSetToReview(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getLong("review_id"));
        review.setReviewRating(rs.getInt("review_rating"));
        review.setReviewComment(rs.getString("review_comment"));
        review.setReviewDate(rs.getTimestamp("review_date").toLocalDateTime());

        Long userId = rs.getLong("user_id");
        userRepository.findById(userId).ifPresent(review::setReviewUser);

        Long trackId = rs.getLong("track_id");
        if (!rs.wasNull()) {
            trackRepository.findById(trackId).ifPresent(review::setReviewTrack);
        }

        Long albumId = rs.getLong("album_id");
        if (!rs.wasNull()) {
            albumRepository.findById(albumId).ifPresent(review::setReviewAlbum);
        }

        return review;
    }

    public Review save(Review review) {
        String sql = "INSERT INTO reviews (user_id, track_id, album_id, review_rating, review_comment, review_date) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, review.getReviewUser().getUserId());
            
            if (review.getReviewTrack() != null) {
                ps.setLong(2, review.getReviewTrack().getTrackId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            
            if (review.getReviewAlbum() != null) {
                ps.setLong(3, review.getReviewAlbum().getAlbumId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }
            
            ps.setObject(4, review.getReviewRating(), Types.INTEGER);
            ps.setString(5, review.getReviewComment());
            ps.setTimestamp(6, Timestamp.valueOf(review.getReviewDate()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    review.setReviewId(rs.getLong(1));
                }
            }
            return review;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save review", e);
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete review", e);
        }
    }

    public Optional<Review> findById(Long id) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReview(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find review by id", e);
        }
    }

    public List<Review> findAll() {
        String sql = "SELECT * FROM reviews";
        List<Review> reviews = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                reviews.add(mapResultSetToReview(rs));
            }
            return reviews;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all reviews", e);
        }
    }

    public List<Review> findByReviewTrack_TrackId(Long trackId) {
        String sql = "SELECT * FROM reviews WHERE track_id = ?";
        List<Review> reviews = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, trackId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapResultSetToReview(rs));
                }
            }
            return reviews;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find reviews by track id", e);
        }
    }

    public List<Review> findByReviewAlbum_AlbumId(Long albumId) {
        String sql = "SELECT * FROM reviews WHERE album_id = ?";
        List<Review> reviews = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, albumId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapResultSetToReview(rs));
                }
            }
            return reviews;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find reviews by album id", e);
        }
    }

    public Optional<Review> findByReviewId(Long reviewId) {
        return findById(reviewId);
    }

    public Review update(Review review) {
        String sql = "UPDATE reviews SET user_id = ?, track_id = ?, album_id = ?, review_rating = ?, review_comment = ?, review_date = ? WHERE review_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, review.getReviewUser().getUserId());

            if (review.getReviewTrack() != null) {
                ps.setLong(2, review.getReviewTrack().getTrackId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }

            if (review.getReviewAlbum() != null) {
                ps.setLong(3, review.getReviewAlbum().getAlbumId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }

            ps.setObject(4, review.getReviewRating(), Types.INTEGER);
            ps.setString(5, review.getReviewComment());
            ps.setTimestamp(6, Timestamp.valueOf(review.getReviewDate()));
            ps.setLong(7, review.getReviewId());

            ps.executeUpdate();
            return review;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update review", e);
        }
    }
}
