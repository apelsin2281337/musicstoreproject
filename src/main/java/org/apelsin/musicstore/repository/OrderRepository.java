package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.Order;
import org.apelsin.musicstore.model.Track;
import org.apelsin.musicstore.model.User;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {

    private final DataSource dataSource;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;

    public OrderRepository(DataSource dataSource, UserRepository userRepository, TrackRepository trackRepository) {
        this.dataSource = dataSource;
        this.userRepository = userRepository;
        this.trackRepository = trackRepository;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getLong("order_id"));
        order.setOrderTotalAmount(rs.getDouble("order_total_amount"));
        order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());

        Long userId = rs.getLong("user_id");
        userRepository.findById(userId).ifPresent(order::setOrderUser);

        order.setOrderItems(findOrderItemsByOrderId(order.getOrderId()));

        return order;
    }

    private List<Track> findOrderItemsByOrderId(Long orderId) {
        String sql = "SELECT t.* FROM tracks t JOIN orders_order_items oi ON t.track_id = oi.order_items_track_id WHERE oi.order_order_id = ?";
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Track track = new Track();
                    track.setTrackId(rs.getLong("track_id"));
                    track.setTrackTitle(rs.getString("track_title"));
                    track.setTrackPrice(rs.getDouble("track_price"));
                    track.setTrackFilePath(rs.getString("track_file_path"));
                    track.setTrackDownloadCount(rs.getLong("track_download_count"));
                    tracks.add(track);
                }
            }
            return tracks;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find order items", e);
        }
    }

    public Order save(Order order) {
        String sql = "INSERT INTO orders (user_id, order_total_amount, order_date) VALUES (?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, order.getOrderUser().getUserId());
            ps.setObject(2, order.getOrderTotalAmount(), Types.DOUBLE);
            ps.setTimestamp(3, Timestamp.valueOf(order.getOrderDate()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    order.setOrderId(rs.getLong(1));
                }
            }
            
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                saveOrderItems(conn, order.getOrderId(), order.getOrderItems());
            }
            
            return order;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save order", e);
        }
    }

    private void saveOrderItems(Connection conn, Long orderId, List<Track> items) throws SQLException {
        String sql = "INSERT INTO orders_order_items (order_order_id, order_items_track_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Track track : items) {
                ps.setLong(1, orderId);
                ps.setLong(2, track.getTrackId());
                ps.executeUpdate();
            }
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM orders WHERE order_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete order", e);
        }
    }

    public Optional<Order> findById(Long id) {
        String sql = "SELECT * FROM orders WHERE order_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToOrder(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find order by id", e);
        }
    }

    public List<Order> findAll() {
        String sql = "SELECT * FROM orders";
        List<Order> orders = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
            return orders;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all orders", e);
        }
    }

    public List<Order> findByOrderUser_UserId(Long userId) {
        String sql = "SELECT * FROM orders WHERE user_id = ?";
        List<Order> orders = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
            return orders;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find orders by user id", e);
        }
    }

    public Optional<Order> findByOrderId(Long orderId) {
        return findById(orderId);
    }

    public List<Order> findAllByOrderByOrderDateDesc() {
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";
        List<Order> orders = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
            return orders;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find orders by date", e);
        }
    }

    public Order update(Order order) {
        String sql = "UPDATE orders SET user_id = ?, order_total_amount = ?, order_date = ? WHERE order_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, order.getOrderUser().getUserId());
            ps.setObject(2, order.getOrderTotalAmount(), Types.DOUBLE);
            ps.setTimestamp(3, Timestamp.valueOf(order.getOrderDate()));
            ps.setLong(4, order.getOrderId());

            ps.executeUpdate();

            updateOrderItems(conn, order.getOrderId(), order.getOrderItems());

            return order;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update order", e);
        }
    }

    private void updateOrderItems(Connection conn, Long orderId, List<Track> items) throws SQLException {
        deleteOrderItems(conn, orderId);
        saveOrderItems(conn, orderId, items);
    }

    private void deleteOrderItems(Connection conn, Long orderId) throws SQLException {
        String sql = "DELETE FROM orders_order_items WHERE order_order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.executeUpdate();
        }
    }
}
