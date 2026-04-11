package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    @Query(value = "SELECT * FROM genres WHERE genre_name = :name", nativeQuery = true)
    Optional<Genre> findByGenreName(@Param("name") String name);

    @Query(value = "SELECT * FROM genres WHERE genre_id = :genreId", nativeQuery = true)
    Optional<Genre> findByGenreId(@Param("genreId") Long genreId);

    @Query(value = "SELECT * FROM genres", nativeQuery = true)
    List<Genre> findAllGenres();
}
