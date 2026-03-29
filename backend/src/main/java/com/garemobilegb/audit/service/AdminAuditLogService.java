package com.garemobilegb.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garemobilegb.audit.domain.AdminAuditLogEntry;
import com.garemobilegb.audit.repository.AdminAuditLogRepository;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuditLogService {

  private static final Logger log = LoggerFactory.getLogger(AdminAuditLogService.class);

  private final AdminAuditLogRepository adminAuditLogRepository;
  private final ObjectMapper objectMapper;

  public AdminAuditLogService(
      AdminAuditLogRepository adminAuditLogRepository, ObjectMapper objectMapper) {
    this.adminAuditLogRepository = adminAuditLogRepository;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public void record(
      long actorUserId, String action, String entityType, Long entityId, Map<String, Object> details) {
    String json = null;
    if (details != null && !details.isEmpty()) {
      try {
        json = objectMapper.writeValueAsString(details);
      } catch (JsonProcessingException e) {
        log.warn("Audit JSON serialization failed for action={}", action, e);
        json = "{\"error\":\"serialization_failed\"}";
      }
    }
    adminAuditLogRepository.save(new AdminAuditLogEntry(actorUserId, action, entityType, entityId, json));
  }
}
