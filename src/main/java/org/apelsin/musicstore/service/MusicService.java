package org.apelsin.musicstore.service;

import lombok.RequiredArgsConstructor;
import org.apelsin.musicstore.model.Track;
import org.apelsin.musicstore.repository.TrackRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MusicService {
    private final TrackRepository trackRepository;

    public List<Track> getTracksByArtist(Long artistId) {
        return trackRepository.findByTrackArtist_ArtistId(artistId);
    }

}