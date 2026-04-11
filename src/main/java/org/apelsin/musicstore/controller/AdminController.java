package org.apelsin.musicstore.controller;

import lombok.RequiredArgsConstructor;
import org.apelsin.musicstore.model.*;
import org.apelsin.musicstore.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final AdminActionLogRepository actionLogRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    @Value("${upload.path}")
    private String uploadPath;

    private void logAction(User admin, String actionType, String targetEntity, Long targetId, String details) {
        AdminActionLog log = new AdminActionLog();
        log.setAdmin(admin);
        log.setActionType(actionType);
        log.setTargetEntity(targetEntity);
        log.setTargetId(targetId);
        log.setActionDetails(details);
        actionLogRepository.save(log);
    }

    @PostMapping("/promote/{userId}")
    public ResponseEntity<?> promote(@PathVariable Long userId, @RequestParam("adminId") Long adminId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        User admin = userRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Админ не найден"));
        
        if (user.getUserRole() == Role.ADMIN){
            return ResponseEntity.badRequest().body("Пользователь уже является администратором");
        }
        
        user.setUserRole(Role.ADMIN);
        userRepository.save(user);
        
        logAction(admin, "PROMOTE", "User", userId, "Повышен до администратора: " + user.getUserUsername());
        
        return ResponseEntity.ok("Пользователь повышен до администратора");
    }

    @PostMapping("/demote/{userId}")
    public ResponseEntity<?> demote(@PathVariable Long userId, @RequestParam("adminId") Long adminId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        User admin = userRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Админ не найден"));
        
        if (user.getUserRole() == Role.USER){
            return ResponseEntity.badRequest().body("Пользователь уже является обычным пользователем");
        }
        
        user.setUserRole(Role.USER);
        userRepository.save(user);
        
        logAction(admin, "DEMOTE", "User", userId, "Понижен до пользователя: " + user.getUserUsername());
        
        return ResponseEntity.ok("Администратор понижен до пользователя");
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/addartist")
    public ResponseEntity<?> addArtist(
            @RequestParam("name") String name,
            @RequestParam("description") String desc,
            @RequestParam("rating") Long rating,
            @RequestParam("adminId") Long adminId
    ){
        try {
            User admin = userRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Админ не найден"));
            
            if (name == null || name.isEmpty()) {
                return ResponseEntity.badRequest().body("Имя артиста не может быть пустым");
            }

            Artist artist = new Artist();
            artist.setArtistName(name);
            artist.setArtistDescription(desc);
            artist.setArtistRating(rating != null ? rating : 0L);

            Artist savedArtist = artistRepository.save(artist);

            logAction(admin, "ADD_ARTIST", "Artist", savedArtist.getArtistId(), "Добавлен артист: " + name);

            return ResponseEntity.ok("Артист успешно добавлен! ID: " + savedArtist.getArtistId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при добавлении артиста: " + e.getMessage());
        }
    }

    @PostMapping("/addalbum")
    public ResponseEntity<?> addAlbum(
            @RequestParam("title") String title,
            @RequestParam("year") Integer year,
            @RequestParam("price") Double price,
            @RequestParam("artistId") Long artistId,
            @RequestParam("adminId") Long adminId
    ) {
        try {
            User admin = userRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Админ не найден"));
            Artist artist = artistRepository.findById(artistId)
                    .orElseThrow(() -> new RuntimeException("Артист с таким ID не найден"));

            Album album = new Album();
            album.setAlbumTitle(title);
            album.setAlbumReleaseYear(year);
            album.setAlbumPrice(price);
            album.setAlbumArtist(artist);
            album.setAlbumRating(0L);

            Album savedAlbum = albumRepository.save(album);
            
            logAction(admin, "ADD_ALBUM", "Album", savedAlbum.getAlbumId(), "Добавлен альбом: " + title + " (артист: " + artist.getArtistName() + ")");

            return ResponseEntity.ok("Альбом '" + title + "' успешно создан! ID: " + savedAlbum.getAlbumId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Ошибка при создании альбома: " + e.getMessage());
        }
    }

    @DeleteMapping("/albums/{albumId}")
    public ResponseEntity<?> deleteAlbum(@PathVariable Long albumId, @RequestParam("adminId") Long adminId){
    Album album = albumRepository.findById(albumId).orElseThrow();
    User admin = userRepository.findById(adminId).orElseThrow();
    
    trackRepository.findByTrackAlbum_AlbumId(albumId).forEach(track -> {
        track.setTrackAlbum(null);
        trackRepository.save(track);
    });
    
    reviewRepository.findByReviewAlbum_AlbumId(albumId).forEach(review -> {
        review.setReviewAlbum(null);
        reviewRepository.save(review);
    });
    
    userRepository.findAll().forEach(user -> {
        if (user.getUserPurchasedAlbums() != null && user.getUserPurchasedAlbums().contains(album)) {
            user.getUserPurchasedAlbums().remove(album);
            userRepository.save(user);
        }
    });

    logAction(admin, "DELETE_ALBUM", "Album", albumId, "Удален альбом: " + album.getAlbumTitle());

    albumRepository.deleteById(albumId);
    return ResponseEntity.ok("Альбом удален");

    }



    @PostMapping("/uploadtrack")
    public ResponseEntity<?> uploadTrack(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("price") Double price,
            @RequestParam("artistId") Long artistId,
            @RequestParam(value = "albumId", required = false) Long albumId,
            @RequestParam("adminId") Long adminId) {

        try {
            User admin = userRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Админ не найден"));
            
            Path root = Paths.get(uploadPath);
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            Artist artist = artistRepository.findById(artistId)
                    .orElseThrow(() -> new RuntimeException("Артист не найден"));

            Album album = null;
            if (albumId != null) {
                album = albumRepository.findById(albumId)
                        .orElseThrow(() -> new RuntimeException("Альбом не найден"));
            }

            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), root.resolve(filename));

            Track track = new Track();
            track.setTrackTitle(title);
            track.setTrackPrice(price);
            track.setTrackArtist(artist);
            track.setTrackAlbum(album);
            track.setTrackUploadedBy(admin);
            track.setTrackFilePath("/uploads/music/" + filename);
            track.setTrackDownloadCount(0L);

            Track saved = trackRepository.save(track);
            
            logAction(admin, "UPLOAD_TRACK", "Track", saved.getTrackId(), 
                    "Загружен трек: " + title + " (артист: " + artist.getArtistName() + 
                    (album != null ? ", альбом: " + album.getAlbumTitle() : "") + ")");

            return ResponseEntity.ok("Трек загружен! ID: " + saved.getTrackId());

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при сохранении файла: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Ошибка: " + e.getMessage());
        }
    }

    @DeleteMapping("/tracks/{trackId}/delete")
    public ResponseEntity<?> deleteTrack(@PathVariable Long trackId, @RequestParam("adminId") Long adminId) {
        try {
            Track track = trackRepository.findById(trackId).orElseThrow(() -> new RuntimeException("Track not found"));
            User admin = userRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Admin not found"));
            
            orderRepository.findAll().forEach(order -> {
                if (order.getOrderItems() != null && order.getOrderItems().contains(track)) {
                    order.getOrderItems().remove(track);
                    orderRepository.save(order);
                }
            });
            
            userRepository.findAll().forEach(user -> {
                if (user.getUserPurchasedTracks() != null && user.getUserPurchasedTracks().contains(track)) {
                    user.getUserPurchasedTracks().remove(track);
                    userRepository.save(user);
                }
            });
            
            logAction(admin, "DELETE_TRACK", "Track", trackId, "Удален трек: " + track.getTrackTitle());
            
            trackRepository.delete(track);
            return ResponseEntity.ok("Трек удален");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/tracks/{trackId}")
    public ResponseEntity<?> updateTrack(@PathVariable Long trackId, 
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Double price,
            @RequestParam Long adminId) {
        Track track = trackRepository.findById(trackId).orElseThrow();
        User admin = userRepository.findById(adminId).orElseThrow();
        
        StringBuilder changes = new StringBuilder();
        if (title != null && !title.equals(track.getTrackTitle())) {
            changes.append("название: ").append(track.getTrackTitle()).append(" -> ").append(title);
            track.setTrackTitle(title);
        }
        if (price != null && !price.equals(track.getTrackPrice())) {
            if (changes.length() > 0) changes.append(", ");
            changes.append("цена: ").append(track.getTrackPrice()).append(" -> ").append(price);
            track.setTrackPrice(price);
        }
        
        trackRepository.save(track);
        
        if (changes.length() > 0) {
            logAction(admin, "UPDATE_TRACK", "Track", trackId, "Обновлен трек ID=" + trackId + ": " + changes);
        }
        
        return ResponseEntity.ok("Трек обновлен");
    }

    @GetMapping("/tracks/popular")
    public List<Track> getPopularTracks() {
        return trackRepository.findAllByOrderByTrackDownloadCountDesc();
    }

    @GetMapping("/logs")
    public List<AdminActionLog> getAllLogs() {
        return actionLogRepository.findAllByOrderByActionTimestampDesc();
    }

    @GetMapping("/logs/track/{trackId}")
    public List<AdminActionLog> getTrackLiability(@PathVariable Long trackId) {
        return actionLogRepository.findByTargetEntityAndTargetId("Track", trackId);
    }
}
