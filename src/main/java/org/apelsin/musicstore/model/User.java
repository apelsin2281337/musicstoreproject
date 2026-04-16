package org.apelsin.musicstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String userUsername;

    @JsonIgnoreProperties
    private String userPassword;

    @Enumerated(EnumType.STRING)
    private Role userRole;

    @ManyToMany
    @JoinTable(
            name = "user_purchased_tracks",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "track_id")
    )
    @JsonIgnoreProperties({"trackArtist", "trackAlbum", "trackGenres", "trackUploadedBy", "trackFilePath", "trackPrice", "trackDownloadCount"})
    private List<Track> userPurchasedTracks;

    @ManyToMany
    @JoinTable(
            name = "user_purchased_albums",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "album_id")
    )
    @JsonIgnoreProperties({"albumArtist", "albumTracks", "albumPrice", "albumRating", "albumReleaseYear"})
    private List<Album> userPurchasedAlbums;

    @OneToMany(mappedBy = "trackUploadedBy")
    @JsonIgnoreProperties({"trackArtist", "trackAlbum", "trackGenres", "trackUploadedBy", "trackFilePath", "trackPrice", "trackDownloadCount"})
    private List<Track> userUploadedTracks;

    @OneToMany(mappedBy = "playlistOwner")
    @JsonIgnoreProperties({"playlistOwner", "playlistTracks"})
    private List<Playlist> userPlaylists;
}