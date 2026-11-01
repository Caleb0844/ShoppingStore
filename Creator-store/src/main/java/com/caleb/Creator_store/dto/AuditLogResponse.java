package com.caleb.Creator_store.dto;

import java.time.LocalDateTime;

import com.caleb.Creator_store.Enums.AuditAction;

import lombok.Builder;
import lombok.Value;

// Defines the safe administrator-facing audit representation.
@Value
@Builder
public class AuditLogResponse {

    Long id;
    Long actorUserId;
    AuditAction action;
    String resourceType;
    Long resourceId;
    String details;
    LocalDateTime createdAt;
}
