package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.Artist;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ArtistRepository {

    private final DataSource dataSource;

    public ArtistRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Artist mapResultSetToArtist(ResultSet rs) throws SQLException {
        Artist artist = new Artist();
        artist.setArtistId(rs.getLong("artist_id"));
        artist.setArtistName(rs.getString("artist_name"));
        artist.setArtistDescription(rs.getString("artist_description"));
        artist.setArtistRating(rs.getLong("artist_rating"));
        return artist;
    }

    public Artist save(Artist artist) {
        String sql = "INSERT INTO artists (artist_name, artist_rating, artist_description) VALUES (?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, artist.getArtistName());
            ps.setObject(2, artist.getArtistRating(), Types.BIGINT);
            ps.setString(3, artist.getArtistDescription());
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    artist.setArtistId(rs.getLong(1));
                }
            }
            return artist;
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save artist", e);
        }
    }

    public Artist update(Artist artist) {
        String sql = "UPDATE artists SET artist_name = ?, artist_rating = ?, artist_description = ? WHERE artist_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, artist.getArtistName());
            ps.setObject(2, artist.getArtistRating(), Types.BIGINT);
            ps.setString(3, artist.getArtistDescription());
            ps.setLong(4, artist.getArtistId());
            
            ps.executeUpdate();
            return artist;
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update artist", e);
        }
    }

    public void deleteByArtistId(Long artistId) {
        String sql = "DELETE FROM artists WHERE artist_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, artistId);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete artist", e);
        }
    }

    public void incrementRating(Long artistId, Long increment) {
        String sql = "UPDATE artists SET artist_rating = artist_rating + ? WHERE artist_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, increment, Types.BIGINT);
            ps.setLong(2, artistId);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to increment rating", e);
        }
    }

    public void decrementRating(Long artistId, Long decrement) {
        String sql = "UPDATE artists SET artist_rating = artist_rating - ? WHERE artist_id = ? AND artist_rating >= ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, decrement, Types.BIGINT);
            ps.setLong(2, artistId);
            ps.setObject(3, decrement, Types.BIGINT);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to decrement rating", e);
        }
    }

    public List<Artist> findAll() {
        String sql = "SELECT * FROM artists";
        List<Artist> artists = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                artists.add(mapResultSetToArtist(rs));
            }
            return artists;
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all artists", e);
        }
    }

    public List<Artist> findAllByOrderByArtistRatingDesc() {
        String sql = "SELECT * FROM artists ORDER BY artist_rating DESC";
        List<Artist> artists = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                artists.add(mapResultSetToArtist(rs));
            }
            return artists;
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find artists by rating", e);
        }
    }

    public Optional<Artist> findByArtistName(String name) {
        String sql = "SELECT * FROM artists WHERE artist_name = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, name);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToArtist(rs));
                }
            }
            return Optional.empty();
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find artist by name", e);
        }
    }

    public List<Artist> findByArtistNameContainingIgnoreCase(String name) {
        String sql = "SELECT * FROM artists WHERE LOWER(artist_name) LIKE LOWER(CONCAT('%', ?, '%'))";
        List<Artist> artists = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, name);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    artists.add(mapResultSetToArtist(rs));
                }
            }
            return artists;
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find artists by name containing", e);
        }
    }

    public Optional<Artist> findByArtistId(Long artistId) {
        String sql = "SELECT * FROM artists WHERE artist_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, artistId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToArtist(rs));
                }
            }
            return Optional.empty();
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find artist by id", e);
        }
    }

    public Optional<Artist> findById(Long id) {
        return findByArtistId(id);
    }
}
