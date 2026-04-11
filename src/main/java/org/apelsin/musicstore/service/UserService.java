package org.apelsin.musicstore.service;

import lombok.RequiredArgsConstructor;
import org.apelsin.musicstore.model.*;
import org.apelsin.musicstore.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final OrderRepository orderRepository;
    private final AlbumRepository albumRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Transactional
    public Order buyTracks(Long userId, List<Long> trackIds, String paymentMethod) {
        User user = userRepository.findById(userId).orElseThrow();
        List<Track> tracksToBuy = trackRepository.findAllById(trackIds);
        
        double total = tracksToBuy.stream().mapToDouble(Track::getTrackPrice).sum();

        Order order = new Order();
        order.setOrderUser(user);
        order.setOrderItems(tracksToBuy);
        order.setOrderTotalAmount(total);
        order.setOrderDate(LocalDateTime.now());
        order = orderRepository.save(order);

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setTransactionOrder(order);
        transaction.setPaymentMethod(paymentMethod != null ? paymentMethod : "CARD");
        transaction.setTransactionStatus("SUCCESS");
        transaction.setExternalTransactionId("TXN-" + System.currentTimeMillis());
        transaction.setTransactionCompletionDate(LocalDateTime.now());
        paymentTransactionRepository.save(transaction);

        user.getUserPurchasedTracks().addAll(tracksToBuy);
        userRepository.save(user);
        
        return order;
    }

    @Transactional
    public Order buyAlbum(Long userId, Long albumId, String paymentMethod) {
        User user = userRepository.findById(userId).orElseThrow();
        Album album = albumRepository.findById(albumId).orElseThrow();
        
        List<Track> albumTracks = trackRepository.findByTrackAlbum_AlbumId(albumId);
        
        Order order = new Order();
        order.setOrderUser(user);
        order.setOrderItems(albumTracks);
        order.setOrderTotalAmount(album.getAlbumPrice());
        order.setOrderDate(LocalDateTime.now());
        order = orderRepository.save(order);

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setTransactionOrder(order);
        transaction.setPaymentMethod(paymentMethod != null ? paymentMethod : "CARD");
        transaction.setTransactionStatus("SUCCESS");
        transaction.setExternalTransactionId("TXN-" + System.currentTimeMillis());
        transaction.setTransactionCompletionDate(LocalDateTime.now());
        paymentTransactionRepository.save(transaction);

        user.getUserPurchasedTracks().addAll(albumTracks);
        user.getUserPurchasedAlbums().add(album);
        userRepository.save(user);
        
        return order;
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
    
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByOrderUser_UserId(userId);
    }
}
