package org.apelsin.musicstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "tracks")
@Data
public class Track {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trackId;

    @Column(nullable = false)
    private String trackTitle;

    private Double trackPrice;

    private String trackFilePath;

    private Long trackDownloadCount = 0L;

    @ManyToOne
    @JoinColumn(name = "artist_id")
    @JsonIgnoreProperties({"artistAlbums", "artistDescription", "artistTracks"})
    private Artist trackArtist;

    @ManyToOne
    @JoinColumn(name = "album_id")
    @JsonIgnoreProperties({"albumTracks", "albumArtist", "albumPrice", "albumRating", "albumReleaseYear"})
    private Album trackAlbum;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    @JsonIgnoreProperties({"userPurchasedTracks", "userPurchasedAlbums", "userPlaylists", "userUploadedTracks", "userPassword", "userRole", "userUsername"})
    private User trackUploadedBy;

    @ManyToMany
    @JoinTable(
            name = "trackGenres",
            joinColumns = @JoinColumn(name = "track_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @JsonIgnoreProperties("genreTracks")
    private List<Genre> trackGenres;
}