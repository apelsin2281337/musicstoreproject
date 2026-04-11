package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    @Query(value = "SELECT * FROM transactions WHERE order_id = :orderId", nativeQuery = true)
    Optional<PaymentTransaction> findByOrderId(@Param("orderId") Long orderId);

    @Query(value = "SELECT * FROM transactions WHERE transaction_status = :status", nativeQuery = true)
    List<PaymentTransaction> findByStatus(@Param("status") String status);

    @Query(value = "SELECT * FROM transactions WHERE transaction_id = :transactionId", nativeQuery = true)
    Optional<PaymentTransaction> findByTransactionId(@Param("transactionId") Long transactionId);
}
