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
    List<Track> findByTrackArtist_ArtistId(Long artistId);
    List<Track> findByTrackAlbum_AlbumId(Long albumId);
    List<Track> findByTrackUploadedBy_UserId(Long userId);
    List<Track> findAllByOrderByTrackDownloadCountDesc();
    List<Track> findByTrackGenres_GenreId(Long genreId);
    List<Track> findAllByTrackTitle(String trackTitle);
    List<Track> findAllByTrackTitleContainingIgnoreCase(String title);
}