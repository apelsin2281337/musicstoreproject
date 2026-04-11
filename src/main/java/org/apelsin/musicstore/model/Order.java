package org.apelsin.musicstore.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User orderUser;

    private Double orderTotalAmount;
    private LocalDateTime orderDate = LocalDateTime.now();

    @ManyToMany
    private List<Track> orderItems;
}