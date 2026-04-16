package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.AdminActionLog;
import org.apelsin.musicstore.model.User;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AdminActionLogRepository {

    private final DataSource dataSource;
    private final UserRepository userRepository;

    public AdminActionLogRepository(DataSource dataSource, UserRepository userRepository) {
        this.dataSource = dataSource;
        this.userRepository = userRepository;
    }

    private AdminActionLog mapResultSetToAdminActionLog(ResultSet rs) throws SQLException {
        AdminActionLog log = new AdminActionLog();
        log.setLogId(rs.getLong("log_id"));
        log.setActionType(rs.getString("action_type"));
        log.setTargetEntity(rs.getString("target_entity"));
        log.setTargetId(rs.getLong("target_id"));
        log.setActionTimestamp(rs.getTimestamp("action_timestamp").toLocalDateTime());
        log.setActionDetails(rs.getString("action_details"));

        Long adminId = rs.getLong("admin_id");
        userRepository.findById(adminId).ifPresent(log::setAdmin);

        return log;
    }

    public AdminActionLog save(AdminActionLog log) {
        String sql = "INSERT INTO admin_action_logs (admin_id, action_type, target_entity, target_id, action_timestamp, action_details) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, log.getAdmin().getUserId());
            ps.setString(2, log.getActionType());
            ps.setString(3, log.getTargetEntity());
            ps.setLong(4, log.getTargetId());
            ps.setTimestamp(5, Timestamp.valueOf(log.getActionTimestamp()));
            ps.setString(6, log.getActionDetails());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    log.setLogId(rs.getLong(1));
                }
            }
            return log;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save admin action log", e);
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM admin_action_logs WHERE log_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete admin action log", e);
        }
    }

    public Optional<AdminActionLog> findById(Long id) {
        String sql = "SELECT * FROM admin_action_logs WHERE log_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAdminActionLog(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find admin action log by id", e);
        }
    }

    public List<AdminActionLog> findAll() {
        String sql = "SELECT * FROM admin_action_logs";
        List<AdminActionLog> logs = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                logs.add(mapResultSetToAdminActionLog(rs));
            }
            return logs;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all admin action logs", e);
        }
    }

    public List<AdminActionLog> findByAdmin_UserId(Long userId) {
        String sql = "SELECT * FROM admin_action_logs WHERE admin_id = ?";
        List<AdminActionLog> logs = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToAdminActionLog(rs));
                }
            }
            return logs;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find admin action logs by user id", e);
        }
    }

    public List<AdminActionLog> findByTargetEntityAndTargetId(String targetEntity, Long targetId) {
        String sql = "SELECT * FROM admin_action_logs WHERE target_entity = ? AND target_id = ?";
        List<AdminActionLog> logs = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, targetEntity);
            ps.setLong(2, targetId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToAdminActionLog(rs));
                }
            }
            return logs;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find admin action logs by target", e);
        }
    }

    public Optional<AdminActionLog> findByLogId(Long logId) {
        return findById(logId);
    }

    public List<AdminActionLog> findAllByOrderByActionTimestampDesc() {
        String sql = "SELECT * FROM admin_action_logs ORDER BY action_timestamp DESC";
        List<AdminActionLog> logs = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                logs.add(mapResultSetToAdminActionLog(rs));
            }
            return logs;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find admin action logs by timestamp", e);
        }
    }
}
