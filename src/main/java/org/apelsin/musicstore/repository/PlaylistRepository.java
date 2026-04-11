package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    // Плейлисты конкретного пользователя
    List<Playlist> findByPlaylistOwner_UserId(Long userId);
}
