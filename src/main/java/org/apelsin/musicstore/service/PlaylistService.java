package org.apelsin.musicstore.service;

import lombok.RequiredArgsConstructor;
import org.apelsin.musicstore.model.Playlist;
import org.apelsin.musicstore.model.User;
import org.apelsin.musicstore.repository.PlaylistRepository;
import org.apelsin.musicstore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    public List<Playlist> getUserPlaylists(Long userId) {
        return playlistRepository.findByPlaylistOwner_UserId(userId);
    }

    public Playlist getPlaylistById(Long playlistId) {
        return playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));
    }

    @Transactional
    public Playlist createPlaylist(Long userId, String title) {
        User user = userRepository.findById(userId).orElseThrow();
        Playlist playlist = new Playlist();
        playlist.setPlaylistTitle(title);
        playlist.setPlaylistOwner(user);
        playlist.setPlaylistTracks(new ArrayList<>());
        return playlistRepository.save(playlist);
    }

    @Transactional
    public Playlist addTrackToPlaylist(Long playlistId, Long trackId) {
        playlistRepository.addTrackToPlaylist(playlistId, trackId);
        return playlistRepository.findById(playlistId).orElseThrow();
    }

    @Transactional
    public void removeTrackFromPlaylist(Long playlistId, Long trackId) {
        playlistRepository.removeTrackFromPlaylist(playlistId, trackId);
    }

    @Transactional
    public Playlist updatePlaylist(Long playlistId, String newTitle) {
        Playlist playlist = playlistRepository.findById(playlistId).orElseThrow();
        playlist.setPlaylistTitle(newTitle);
        return playlistRepository.save(playlist);
    }

    @Transactional
    public void deletePlaylist(Long playlistId) {
        playlistRepository.deleteById(playlistId);
    }
}
