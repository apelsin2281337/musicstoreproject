package org.apelsin.musicstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "licenses")
@Data
public class License {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long licenseId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "track_id")
    @JsonIgnoreProperties({"trackArtist", "trackAlbum", "trackGenres", "trackUploadedBy", "trackFilePath", "trackDownloadCount"})
    private Track licenseTrack;

    private String licenseContractNumber;
    private String licenseOwnerName;
    private LocalDate licenseStartDate;
    private LocalDate licenseExpirationDate;

    @Column(columnDefinition = "TEXT")
    private String licenseTerms;
}