package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.PaymentTransaction;
import org.apelsin.musicstore.model.Order;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PaymentTransactionRepository {

    private final DataSource dataSource;
    private final OrderRepository orderRepository;

    public PaymentTransactionRepository(DataSource dataSource, OrderRepository orderRepository) {
        this.dataSource = dataSource;
        this.orderRepository = orderRepository;
    }

    private PaymentTransaction mapResultSetToPaymentTransaction(ResultSet rs) throws SQLException {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setTransactionId(rs.getLong("transaction_id"));
        transaction.setPaymentMethod(rs.getString("payment_method"));
        transaction.setTransactionStatus(rs.getString("transaction_status"));
        transaction.setExternalTransactionId(rs.getString("external_transaction_id"));
        
        Timestamp completionDate = rs.getTimestamp("transaction_completion_date");
        if (completionDate != null) {
            transaction.setTransactionCompletionDate(completionDate.toLocalDateTime());
        }

        Long orderId = rs.getLong("order_id");
        if (!rs.wasNull()) {
            orderRepository.findById(orderId).ifPresent(transaction::setTransactionOrder);
        }

        return transaction;
    }

    public PaymentTransaction save(PaymentTransaction transaction) {
        String sql = "INSERT INTO transactions (order_id, payment_method, transaction_status, external_transaction_id, transaction_completion_date) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, transaction.getTransactionOrder().getOrderId());
            ps.setString(2, transaction.getPaymentMethod());
            ps.setString(3, transaction.getTransactionStatus());
            ps.setString(4, transaction.getExternalTransactionId());
            
            if (transaction.getTransactionCompletionDate() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(transaction.getTransactionCompletionDate()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    transaction.setTransactionId(rs.getLong(1));
                }
            }
            return transaction;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save payment transaction", e);
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM transactions WHERE transaction_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete payment transaction", e);
        }
    }

    public Optional<PaymentTransaction> findById(Long id) {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPaymentTransaction(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find payment transaction by id", e);
        }
    }

    public List<PaymentTransaction> findAll() {
        String sql = "SELECT * FROM transactions";
        List<PaymentTransaction> transactions = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                transactions.add(mapResultSetToPaymentTransaction(rs));
            }
            return transactions;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all payment transactions", e);
        }
    }

    public Optional<PaymentTransaction> findByOrderId(Long orderId) {
        String sql = "SELECT * FROM transactions WHERE order_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPaymentTransaction(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find payment transaction by order id", e);
        }
    }

    public List<PaymentTransaction> findByStatus(String status) {
        String sql = "SELECT * FROM transactions WHERE transaction_status = ?";
        List<PaymentTransaction> transactions = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToPaymentTransaction(rs));
                }
            }
            return transactions;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find payment transactions by status", e);
        }
    }

    public Optional<PaymentTransaction> findByTransactionId(Long transactionId) {
        return findById(transactionId);
    }

    public PaymentTransaction update(PaymentTransaction transaction) {
        String sql = "UPDATE transactions SET order_id = ?, payment_method = ?, transaction_status = ?, external_transaction_id = ?, transaction_completion_date = ? WHERE transaction_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, transaction.getTransactionOrder().getOrderId());
            ps.setString(2, transaction.getPaymentMethod());
            ps.setString(3, transaction.getTransactionStatus());
            ps.setString(4, transaction.getExternalTransactionId());

            if (transaction.getTransactionCompletionDate() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(transaction.getTransactionCompletionDate()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }

            ps.setLong(6, transaction.getTransactionId());
            ps.executeUpdate();
            return transaction;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update payment transaction", e);
        }
    }
}
