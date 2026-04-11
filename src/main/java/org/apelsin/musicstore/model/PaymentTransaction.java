package org.apelsin.musicstore.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order transactionOrder;

    private String paymentMethod; 

    private String transactionStatus; 

    private String externalTransactionId; 

    private LocalDateTime transactionCompletionDate;
}