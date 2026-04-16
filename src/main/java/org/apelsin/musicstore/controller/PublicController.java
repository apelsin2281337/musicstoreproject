package org.apelsin.musicstore.controller;

import lombok.RequiredArgsConstructor;
import org.apelsin.musicstore.model.Album;
import org.apelsin.musicstore.model.Artist;
import org.apelsin.musicstore.model.Genre;
import org.apelsin.musicstore.model.Track;
import org.apelsin.musicstore.repository.AlbumRepository;
import org.apelsin.musicstore.repository.ArtistRepository;
import org.apelsin.musicstore.repository.GenreRepository;
import org.apelsin.musicstore.repository.TrackRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {
    private final TrackRepository trackRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;

    @GetMapping("/genres")
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    @GetMapping("/genres/{genreId}")
    public Genre getGenre(@PathVariable Long genreId) {
        return genreRepository.findById(genreId).orElseThrow();
    }

    @GetMapping("/genres/{genreId}/tracks")
    public List<Track> getTracksByGenre(@PathVariable Long genreId) {
        return trackRepository.findByTrackGenres_GenreId(genreId);
    }

    @GetMapping("/artists")
    public List<Artist> getAllArtists(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "rating") String sort
    ) {
        if (search != null && !search.isEmpty()) {
            return artistRepository.findByArtistNameContainingIgnoreCase(search);
        }
        if ("rating".equals(sort)) {
            return artistRepository.findAllByOrderByArtistRatingDesc();
        }
        return artistRepository.findAll();
    }

    @GetMapping("/artists/{artistId}")
    public Artist getArtist(@PathVariable Long artistId) {
        return artistRepository.findById(artistId).orElseThrow();
    }

    @GetMapping("/artists/{artistId}/tracks")
    public List<Track> getTracksByArtist(@PathVariable Long artistId) {
        return trackRepository.findByTrackArtist_ArtistId(artistId);
    }

    @GetMapping("/artists/{artistId}/albums")
    public List<Album> getAlbumsByArtist(@PathVariable Long artistId) {
        return albumRepository.findByAlbumArtist_ArtistId(artistId);
    }

    @GetMapping("/albums")
    public List<Album> getAllAlbums(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "rating") String sort
    ) {
        if (search != null && !search.isEmpty()) {
            return albumRepository.findAllByAlbumTitleContainingIgnoreCase(search);
        }
        if ("rating".equals(sort)) {
            return albumRepository.findAllByOrderByAlbumRatingDesc();
        }
        return albumRepository.findAll();
    }

    @GetMapping("/albums/{albumId}")
    public Album getAlbum(@PathVariable Long albumId) {
        return albumRepository.findById(albumId).orElseThrow();
    }

    @GetMapping("/albums/{albumId}/tracks")
    public List<Track> getTracksByAlbum(@PathVariable Long albumId) {
        return trackRepository.findByTrackAlbum_AlbumId(albumId);
    }

    @GetMapping("/tracks")
    public List<Track> getAllTracks(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "popular") String sort
    ) {
        if (search != null && !search.isEmpty()) {
            return trackRepository.findAllByTrackTitleContainingIgnoreCase(search);
        }
        if ("popular".equals(sort)) {
            return trackRepository.findAllByOrderByTrackDownloadCountDesc();
        }
        return trackRepository.findAll();
    }

    @GetMapping("/tracks/popular")
    public List<Track> getPopularTracks() {
        return trackRepository.findAllByOrderByTrackDownloadCountDesc();
    }

    @GetMapping("/tracks/{trackId}")
    public Track getTrack(@PathVariable Long trackId) {
        return trackRepository.findById(trackId).orElseThrow();
    }
}
