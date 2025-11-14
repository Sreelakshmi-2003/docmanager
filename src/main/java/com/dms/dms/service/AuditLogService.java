package com.dms.dms.service;

import com.dms.dms.entity.AuditLog;
import com.dms.dms.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Save an audit log entry.
     * Safe method: catches and logs all exceptions internally to prevent breaking business logic.
     */
    public void log(String userId,
                    String actionType,
                    String entityType,
                    String entityId,
                    String description,
                    String ipAddress,
                    String userAgent) {

        try {
            AuditLog logEntry = AuditLog.builder()
                    .userId(userId)
                    .actionType(actionType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            auditLogRepository.save(logEntry);

            log.info("Audit log saved: [{}] {} on {} (Entity ID: {})",
                    actionType, userId, entityType, entityId);

        } catch (DataAccessException dae) {
            log.error("Database error while saving audit log for user {}: {}", userId, dae.getMessage(), dae);
        } catch (Exception e) {
            log.error("Unexpected error while saving audit log: {}", e.getMessage(), e);
        }
    }

    /**
     * Fetch all logs for a specific file, ordered by latest first.
     * Returns empty list on failure.
     */
    public List<AuditLog> getLogsByFileId(String fileId) {
        try {
            return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc("FILE", fileId);
        } catch (DataAccessException dae) {
            log.error("Database error while fetching audit logs for file {}: {}", fileId, dae.getMessage(), dae);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unexpected error while fetching audit logs for file {}: {}", fileId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
