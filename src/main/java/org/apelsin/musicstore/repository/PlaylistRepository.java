package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    @Query(value = "SELECT * FROM playlists WHERE user_id = :userId", nativeQuery = true)
    List<Playlist> findByPlaylistOwner_UserId(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM playlists WHERE playlist_id = :playlistId", nativeQuery = true)
    Optional<Playlist> findByPlaylistId(@Param("playlistId") Long playlistId);
}
