package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.Album;
import org.apelsin.musicstore.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    List<Album> findByAlbumArtist_ArtistId(Long artistId);
    List<Album> findAllByAlbumTitle(String albumTitle);
    Optional<Album> findByAlbumTitleAndAlbumArtist(String title, Artist artist);

    List<Album> findAllByOrderByAlbumRatingDesc();
    List<Album> findAllByAlbumTitleContainingIgnoreCase(String title);
}