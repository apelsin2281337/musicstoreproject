package org.apelsin.musicstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_action_logs")
@Data
public class AdminActionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "admin_id")
    @JsonIgnoreProperties({"userPassword", "userRole", "userPurchasedTracks", "userPurchasedAlbums"})
    private User admin; 

    private String actionType; 

    private String targetEntity; 

    private Long targetId; 

    private LocalDateTime actionTimestamp = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String actionDetails; 
}