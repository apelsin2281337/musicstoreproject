package org.apelsin.musicstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"userPassword", "userRole", "userPurchasedTracks", "userPurchasedAlbums", "userPlaylists", "userUploadedTracks"})
    private User reviewUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id")
    @JsonIgnoreProperties({"trackArtist", "trackAlbum", "trackGenres", "trackUploadedBy", "trackFilePath"})
    private Track reviewTrack;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    @JsonIgnoreProperties({"albumArtist", "albumTracks"})
    private Album reviewAlbum;

    private Integer reviewRating; 

    @Column(columnDefinition = "TEXT")
    private String reviewComment;

    private LocalDateTime reviewDate = LocalDateTime.now();
}