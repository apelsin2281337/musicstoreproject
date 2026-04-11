package org.apelsin.musicstore.controller;

import lombok.RequiredArgsConstructor;
import org.apelsin.musicstore.model.*;
import org.apelsin.musicstore.repository.*;
import org.apelsin.musicstore.service.UserService;
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

    private final UserService userService;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;

    // Добавьте эти финальные поля, чтобы Lombok их внедрил
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final AdminActionLogRepository actionLogRepository;

    @Value("${upload.path}")
    private String uploadPath;

    @PostMapping("/promote/{userId}")
    public ResponseEntity<?> promote(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (user.getUserRole() == Role.ADMIN){
            return ResponseEntity.badRequest().body("Пользователь уже является администратором");
        }
        return ResponseEntity.ok(userService.changeRole(userId, Role.ADMIN));
    }

    @PostMapping("/demote/{userId}")
    public ResponseEntity<?> demote(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (user.getUserRole() == Role.USER){
            return ResponseEntity.badRequest().body("Пользователь уже является обычным пользователем");
        }
        return ResponseEntity.ok(userService.changeRole(userId, Role.USER));
    }


    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/addartist")
    public ResponseEntity<?> addArtist(
            @RequestParam("name") String name,
            @RequestParam("description") String desc,
            @RequestParam("rating") Long rating
    ){
        try {
            if (name == null || name.isEmpty()) {
                return ResponseEntity.badRequest().body("Имя артиста не может быть пустым");
            }


            Artist artist = new Artist();
            artist.setArtistName(name);
            artist.setArtistDescription(desc);
            artist.setArtistRating(rating != null ? rating : 0L);

            Artist savedArtist = artistRepository.save(artist);


            AdminActionLog log = new AdminActionLog();
            log.setActionType("ADD_ARTIST");
            log.setTargetEntity("ARTIST");
            log.setTargetId(savedArtist.getArtistId());
            log.setActionDetails("Добавлен новый артист: " + name);
            actionLogRepository.save(log);


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
            @RequestParam("artistId") Long artistId
    ) {
        try {
            // 1. Ищем артиста, которому принадлежит альбом
            Artist artist = artistRepository.findById(artistId)
                    .orElseThrow(() -> new RuntimeException("Артист с таким ID не найден"));

            // 2. Создаем объект альбома
            Album album = new Album();
            album.setAlbumTitle(title);
            album.setAlbumReleaseYear(year);
            album.setAlbumPrice(price);
            album.setAlbumArtist(artist); // Устанавливаем связь "Многие-к-Одному"
            album.setAlbumRating(0L);

            // 3. Сохраняем
            Album savedAlbum = albumRepository.save(album);

            return ResponseEntity.ok("Альбом '" + title + "' успешно создан! ID: " + savedAlbum.getAlbumId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Ошибка при создании альбома: " + e.getMessage());
        }
    }

    @PostMapping("/publishtrack")
    public ResponseEntity<?> publishTrack(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("price") Double price,
            @RequestParam("artist") String artistname,
            @RequestParam("album") String albumtitle,
            @RequestParam("admin") String adminname) {

        try {
            Path root = Paths.get(uploadPath);
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            // 1. Ищем артиста по имени
            Artist artist = artistRepository.findByArtistName(artistname)
                    .orElseThrow(() -> new RuntimeException("Артист '" + artistname + "' не найден"));

            // 2. Ищем альбом именно у этого артиста (чтобы не путать дубликаты)
            Album album = albumRepository.findByAlbumTitleAndAlbumArtist(albumtitle, artist)
                    .orElseThrow(() -> new RuntimeException("Альбом '" + albumtitle + "' у артиста " + artistname + " не найден"));

            // 3. Ищем админа по юзернейму
            User admin = userRepository.findByUserUsername(adminname)
                    .orElseThrow(() -> new RuntimeException("Администратор '" + adminname + "' не найден"));

            // 4. Сохраняем файл
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), root.resolve(filename));

            // 5. Создаем трек
            Track track = new Track();
            track.setTrackTitle(title);
            track.setTrackPrice(price);
            track.setTrackArtist(artist);
            track.setTrackAlbum(album);
            track.setTrackUploadedBy(admin);
            track.setTrackFilePath("/uploads/music/" + filename);

            trackRepository.save(track);

            return ResponseEntity.ok("Трек успешно загружен и опубликован!");

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при сохранении файла: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Ошибка: " + e.getMessage());
        }
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

            User admin = userRepository.findById(adminId)
                    .orElseThrow(() -> new RuntimeException("Админ не найден"));

            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), root.resolve(filename));

            Track track = new Track();
            track.setTrackTitle(title);
            track.setTrackPrice(price);
            track.setTrackArtist(artist);
            track.setTrackAlbum(album);
            track.setTrackUploadedBy(admin);
            track.setTrackFilePath("/uploads/music/" + filename);

            Track saved = trackRepository.save(track);

            return ResponseEntity.ok("Трек загружен! ID: " + saved.getTrackId());

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при сохранении файла: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Ошибка: " + e.getMessage());
        }
    }
}