package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.User;
import org.apelsin.musicstore.model.Role;
import org.apelsin.musicstore.model.Track;
import org.apelsin.musicstore.model.Album;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setUserUsername(rs.getString("user_username"));
        user.setUserPassword(rs.getString("user_password"));
        user.setUserRole(Role.valueOf(rs.getString("user_role")));
        
        user.setUserPurchasedTracks(findPurchasedTracksByUserId(user.getUserId()));
        user.setUserPurchasedAlbums(findPurchasedAlbumsByUserId(user.getUserId()));
        
        return user;
    }

    private List<Track> findPurchasedTracksByUserId(Long userId) {
        String sql = "SELECT t.* FROM tracks t JOIN user_purchased_tracks upt ON t.track_id = upt.track_id WHERE upt.user_id = ?";
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Track track = new Track();
                    track.setTrackId(rs.getLong("track_id"));
                    track.setTrackTitle(rs.getString("track_title"));
                    track.setTrackPrice(rs.getDouble("track_price"));
                    track.setTrackFilePath(rs.getString("track_file_path"));
                    track.setTrackDownloadCount(rs.getLong("track_download_count"));
                    tracks.add(track);
                }
            }
            return tracks;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find purchased tracks", e);
        }
    }

    private List<Album> findPurchasedAlbumsByUserId(Long userId) {
        String sql = "SELECT a.* FROM albums a JOIN user_purchased_albums upa ON a.album_id = upa.album_id WHERE upa.user_id = ?";
        List<Album> albums = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Album album = new Album();
                    album.setAlbumId(rs.getLong("album_id"));
                    album.setAlbumTitle(rs.getString("album_title"));
                    album.setAlbumReleaseYear(rs.getInt("album_release_year"));
                    album.setAlbumPrice(rs.getDouble("album_price"));
                    album.setAlbumRating(rs.getLong("album_rating"));
                    albums.add(album);
                }
            }
            return albums;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find purchased albums", e);
        }
    }

    public User save(User user) {
        String sql = "INSERT INTO users (user_username, user_password, user_role) VALUES (?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUserUsername());
            ps.setString(2, user.getUserPassword());
            ps.setString(3, user.getUserRole().name());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setUserId(rs.getLong(1));
                }
            }
            
            if (user.getUserPurchasedTracks() != null && !user.getUserPurchasedTracks().isEmpty()) {
                savePurchasedTracks(conn, user.getUserId(), user.getUserPurchasedTracks());
            }
            
            if (user.getUserPurchasedAlbums() != null && !user.getUserPurchasedAlbums().isEmpty()) {
                savePurchasedAlbums(conn, user.getUserId(), user.getUserPurchasedAlbums());
            }
            
            return user;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    private void savePurchasedTracks(Connection conn, Long userId, List<Track> tracks) throws SQLException {
        String sql = "INSERT INTO user_purchased_tracks (user_id, track_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Track track : tracks) {
                ps.setLong(1, userId);
                ps.setLong(2, track.getTrackId());
                ps.executeUpdate();
            }
        }
    }

    private void savePurchasedAlbums(Connection conn, Long userId, List<Album> albums) throws SQLException {
        String sql = "INSERT INTO user_purchased_albums (user_id, album_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Album album : albums) {
                ps.setLong(1, userId);
                ps.setLong(2, album.getAlbumId());
                ps.executeUpdate();
            }
        }
    }

    public void addPurchasedTracks(Long userId, List<Long> trackIds) {
        String sql = "INSERT INTO user_purchased_tracks (user_id, track_id) VALUES (?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Long trackId : trackIds) {
                ps.setLong(1, userId);
                ps.setLong(2, trackId);
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to add purchased tracks", e);
        }
    }

    public void addPurchasedAlbums(Long userId, List<Long> albumIds) {
        String sql = "INSERT INTO user_purchased_albums (user_id, album_id) VALUES (?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Long albumId : albumIds) {
                ps.setLong(1, userId);
                ps.setLong(2, albumId);
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to add purchased albums", e);
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by id", e);
        }
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all users", e);
        }
    }

    public Optional<User> findByUserUsername(String username) {
        String sql = "SELECT * FROM users WHERE user_username = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by username", e);
        }
    }

    public boolean existsByUserUsername(String username) {
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE user_username = ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
            return false;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check if user exists", e);
        }
    }

    public List<User> findByUserRole(Role role) {
        String sql = "SELECT * FROM users WHERE user_role = ?";
        List<User> users = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
            return users;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find users by role", e);
        }
    }

    public List<User> findAllUsers() {
        return findAll();
    }

    public Optional<User> findByUserId(Long userId) {
        return findById(userId);
    }

    public User update(User user) {
        String sql = "UPDATE users SET user_username = ?, user_password = ?, user_role = ? WHERE user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUserUsername());
            ps.setString(2, user.getUserPassword());
            ps.setString(3, user.getUserRole().name());
            ps.setLong(4, user.getUserId());

            ps.executeUpdate();
            return user;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }
}
