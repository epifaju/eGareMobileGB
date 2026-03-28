package com.garemobilegb.booking.service;

import com.garemobilegb.booking.domain.RefundAuditEventType;
import com.garemobilegb.booking.domain.RefundAuditLog;
import com.garemobilegb.booking.repository.RefundAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefundAuditService {

  private final RefundAuditLogRepository repository;

  public RefundAuditService(RefundAuditLogRepository repository) {
    this.repository = repository;
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void append(long bookingId, long userId, RefundAuditEventType type, String detail) {
    repository.save(new RefundAuditLog(bookingId, userId, type, truncate(detail)));
  }

  private static String truncate(String s) {
    if (s == null) {
      return "";
    }
    return s.length() <= 4000 ? s : s.substring(0, 3997) + "...";
  }
}
