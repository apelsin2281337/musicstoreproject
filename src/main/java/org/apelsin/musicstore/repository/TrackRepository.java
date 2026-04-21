package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.Track;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TrackRepository {

    private final DataSource dataSource;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;

    public TrackRepository(DataSource dataSource, ArtistRepository artistRepository, 
                          AlbumRepository albumRepository, UserRepository userRepository,
                          GenreRepository genreRepository) {
        this.dataSource = dataSource;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.userRepository = userRepository;
    }

    private Track mapResultSetToTrack(ResultSet rs) throws SQLException {
        Track track = new Track();
        track.setTrackId(rs.getLong("track_id"));
        track.setTrackTitle(rs.getString("track_title"));
        track.setTrackPrice(rs.getDouble("track_price"));
        track.setTrackFilePath(rs.getString("track_file_path"));
        track.setTrackDownloadCount(rs.getLong("track_download_count"));

        Long artistId = rs.getLong("artist_id");
        artistRepository.findById(artistId).ifPresent(track::setTrackArtist);

        Long albumId = rs.getLong("album_id");
        if (!rs.wasNull()) {
            albumRepository.findById(albumId).ifPresent(track::setTrackAlbum);
        }

        Long adminId = rs.getLong("admin_id");
        if (!rs.wasNull()) {
            userRepository.findById(adminId).ifPresent(track::setTrackUploadedBy);
        }

        return track;
    }

    public Track save(Track track) {
        String sql = "INSERT INTO tracks (track_title, track_price, track_file_path, track_download_count, artist_id, album_id, admin_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, track.getTrackTitle());
            ps.setObject(2, track.getTrackPrice(), Types.DOUBLE);
            ps.setString(3, track.getTrackFilePath());
            ps.setObject(4, track.getTrackDownloadCount(), Types.BIGINT);
            ps.setLong(5, track.getTrackArtist().getArtistId());
            
            if (track.getTrackAlbum() != null) {
                ps.setLong(6, track.getTrackAlbum().getAlbumId());
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            
            if (track.getTrackUploadedBy() != null) {
                ps.setLong(7, track.getTrackUploadedBy().getUserId());
            } else {
                ps.setNull(7, Types.BIGINT);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    track.setTrackId(rs.getLong(1));
                }
            }
            return track;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save track", e);
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM tracks WHERE track_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete track", e);
        }
    }

    public void incrementDownloadCount(Long trackId) {
        String sql = "UPDATE tracks SET track_download_count = track_download_count + 1 WHERE track_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, trackId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to increment download count", e);
        }
    }

    public Optional<Track> findById(Long id) {
        String sql = "SELECT * FROM tracks WHERE track_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTrack(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find track by id", e);
        }
    }

    public List<Track> findAll() {
        String sql = "SELECT * FROM tracks";
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tracks.add(mapResultSetToTrack(rs));
            }
            return tracks;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all tracks", e);
        }
    }

    public List<Track> findByTrackArtist_ArtistId(Long artistId) {
        String sql = "SELECT * FROM tracks WHERE artist_id = ?";
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, artistId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tracks.add(mapResultSetToTrack(rs));
                }
            }
            return tracks;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tracks by artist id", e);
        }
    }

    public List<Track> findByTrackAlbum_AlbumId(Long albumId) {
        String sql = "SELECT * FROM tracks WHERE album_id = ?";
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, albumId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tracks.add(mapResultSetToTrack(rs));
                }
            }
            return tracks;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tracks by album id", e);
        }
    }

    public List<Track> findByTrackUploadedBy_UserId(Long userId) {
        String sql = "SELECT * FROM tracks WHERE admin_id = ?";
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tracks.add(mapResultSetToTrack(rs));
                }
            }
            return tracks;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tracks by user id", e);
        }
    }

    public List<Track> findAllByOrderByTrackDownloadCountDesc() {
        String sql = "SELECT * FROM tracks ORDER BY track_download_count DESC";
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tracks.add(mapResultSetToTrack(rs));
            }
            return tracks;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tracks by download count", e);
        }
    }

    public List<Track> findByTrackGenres_GenreId(Long genreId) {
        String sql = "SELECT t.* FROM tracks t JOIN trackGenres tg ON t.track_id = tg.track_id WHERE tg.genre_id = ?";
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, genreId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tracks.add(mapResultSetToTrack(rs));
                }
            }
            return tracks;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tracks by genre id", e);
        }
    }

    public List<Track> findAllByTrackTitle(String trackTitle) {
        String sql = "SELECT * FROM tracks WHERE track_title = ?";
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, trackTitle);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tracks.add(mapResultSetToTrack(rs));
                }
            }
            return tracks;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tracks by title", e);
        }
    }

    public List<Track> findAllByTrackTitleContainingIgnoreCase(String title) {
        String sql = "SELECT * FROM tracks WHERE LOWER(track_title) LIKE LOWER(CONCAT('%', ?, '%'))";
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tracks.add(mapResultSetToTrack(rs));
                }
            }
            return tracks;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tracks by title containing", e);
        }
    }

    public Optional<Track> findByTrackId(Long trackId) {
        return findById(trackId);
    }

    public Track update(Track track) {
        String sql = "UPDATE tracks SET track_title = ?, track_price = ?, track_file_path = ?, track_download_count = ?, artist_id = ?, album_id = ?, admin_id = ? WHERE track_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, track.getTrackTitle());
            ps.setObject(2, track.getTrackPrice(), Types.DOUBLE);
            ps.setString(3, track.getTrackFilePath());
            ps.setObject(4, track.getTrackDownloadCount(), Types.BIGINT);
            ps.setLong(5, track.getTrackArtist().getArtistId());

            if (track.getTrackAlbum() != null) {
                ps.setLong(6, track.getTrackAlbum().getAlbumId());
            } else {
                ps.setNull(6, Types.BIGINT);
            }

            if (track.getTrackUploadedBy() != null) {
                ps.setLong(7, track.getTrackUploadedBy().getUserId());
            } else {
                ps.setNull(7, Types.BIGINT);
            }

            ps.setLong(8, track.getTrackId());
            ps.executeUpdate();
            return track;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update track", e);
        }
    }

    public List<Track> findRecommendedTracks() {
        String sql = "SELECT * FROM tracks ORDER BY track_download_count DESC, track_id DESC LIMIT 20";
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tracks.add(mapResultSetToTrack(rs));
            }
            return tracks;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find recommended tracks", e);
        }
    }

    public List<?> findFeaturedPlaylists() {
        String sql = """
            SELECT p.*, COUNT(pt.track_id) as track_count
            FROM playlists p
            LEFT JOIN playlist_tracks pt ON p.playlist_id = pt.playlist_id
            GROUP BY p.playlist_id, p.playlist_title, p.user_id
            ORDER BY track_count DESC, p.playlist_id DESC
            LIMIT 10
            """;
        List<Object[]> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                results.add(new Object[]{
                    rs.getLong("playlist_id"),
                    rs.getString("playlist_title"),
                    rs.getLong("user_id"),
                    rs.getInt("track_count")
                });
            }
            return results;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find featured playlists", e);
        }
    }

    public List<Track> findAllById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        
        StringBuilder sql = new StringBuilder("SELECT * FROM tracks WHERE track_id IN (");
        for (int i = 0; i < ids.size(); i++) {
            sql.append("?");
            if (i < ids.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");
        
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < ids.size(); i++) {
                ps.setLong(i + 1, ids.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tracks.add(mapResultSetToTrack(rs));
                }
            }
            return tracks;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tracks by ids", e);
        }
    }

    public List<Track> findByPlaylistId(Long playlistId) {
        String sql = "SELECT t.* FROM tracks t " +
                   "JOIN playlist_tracks pt ON t.track_id = pt.track_id " +
                   "WHERE pt.playlist_id = ?";
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, playlistId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tracks.add(mapResultSetToTrack(rs));
                }
            }
            return tracks;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tracks by playlist id", e);
        }
    }

    public List<Track> findByArtistIdUsingFunction(Long artistId) {
        String sql = "SELECT * FROM fn_get_tracks_by_artist(?)";
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, artistId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tracks.add(mapResultSetToTrack(rs));
                }
            }
            return tracks;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tracks by artist using function", e);
        }
    }

    public List<Track> findByAlbumIdUsingFunction(Long albumId) {
        String sql = "SELECT * FROM fn_get_tracks_by_album(?)";
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, albumId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tracks.add(mapResultSetToTrack(rs));
                }
            }
            return tracks;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tracks by album using function", e);
        }
    }

    public List<Track> findPopularTracksUsingFunction(Long limit) {
        String sql = "SELECT * FROM fn_get_popular_tracks(?)";
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tracks.add(mapResultSetToTrack(rs));
                }
            }
            return tracks;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find popular tracks using function", e);
        }
    }

    public List<Track> findPurchasedTracksByUserUsingFunction(Long userId) {
        String sql = "SELECT * FROM fn_get_purchased_tracks_by_user(?)";
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tracks.add(mapResultSetToTrack(rs));
                }
            }
            return tracks;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find purchased tracks using function", e);
        }
    }
}
