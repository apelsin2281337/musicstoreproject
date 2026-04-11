package org.apelsin.musicstore.model;

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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User reviewUser;

    @ManyToOne
    @JoinColumn(name = "track_id")
    private Track reviewTrack;

    @ManyToOne
    @JoinColumn(name = "album_id")
    private Album reviewAlbum;

    private Integer reviewRating; // Оценка от 1 до 5

    @Column(columnDefinition = "TEXT")
    private String reviewComment;

    private LocalDateTime reviewDate = LocalDateTime.now();
}