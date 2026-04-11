package org.apelsin.musicstore.model;

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

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin; // Администратор, совершивший действие

    private String actionType; // UPLOAD, DELETE, EDIT_INFO, EDIT_PRICE

    private String targetEntity; // Album, Track, Artist

    private Long targetId; // ID объекта, над которым совершено действие

    private LocalDateTime actionTimestamp = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String actionDetails; // Описание изменений
}