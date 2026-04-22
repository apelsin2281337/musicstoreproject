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
import java.util.ArrayList;
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
    private final LicenseRepository licenseRepository;
    private final GenreRepository genreRepository;

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
        userRepository.update(user);
        
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
        userRepository.update(user);
        
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
        try {
            User admin = userRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Админ не найден"));
            Album album = albumRepository.findById(albumId).orElseThrow(() -> new RuntimeException("Альбом не найден"));

            List<Track> tracks = trackRepository.findByTrackAlbum_AlbumId(albumId);
            for (Track track : tracks) {
                Long trackId = track.getTrackId();
                var optLicense = licenseRepository.findByLicenseTrack_TrackId(trackId);
                if (optLicense.isPresent()) {
                    licenseRepository.deleteById(optLicense.get().getLicenseId());
                }
                orderRepository.removeOrderTrack(trackId);
                orderRepository.removePlaylistTrack(trackId);
                userRepository.removePurchasedTrack(trackId);
                trackRepository.deleteById(trackId);
            }

            reviewRepository.findByReviewAlbum_AlbumId(albumId).forEach(review -> {
                review.setReviewAlbum(null);
                reviewRepository.update(review);
            });

            userRepository.removePurchasedAlbum(albumId);

            logAction(admin, "DELETE_ALBUM", "Album", albumId, "Удален альбом: " + album.getAlbumTitle());

            albumRepository.deleteById(albumId);
            return ResponseEntity.ok("Альбом удален (вместе с треками)");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка: " + e.getMessage());
        }
    }



    @PostMapping("/uploadtrack")
    public ResponseEntity<?> uploadTrack(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("price") Double price,
            @RequestParam("artistId") Long artistId,
            @RequestParam(value = "albumId", required = false) Long albumId,
            @RequestParam("adminId") Long adminId,
            @RequestParam("licenseTerms") String terms,
            @RequestParam(value = "genreIds", required = false) List<Long> genreIds
    )
        {

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

            if (genreIds != null && !genreIds.isEmpty()) {
                List<Genre> genres = new ArrayList<>();
                for (Long gid : genreIds) {
                    genreRepository.findById(gid).ifPresent(genres::add);
                }
                track.setTrackGenres(genres);
            }

            Track saved = trackRepository.save(track);

            License license = new License();
            license.setLicenseTrack(saved);
            license.setLicenseTerms(terms);
            license.setLicenseContractNumber("CONTRACT-" + saved.getTrackId());
            license.setLicenseOwnerName(artist.getArtistName());
            license.setLicenseStartDate(java.time.LocalDate.now());
            license.setLicenseExpirationDate(java.time.LocalDate.now().plusYears(1));
            license.setUploader(admin.getUserUsername());
            licenseRepository.save(license);

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

            var optLicense = licenseRepository.findByLicenseTrack_TrackId(trackId);
            if (optLicense.isPresent()) {
                licenseRepository.deleteById(optLicense.get().getLicenseId());
            }

            orderRepository.removeOrderTrack(trackId);
            orderRepository.removePlaylistTrack(trackId);

            userRepository.removePurchasedTrack(trackId);

            logAction(admin, "DELETE_TRACK", "Track", trackId, "Удален трек: " + track.getTrackTitle());

            trackRepository.deleteById(trackId);
            return ResponseEntity.ok("Трек удален");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
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

    @PostMapping("/genres")
    public ResponseEntity<?> addGenre(@RequestParam("name") String name, @RequestParam("adminId") Long adminId) {
        try {
            User admin = userRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Админ не найден"));
            
            if (name == null || name.isEmpty()) {
                return ResponseEntity.badRequest().body("Название жанра не может быть пустым");
            }
            
            if (genreRepository.findByGenreName(name).isPresent()) {
                return ResponseEntity.badRequest().body("Жанр уже существует");
            }
            
            Genre genre = new Genre();
            genre.setGenreName(name);
            Genre saved = genreRepository.save(genre);
            
            logAction(admin, "ADD_GENRE", "Genre", saved.getGenreId(), "Добавлен жанр: " + name);
            
            return ResponseEntity.ok("Жанр добавлен! ID: " + saved.getGenreId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка: " + e.getMessage());
        }
    }

    @DeleteMapping("/genres/{genreId}")
    public ResponseEntity<?> deleteGenre(@PathVariable Long genreId, @RequestParam("adminId") Long adminId) {
        try {
            User admin = userRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Админ не найден"));
            Genre genre = genreRepository.findById(genreId).orElseThrow(() -> new RuntimeException("Жанр не найден"));

            genreRepository.removeGenreFromAllTracks(genreId);

            logAction(admin, "DELETE_GENRE", "Genre", genreId, "Удален жанр: " + genre.getGenreName());

            genreRepository.deleteById(genreId);
            return ResponseEntity.ok("Жанр удален");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка: " + e.getMessage());
        }
    }

    @PutMapping("/artists/{artistId}")
    public ResponseEntity<?> updateArtist(@PathVariable Long artistId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long rating,
            @RequestParam Long adminId) {
        try {
            User admin = userRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Админ не найден"));
            Artist artist = artistRepository.findById(artistId).orElseThrow(() -> new RuntimeException("Артист не найден"));
            
            StringBuilder changes = new StringBuilder();
            if (name != null && !name.equals(artist.getArtistName())) {
                changes.append("название: ").append(artist.getArtistName()).append(" -> ").append(name);
                artist.setArtistName(name);
            }
            if (description != null && !description.equals(artist.getArtistDescription())) {
                if (changes.length() > 0) changes.append(", ");
                changes.append("описание: ").append(artist.getArtistDescription()).append(" -> ").append(description);
                artist.setArtistDescription(description);
            }
            if (rating != null && !rating.equals(artist.getArtistRating())) {
                if (changes.length() > 0) changes.append(", ");
                changes.append("рейтинг: ").append(artist.getArtistRating()).append(" -> ").append(rating);
                artist.setArtistRating(rating);
            }
            
            artistRepository.update(artist);
            
            if (changes.length() > 0) {
                logAction(admin, "UPDATE_ARTIST", "Artist", artistId, "Обновлен артист ID=" + artistId + ": " + changes);
            }
            
            return ResponseEntity.ok("Артист обновлен");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка: " + e.getMessage());
        }
    }

    @DeleteMapping("/artists/{artistId}")
    public ResponseEntity<?> deleteArtist(@PathVariable Long artistId, @RequestParam("adminId") Long adminId) {
        try {
            User admin = userRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Админ не найден"));
            Artist artist = artistRepository.findById(artistId).orElseThrow(() -> new RuntimeException("Артист не найден"));

            List<Track> tracks = trackRepository.findByTrackArtist_ArtistId(artistId);
            for (Track track : tracks) {
                Long trackId = track.getTrackId();
                var optLicense = licenseRepository.findByLicenseTrack_TrackId(trackId);
                if (optLicense.isPresent()) {
                    licenseRepository.deleteById(optLicense.get().getLicenseId());
                }
                orderRepository.removeOrderTrack(trackId);
                orderRepository.removePlaylistTrack(trackId);
                userRepository.removePurchasedTrack(trackId);
                trackRepository.deleteById(trackId);
            }

            List<Album> albums = albumRepository.findByAlbumArtist_ArtistId(artistId);
            for (Album album : albums) {
                List<Track> albumTracks = trackRepository.findByTrackAlbum_AlbumId(album.getAlbumId());
                for (Track track : albumTracks) {
                    Long trackId = track.getTrackId();
var optLicense = licenseRepository.findByLicenseTrack_TrackId(trackId);
                if (optLicense.isPresent()) {
                    licenseRepository.deleteById(optLicense.get().getLicenseId());
                }
                orderRepository.removeOrderTrack(trackId);
                orderRepository.removePlaylistTrack(trackId);
                userRepository.removePurchasedTrack(trackId);
                trackRepository.deleteById(trackId);
                }
                reviewRepository.findByReviewAlbum_AlbumId(album.getAlbumId()).forEach(review -> {
                    review.setReviewAlbum(null);
                    reviewRepository.update(review);
                });
                userRepository.removePurchasedAlbum(album.getAlbumId());
                albumRepository.deleteById(album.getAlbumId());
            }

            logAction(admin, "DELETE_ARTIST", "Artist", artistId, "Удален артист: " + artist.getArtistName());

            artistRepository.deleteByArtistId(artistId);
            return ResponseEntity.ok("Артист удален (вместе с ал��бомами и треками)");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка: " + e.getMessage());
        }
    }

    @PostMapping("/addtrack")
    public ResponseEntity<?> addTrack(
            @RequestParam("title") String title,
            @RequestParam("price") Double price,
            @RequestParam("artistId") Long artistId,
            @RequestParam(value = "albumId", required = false) Long albumId,
            @RequestParam("adminId") Long adminId,
            @RequestParam("licenseTerms") String terms,
            @RequestParam(value = "genreIds", required = false) List<Long> genreIds
    ) {
        try {
            User admin = userRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Админ не найден"));
            Artist artist = artistRepository.findById(artistId).orElseThrow(() -> new RuntimeException("Артист не найден"));

            Album album = null;
            if (albumId != null) {
                album = albumRepository.findById(albumId).orElseThrow(() -> new RuntimeException("Альбом не найден"));
            }

            Track track = new Track();
            track.setTrackTitle(title);
            track.setTrackPrice(price);
            track.setTrackArtist(artist);
            track.setTrackAlbum(album);
            track.setTrackUploadedBy(admin);
            track.setTrackFilePath("");
            track.setTrackDownloadCount(0L);

            if (genreIds != null && !genreIds.isEmpty()) {
                List<Genre> genres = new ArrayList<>();
                for (Long gid : genreIds) {
                    genreRepository.findById(gid).ifPresent(genres::add);
                }
                track.setTrackGenres(genres);
            }

            Track saved = trackRepository.save(track);

            License license = new License();
            license.setLicenseTrack(saved);
            license.setLicenseTerms(terms);
            license.setLicenseContractNumber("CONTRACT-" + saved.getTrackId());
            license.setLicenseOwnerName(artist.getArtistName());
            license.setLicenseStartDate(java.time.LocalDate.now());
            license.setLicenseExpirationDate(java.time.LocalDate.now().plusYears(1));
            license.setUploader(admin.getUserUsername());
            licenseRepository.save(license);

            logAction(admin, "ADD_TRACK", "Track", saved.getTrackId(),
                    "Добавлен трек: " + title + " (артист: " + artist.getArtistName() + ")");

            return ResponseEntity.ok("Трек добавлен! ID: " + saved.getTrackId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка: " + e.getMessage());
        }
    }

    @PutMapping("/albums/{albumId}")
    public ResponseEntity<?> updateAlbum(@PathVariable Long albumId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Double price,
            @RequestParam(required = false) Long artistId,
            @RequestParam Long adminId) {
        try {
            User admin = userRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Админ не найден"));
            Album album = albumRepository.findById(albumId).orElseThrow(() -> new RuntimeException("Альбом не найден"));
            
            StringBuilder changes = new StringBuilder();
            if (title != null && !title.equals(album.getAlbumTitle())) {
                changes.append("название: ").append(album.getAlbumTitle()).append(" -> ").append(title);
                album.setAlbumTitle(title);
            }
            if (year != null && !year.equals(album.getAlbumReleaseYear())) {
                if (changes.length() > 0) changes.append(", ");
                changes.append("год: ").append(album.getAlbumReleaseYear()).append(" -> ").append(year);
                album.setAlbumReleaseYear(year);
            }
            if (price != null && !price.equals(album.getAlbumPrice())) {
                if (changes.length() > 0) changes.append(", ");
                changes.append("цена: ").append(album.getAlbumPrice()).append(" -> ").append(price);
                album.setAlbumPrice(price);
            }
            if (artistId != null && (album.getAlbumArtist() == null || !artistId.equals(album.getAlbumArtist().getArtistId()))) {
                Artist artist = artistRepository.findById(artistId).orElseThrow(() -> new RuntimeException("Артист не найден"));
                if (changes.length() > 0) changes.append(", ");
                changes.append("артист: ").append(album.getAlbumArtist() != null ? album.getAlbumArtist().getArtistName() : "null").append(" -> ").append(artist.getArtistName());
                album.setAlbumArtist(artist);
            }
            
            albumRepository.update(album);
            
            if (changes.length() > 0) {
                logAction(admin, "UPDATE_ALBUM", "Album", albumId, "Обновлен альбом ID=" + albumId + ": " + changes);
            }
            
            return ResponseEntity.ok("Альбом обновлен");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка: " + e.getMessage());
        }
    }

    @PutMapping("/tracks/{trackId}")
    public ResponseEntity<?> updateTrack(@PathVariable Long trackId, 
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Double price,
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) Long albumId,
            @RequestParam(required = false) List<Long> genreIds,
            @RequestParam Long adminId) {
        try {
            User admin = userRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Админ не найден"));
            Track track = trackRepository.findById(trackId).orElseThrow(() -> new RuntimeException("Трек не найден"));
            
            StringBuilder changes = new StringBuilder();
            if (title != null && !title.equals(track.getTrackTitle())) {
                changes.append("название: ").append(track.getTrackTitle()).append(" -> ").append(title);
                track.setTrackTitle(title);
            }
            if (price != null && !price.equals(track.getTrackPrice())) {
                if (!changes.isEmpty()) changes.append(", ");
                changes.append("цена: ").append(track.getTrackPrice()).append(" -> ").append(price);
                track.setTrackPrice(price);
            }
            if (artistId != null && (track.getTrackArtist() == null || !artistId.equals(track.getTrackArtist().getArtistId()))) {
                Artist artist = artistRepository.findById(artistId).orElseThrow(() -> new RuntimeException("Артист не найден"));
                if (!changes.isEmpty()) changes.append(", ");
                changes.append("артист: ").append(track.getTrackArtist() != null ? track.getTrackArtist().getArtistName() : "null").append(" -> ").append(artist.getArtistName());
                track.setTrackArtist(artist);
            }
            if (albumId != null && (track.getTrackAlbum() == null || !albumId.equals(track.getTrackAlbum().getAlbumId()))) {
                Album album = albumRepository.findById(albumId).orElse(null);
                if (!changes.isEmpty()) changes.append(", ");
                changes.append("альбом: ").append(track.getTrackAlbum() != null ? track.getTrackAlbum().getAlbumTitle() : "null").append(" -> ").append(album != null ? album.getAlbumTitle() : "null");
                track.setTrackAlbum(album);
            }
            if (genreIds != null) {
                List<Genre> genres = new ArrayList<>();
                for (Long gid : genreIds) {
                    genreRepository.findById(gid).ifPresent(genres::add);
                }
                if (!changes.isEmpty()) changes.append(", ");
                changes.append("жанры обновлены");
                track.setTrackGenres(genres);
            }
            
            trackRepository.update(track);
            
            if (!changes.isEmpty()) {
                logAction(admin, "UPDATE_TRACK", "Track", trackId, "Обновлен трек ID=" + trackId + ": " + changes);
            }
            
            return ResponseEntity.ok("Трек обновлен");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка: " + e.getMessage());
        }
    }

    @PutMapping("/genres/{genreId}")
    public ResponseEntity<?> updateGenre(@PathVariable Long genreId,
            @RequestParam(required = false) String name,
            @RequestParam Long adminId) {
        try {
            User admin = userRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Админ не найден"));
            Genre genre = genreRepository.findById(genreId).orElseThrow(() -> new RuntimeException("Жанр не найден"));
            
            StringBuilder changes = new StringBuilder();
            if (name != null && !name.equals(genre.getGenreName())) {
                changes.append("название: ").append(genre.getGenreName()).append(" -> ").append(name);
                genre.setGenreName(name);
            }
            
            genreRepository.update(genre);
            
            if (!changes.isEmpty()) {
                logAction(admin, "UPDATE_GENRE", "Genre", genreId, "Обновлен жанр ID=" + genreId + ": " + changes);
            }
            
            return ResponseEntity.ok("Жанр обновлен");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка: " + e.getMessage());
        }
    }
}
