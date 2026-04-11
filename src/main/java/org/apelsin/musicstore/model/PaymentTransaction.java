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

    private String paymentMethod; // CARD, PAYPAL, CRYPTO

    private String transactionStatus; // SUCCESS, FAILED, PENDING

    private String externalTransactionId; // ID из платежной системы

    private LocalDateTime transactionCompletionDate;
}