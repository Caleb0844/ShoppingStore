package com.caleb.Creator_store.Services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.caleb.Creator_store.Entities.AuditLog;
import com.caleb.Creator_store.Enums.AuditAction;
import com.caleb.Creator_store.Repositories.AuditLogRepo;
import com.caleb.Creator_store.dto.AuditLogResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

// Records important state-changing actions and maps audit data for administrators.
// Keep this service free of secrets and request-body persistence so the audit trail is safe to retain.
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepo auditLogRepo;

    // Saves one compact audit event inside the caller's transaction.
    @Transactional
    public void record(
            Long actorUserId,
            AuditAction action,
            String resourceType,
            Long resourceId,
            String details) {
        auditLogRepo.save(AuditLog.builder()
                .actorUserId(actorUserId)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .details(details)
                .build());
    }

    // Returns audit records for restricted administrator investigation.
    public List<AuditLogResponse> getAll() {
        return auditLogRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(log -> AuditLogResponse.builder()
                        .id(log.getId())
                        .actorUserId(log.getActorUserId())
                        .action(log.getAction())
                        .resourceType(log.getResourceType())
                        .resourceId(log.getResourceId())
                        .details(log.getDetails())
                        .createdAt(log.getCreatedAt())
                        .build())
                .toList();
    }
}
