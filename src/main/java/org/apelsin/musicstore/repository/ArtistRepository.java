package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    @Query(value = "SELECT * FROM artists ORDER BY artist_rating DESC", nativeQuery = true)
    List<Artist> findAllByOrderByArtistRatingDesc();

    @Query(value = "SELECT * FROM artists WHERE artist_name = :name", nativeQuery = true)
    Optional<Artist> findByArtistName(@Param("name") String name);

    @Query(value = "SELECT * FROM artists WHERE LOWER(artist_name) LIKE LOWER(CONCAT('%', :name, '%'))", nativeQuery = true)
    List<Artist> findByArtistNameContainingIgnoreCase(@Param("name") String name);

    @Query(value = "SELECT * FROM artists WHERE artist_id = :artistId", nativeQuery = true)
    Optional<Artist> findByArtistId(@Param("artistId") Long artistId);
}
