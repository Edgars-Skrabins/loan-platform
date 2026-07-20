package io.github.edgarsskrabins.loan_platform.audit.repository;

import io.github.edgarsskrabins.loan_platform.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserId(Long userid);

}
