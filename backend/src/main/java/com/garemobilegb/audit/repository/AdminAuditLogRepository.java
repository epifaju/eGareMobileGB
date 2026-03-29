package com.garemobilegb.audit.repository;

import com.garemobilegb.audit.domain.AdminAuditLogEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLogEntry, Long> {

  Page<AdminAuditLogEntry> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
