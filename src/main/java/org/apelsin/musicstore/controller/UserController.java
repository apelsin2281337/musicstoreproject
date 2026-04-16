package org.apelsin.musicstore.controller;

import lombok.RequiredArgsConstructor;
import org.apelsin.musicstore.model.Playlist;
import org.apelsin.musicstore.model.Track;
import org.apelsin.musicstore.model.User;
import org.apelsin.musicstore.repository.UserRepository;
import org.apelsin.musicstore.service.PlaylistService;
import org.apelsin.musicstore.service.UserService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PlaylistService playlistService;
    
    @org.springframework.beans.factory.annotation.Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserInfo(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/{userId}/library")
    public List<Track> getMyLibrary(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(User::getUserPurchasedTracks)
                .orElseThrow();
    }

    @PostMapping("/{userId}/buy")
    public ResponseEntity<String> purchase(@PathVariable Long userId, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked") //annoying bitch
        List<Long> trackIds = ((List<Integer>) body.get("trackIds")).stream().map(Integer::longValue).toList();
        String paymentMethod = (String) body.getOrDefault("paymentMethod", "CARD");
        userService.buyTracks(userId, trackIds, paymentMethod);
        return ResponseEntity.ok("Покупка успешно завершена!");
    }

    @PostMapping("/{userId}/buy-album/{albumId}")
    public ResponseEntity<String> purchaseAlbum(@PathVariable Long userId, @PathVariable Long albumId, 
            @RequestParam(defaultValue = "CARD") String paymentMethod) {
        userService.buyAlbum(userId, albumId, paymentMethod);
        return ResponseEntity.ok("Альбом успешно куплен!");
    }

    @GetMapping("/{userId}/orders")
    public List<?> getUserOrders(@PathVariable Long userId) {
        return userService.getUserOrders(userId);
    }

    @GetMapping("/recommendations/{trackId}")
    public List<Track> getSimilar(@PathVariable Long trackId) {
        return userService.getRecommendations(trackId);
    }

    @PostMapping("/download/{trackId}")
    public Track prepareDownload(@PathVariable Long trackId) {
        return userService.downloadTrack(trackId);
    }

    @GetMapping("/download/{trackId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long trackId) {
        Track track = userService.downloadTrack(trackId);
        String filename = track.getTrackFilePath().replace("/uploads/music/", "");
        File file = Paths.get(uploadPath, filename).toFile();
        
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        Resource resource = new FileSystemResource(file);
        String title = track.getTrackTitle();
        String safeTitle = "track_" + trackId;
        if (title != null && !title.trim().isEmpty()) {
            safeTitle = title.trim().replaceAll("\\s+", "_");
        }
        String downloadName = safeTitle + ".mp3";
        String encodedName = URLEncoder.encode(downloadName, StandardCharsets.UTF_8).replace("+", "%20");
        String disposition = "attachment; filename*=UTF-8''" + encodedName;
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(resource);
    }

    @GetMapping("/{userId}/playlists")
    public List<Playlist> getUserPlaylists(@PathVariable Long userId) {
        return playlistService.getUserPlaylists(userId);
    }

    @GetMapping("/playlists/{playlistId}")
    public ResponseEntity<Playlist> getPlaylist(@PathVariable Long playlistId) {
        return ResponseEntity.ok(playlistService.getPlaylistById(playlistId));
    }

    @PostMapping("/{userId}/playlists")
    public Playlist createPlaylist(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        return playlistService.createPlaylist(userId, body.get("title"));
    }

    @PutMapping("/playlists/{playlistId}")
    public Playlist updatePlaylist(@PathVariable Long playlistId, @RequestBody Map<String, String> body) {
        return playlistService.updatePlaylist(playlistId, body.get("title"));
    }

    @PostMapping("/playlists/{playlistId}/tracks/{trackId}")
    public Playlist addTrack(@PathVariable Long playlistId, @PathVariable Long trackId) {
        return playlistService.addTrackToPlaylist(playlistId, trackId);
    }

    @DeleteMapping("/playlists/{playlistId}/tracks/{trackId}")
    public ResponseEntity<String> removeTrack(@PathVariable Long playlistId, @PathVariable Long trackId) {
        playlistService.removeTrackFromPlaylist(playlistId, trackId);
        return ResponseEntity.ok("Track removed from playlist");
    }

    @DeleteMapping("/playlists/{playlistId}")
    public ResponseEntity<String> deletePlaylist(@PathVariable Long playlistId) {
        playlistService.deletePlaylist(playlistId);
        return ResponseEntity.ok("Playlist deleted");
    }
}
