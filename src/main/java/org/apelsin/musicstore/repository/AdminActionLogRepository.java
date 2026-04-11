package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.AdminActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {
    // Посмотреть историю действий конкретного админа
    List<AdminActionLog> findByAdmin_UserId(Long userId);

    // Найти, кто совершал действия с конкретным треком
    List<AdminActionLog> findByTargetEntityAndTargetId(String targetEntity, Long targetId);
}
