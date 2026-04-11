package org.apelsin.musicstore.model;

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

    private String userPassword;

    @Enumerated(EnumType.STRING)
    private Role userRole;

    @ManyToMany
    @JoinTable(
            name = "user_purchased_tracks",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "track_id")
    )
    private List<Track> userPurchasedTracks;

    @ManyToMany
    @JoinTable(
            name = "user_purchased_albums",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "album_id")
    )
    private List<Album> userPurchasedAlbums;

    @OneToMany(mappedBy = "trackUploadedBy")
    private List<Track> userUploadedTracks;

    @OneToMany(mappedBy = "playlistOwner")
    private List<Playlist> userPlaylists;
}