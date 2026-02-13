package com.campus.issue_tracker.service;

import com.campus.issue_tracker.entity.AuditLog;
import com.campus.issue_tracker.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void logEvent(String eventType, Long relatedId, String relatedType, String username, String details) {
        AuditLog log = new AuditLog();
        log.setEventType(eventType);
        log.setRelatedId(relatedId);
        log.setRelatedType(relatedType);
        log.setUsername(username);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}
