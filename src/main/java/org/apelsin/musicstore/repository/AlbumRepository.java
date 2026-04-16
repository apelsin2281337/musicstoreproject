package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.Album;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AlbumRepository {

    private final DataSource dataSource;
    private final ArtistRepository artistRepository;

    public AlbumRepository(DataSource dataSource, ArtistRepository artistRepository) {
        this.dataSource = dataSource;
        this.artistRepository = artistRepository;
    }

    private Album mapResultSetToAlbum(ResultSet rs) throws SQLException {
        Album album = new Album();
        album.setAlbumId(rs.getLong("album_id"));
        album.setAlbumTitle(rs.getString("album_title"));
        album.setAlbumReleaseYear(rs.getInt("album_release_year"));
        album.setAlbumPrice(rs.getDouble("album_price"));
        album.setAlbumRating(rs.getLong("album_rating"));

        Long artistId = rs.getLong("artist_id");
        artistRepository.findById(artistId).ifPresent(album::setAlbumArtist);

        return album;
    }

    public Album save(Album album) {
        String sql = "INSERT INTO albums (album_title, album_release_year, album_price, album_rating, artist_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, album.getAlbumTitle());
            ps.setObject(2, album.getAlbumReleaseYear(), Types.INTEGER);
            ps.setObject(3, album.getAlbumPrice(), Types.DOUBLE);
            ps.setObject(4, album.getAlbumRating(), Types.BIGINT);
            ps.setLong(5, album.getAlbumArtist().getArtistId());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    album.setAlbumId(rs.getLong(1));
                }
            }
            return album;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save album", e);
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM albums WHERE album_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete album", e);
        }
    }

    public Optional<Album> findById(Long id) {
        String sql = "SELECT * FROM albums WHERE album_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAlbum(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find album by id", e);
        }
    }

    public List<Album> findAll() {
        String sql = "SELECT * FROM albums";
        List<Album> albums = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                albums.add(mapResultSetToAlbum(rs));
            }
            return albums;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all albums", e);
        }
    }

    public List<Album> findByAlbumArtist_ArtistId(Long artistId) {
        String sql = "SELECT * FROM albums WHERE artist_id = ?";
        List<Album> albums = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, artistId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    albums.add(mapResultSetToAlbum(rs));
                }
            }
            return albums;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find albums by artist id", e);
        }
    }

    public List<Album> findAllByAlbumTitle(String albumTitle) {
        String sql = "SELECT * FROM albums WHERE album_title = ?";
        List<Album> albums = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, albumTitle);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    albums.add(mapResultSetToAlbum(rs));
                }
            }
            return albums;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find albums by title", e);
        }
    }

    public Optional<Album> findByAlbumTitleAndAlbumArtist(String title, Long artistId) {
        String sql = "SELECT * FROM albums WHERE album_title = ? AND artist_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setLong(2, artistId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAlbum(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find album by title and artist", e);
        }
    }

    public List<Album> findAllByOrderByAlbumRatingDesc() {
        String sql = "SELECT * FROM albums ORDER BY album_rating DESC";
        List<Album> albums = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                albums.add(mapResultSetToAlbum(rs));
            }
            return albums;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find albums by rating", e);
        }
    }

    public List<Album> findAllByAlbumTitleContainingIgnoreCase(String title) {
        String sql = "SELECT * FROM albums WHERE LOWER(album_title) LIKE LOWER(CONCAT('%', ?, '%'))";
        List<Album> albums = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    albums.add(mapResultSetToAlbum(rs));
                }
            }
            return albums;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find albums by title containing", e);
        }
    }

    public Optional<Album> findByAlbumId(Long albumId) {
        return findById(albumId);
    }

    public Album update(Album album) {
        String sql = "UPDATE albums SET album_title = ?, album_release_year = ?, album_price = ?, album_rating = ?, artist_id = ? WHERE album_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, album.getAlbumTitle());
            ps.setObject(2, album.getAlbumReleaseYear(), Types.INTEGER);
            ps.setObject(3, album.getAlbumPrice(), Types.DOUBLE);
            ps.setObject(4, album.getAlbumRating(), Types.BIGINT);
            ps.setLong(5, album.getAlbumArtist().getArtistId());
            ps.setLong(6, album.getAlbumId());

            ps.executeUpdate();
            return album;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update album", e);
        }
    }
}
