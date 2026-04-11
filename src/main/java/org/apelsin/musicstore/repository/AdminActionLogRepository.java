package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.AdminActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {
    @Query(value = "SELECT * FROM admin_action_logs WHERE admin_id = :userId", nativeQuery = true)
    List<AdminActionLog> findByAdmin_UserId(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM admin_action_logs WHERE target_entity = :targetEntity AND target_id = :targetId", nativeQuery = true)
    List<AdminActionLog> findByTargetEntityAndTargetId(@Param("targetEntity") String targetEntity, @Param("targetId") Long targetId);

    @Query(value = "SELECT * FROM admin_action_logs WHERE log_id = :logId", nativeQuery = true)
    Optional<AdminActionLog> findByLogId(@Param("logId") Long logId);

    @Query(value = "SELECT * FROM admin_action_logs ORDER BY action_timestamp DESC", nativeQuery = true)
    List<AdminActionLog> findAllByOrderByActionTimestampDesc();
}
