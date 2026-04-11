package org.apelsin.musicstore.service;

import lombok.RequiredArgsConstructor;
import org.apelsin.musicstore.model.*;
import org.apelsin.musicstore.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public void buyTracks(Long userId, List<Long> trackIds) {
        User user = userRepository.findById(userId).orElseThrow();
        List<Track> tracksToBuy = trackRepository.findAllById(trackIds);

        Order order = new Order();
        order.setOrderUser(user);
        order.setOrderItems(tracksToBuy);;
        order.setOrderTotalAmount(tracksToBuy.stream().mapToDouble(Track::getTrackPrice).sum());
        orderRepository.save(order);

        user.getUserPurchasedTracks().addAll(tracksToBuy);
        userRepository.save(user);
    }

    public List<Track> getRecommendations(Long trackId) {
        Track track = trackRepository.findById(trackId).orElseThrow();
        List<Long> genreIds = track.getTrackGenres().stream().map(Genre::getGenreId).toList();

        return trackRepository.findAll().stream()
                .filter(t -> !t.getTrackId().equals(trackId))
                .filter(t -> t.getTrackGenres().stream().anyMatch(g -> genreIds.contains(g.getGenreId())))
                .limit(5)
                .collect(Collectors.toList());
    }

    @Transactional
    public Track downloadTrack(Long trackId) {
        Track track = trackRepository.findById(trackId).orElseThrow();

        track.setTrackDownloadCount(track.getTrackDownloadCount() + 1);

        if (track.getTrackAlbum() != null) {
            Album album = track.getTrackAlbum();
            album.setAlbumRating(album.getAlbumRating() + 1);
        }

        Artist artist = track.getTrackArtist();
        artist.setArtistRating(artist.getArtistRating() + 1);

        return trackRepository.save(track);
    }

    @Transactional
    public User changeRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setUserRole(newRole);
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
}