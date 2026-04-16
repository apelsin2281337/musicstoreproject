package org.apelsin.musicstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "playlists")
@Data
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long playlistId;

    private String playlistTitle;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"userPurchasedTracks", "userPurchasedAlbums", "userPlaylists", "userUploadedTracks", "userPassword", "userRole"})
    private User playlistOwner;

    @ManyToMany
    @JoinTable(
            name = "playlist_tracks",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "track_id")
    )
    @JsonIgnoreProperties({"trackArtist", "trackAlbum", "trackGenres", "trackUploadedBy", "trackFilePath", "trackPrice", "trackDownloadCount"})
    private List<Track> playlistTracks;
}