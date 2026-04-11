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
    List<Artist> findAllByOrderByArtistRatingDesc();
    Optional<Artist> findByArtistName(String name);
    List<Artist> findByArtistNameContainingIgnoreCase(String name);
}
