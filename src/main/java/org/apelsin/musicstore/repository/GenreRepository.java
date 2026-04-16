package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.Genre;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class GenreRepository {

    private final DataSource dataSource;

    public GenreRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Genre mapResultSetToGenre(ResultSet rs) throws SQLException {
        Genre genre = new Genre();
        genre.setGenreId(rs.getLong("genre_id"));
        genre.setGenreName(rs.getString("genre_name"));
        return genre;
    }

    public Genre save(Genre genre) {
        String sql = "INSERT INTO genres (genre_name) VALUES (?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, genre.getGenreName());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    genre.setGenreId(rs.getLong(1));
                }
            }
            return genre;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save genre", e);
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM genres WHERE genre_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete genre", e);
        }
    }

    public Optional<Genre> findById(Long id) {
        String sql = "SELECT * FROM genres WHERE genre_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGenre(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find genre by id", e);
        }
    }

    public List<Genre> findAll() {
        String sql = "SELECT * FROM genres";
        List<Genre> genres = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                genres.add(mapResultSetToGenre(rs));
            }
            return genres;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all genres", e);
        }
    }

    public Optional<Genre> findByGenreName(String name) {
        String sql = "SELECT * FROM genres WHERE genre_name = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGenre(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find genre by name", e);
        }
    }

    public Optional<Genre> findByGenreId(Long genreId) {
        return findById(genreId);
    }

    public List<Genre> findAllGenres() {
        return findAll();
    }

    public Genre update(Genre genre) {
        String sql = "UPDATE genres SET genre_name = ? WHERE genre_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, genre.getGenreName());
            ps.setLong(2, genre.getGenreId());

            ps.executeUpdate();
            return genre;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update genre", e);
        }
    }
}
