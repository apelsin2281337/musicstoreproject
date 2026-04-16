package org.apelsin.musicstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "artists")
@Data
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long artistId;

    @Column(nullable = false)
    private String artistName;

    @Column(columnDefinition = "TEXT")
    private String artistDescription;

    private Long artistRating = 0L;

    @OneToMany(mappedBy = "albumArtist", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"albumArtist", "albumTracks"})
    private List<Album> artistAlbums;
}