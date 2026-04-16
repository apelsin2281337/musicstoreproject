package org.apelsin.musicstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "albums")
@Data
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long albumId;

    private String albumTitle;
    private Integer albumReleaseYear;
    private Double albumPrice;

    private Long albumRating = 0L;

    @ManyToOne
    @JoinColumn(name = "artist_id")
    @JsonIgnoreProperties({"artistAlbums", "artistDescription", "artistTracks", "artistRating"})
    private Artist albumArtist;

    @OneToMany(mappedBy = "trackAlbum", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"trackAlbum", "trackArtist", "trackGenres", "trackPrice", "trackFilePath", "trackDownloadCount", "trackUploadedBy"})
    private List<Track> albumTracks;
}