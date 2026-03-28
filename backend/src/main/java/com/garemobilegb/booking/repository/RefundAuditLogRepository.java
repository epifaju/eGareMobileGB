package com.garemobilegb.booking.repository;

import com.garemobilegb.booking.domain.RefundAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundAuditLogRepository extends JpaRepository<RefundAuditLog, Long> {}
