package org.apelsin.musicstore.model;

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
    private List<Track> genreTracks;
}