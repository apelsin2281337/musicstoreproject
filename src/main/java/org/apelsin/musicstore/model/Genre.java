package org.apelsin.musicstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "genres")
@Data
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long genreId;

    @Column(unique = true)
    private String genreName;

    @ManyToMany(mappedBy = "trackGenres")
    @JsonIgnoreProperties({"trackGenres", "trackAlbum", "trackArtist", "trackUploadedBy"})
    private List<Track> genreTracks;
}