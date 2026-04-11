package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {
    @Query(value = "SELECT * FROM tracks WHERE artist_id = :artistId", nativeQuery = true)
    List<Track> findByTrackArtist_ArtistId(@Param("artistId") Long artistId);

    @Query(value = "SELECT * FROM tracks WHERE album_id = :albumId", nativeQuery = true)
    List<Track> findByTrackAlbum_AlbumId(@Param("albumId") Long albumId);

    @Query(value = "SELECT * FROM tracks WHERE admin_id = :userId", nativeQuery = true)
    List<Track> findByTrackUploadedBy_UserId(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM tracks ORDER BY track_download_count DESC", nativeQuery = true)
    List<Track> findAllByOrderByTrackDownloadCountDesc();

    @Query(value = "SELECT t.* FROM tracks t JOIN trackGenres tg ON t.track_id = tg.track_id WHERE tg.genre_id = :genreId", nativeQuery = true)
    List<Track> findByTrackGenres_GenreId(@Param("genreId") Long genreId);

    @Query(value = "SELECT * FROM tracks WHERE track_title = :trackTitle", nativeQuery = true)
    List<Track> findAllByTrackTitle(@Param("trackTitle") String trackTitle);

    @Query(value = "SELECT * FROM tracks WHERE LOWER(track_title) LIKE LOWER(CONCAT('%', :title, '%'))", nativeQuery = true)
    List<Track> findAllByTrackTitleContainingIgnoreCase(@Param("title") String title);

    @Query(value = "SELECT * FROM tracks WHERE track_id = :trackId", nativeQuery = true)
    Optional<Track> findByTrackId(@Param("trackId") Long trackId);
}
