package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = "SELECT * FROM orders WHERE user_id = :userId", nativeQuery = true)
    List<Order> findByOrderUser_UserId(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM orders WHERE order_id = :orderId", nativeQuery = true)
    Optional<Order> findByOrderId(@Param("orderId") Long orderId);

    @Query(value = "SELECT * FROM orders ORDER BY order_date DESC", nativeQuery = true)
    List<Order> findAllByOrderByOrderDateDesc();
}
