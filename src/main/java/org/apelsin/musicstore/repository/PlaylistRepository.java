package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.Playlist;
import org.apelsin.musicstore.model.User;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PlaylistRepository {

    private final DataSource dataSource;
    private final UserRepository userRepository;

    public PlaylistRepository(DataSource dataSource, UserRepository userRepository) {
        this.dataSource = dataSource;
        this.userRepository = userRepository;
    }

    private Playlist mapResultSetToPlaylist(ResultSet rs) throws SQLException {
        Playlist playlist = new Playlist();
        playlist.setPlaylistId(rs.getLong("playlist_id"));
        playlist.setPlaylistTitle(rs.getString("playlist_title"));

        Long userId = rs.getLong("user_id");
        userRepository.findById(userId).ifPresent(playlist::setPlaylistOwner);

        return playlist;
    }

    public Playlist save(Playlist playlist) {
        String sql = "INSERT INTO playlists (playlist_title, user_id) VALUES (?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, playlist.getPlaylistTitle());
            ps.setLong(2, playlist.getPlaylistOwner().getUserId());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    playlist.setPlaylistId(rs.getLong(1));
                }
            }
            return playlist;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save playlist", e);
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM playlists WHERE playlist_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete playlist", e);
        }
    }

    public Optional<Playlist> findById(Long id) {
        String sql = "SELECT * FROM playlists WHERE playlist_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPlaylist(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find playlist by id", e);
        }
    }

    public List<Playlist> findAll() {
        String sql = "SELECT * FROM playlists";
        List<Playlist> playlists = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                playlists.add(mapResultSetToPlaylist(rs));
            }
            return playlists;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all playlists", e);
        }
    }

    public List<Playlist> findByPlaylistOwner_UserId(Long userId) {
        String sql = "SELECT * FROM playlists WHERE user_id = ?";
        List<Playlist> playlists = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    playlists.add(mapResultSetToPlaylist(rs));
                }
            }
            return playlists;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find playlists by user id", e);
        }
    }

    public Optional<Playlist> findByPlaylistId(Long playlistId) {
        return findById(playlistId);
    }

    public Playlist update(Playlist playlist) {
        String sql = "UPDATE playlists SET playlist_title = ?, user_id = ? WHERE playlist_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, playlist.getPlaylistTitle());
            ps.setLong(2, playlist.getPlaylistOwner().getUserId());
            ps.setLong(3, playlist.getPlaylistId());

            ps.executeUpdate();
            return playlist;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update playlist", e);
        }
    }
}
