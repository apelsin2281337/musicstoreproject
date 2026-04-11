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

    @Query(value = "SELECT * FROM albums WHERE artist_id = :artistId", nativeQuery = true)
    List<Album> findByAlbumArtist_ArtistId(@Param("artistId") Long artistId);

    @Query(value = "SELECT * FROM albums WHERE album_title = :albumTitle", nativeQuery = true)
    List<Album> findAllByAlbumTitle(@Param("albumTitle") String albumTitle);

    @Query(value = "SELECT * FROM albums WHERE album_title = :title AND artist_id = :artistId", nativeQuery = true)
    Optional<Album> findByAlbumTitleAndAlbumArtist(@Param("title") String title, @Param("artistId") Long artistId);

    @Query(value = "SELECT * FROM albums ORDER BY album_rating DESC", nativeQuery = true)
    List<Album> findAllByOrderByAlbumRatingDesc();

    @Query(value = "SELECT * FROM albums WHERE LOWER(album_title) LIKE LOWER(CONCAT('%', :title, '%'))", nativeQuery = true)
    List<Album> findAllByAlbumTitleContainingIgnoreCase(@Param("title") String title);

    @Query(value = "SELECT * FROM albums WHERE album_id = :albumId", nativeQuery = true)
    Optional<Album> findByAlbumId(@Param("albumId") Long albumId);
}
