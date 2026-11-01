package com.caleb.Creator_store.Controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caleb.Creator_store.Services.AuditLogService;
import com.caleb.Creator_store.dto.AuditLogResponse;

import lombok.RequiredArgsConstructor;

// Exposes the audit trail only to administrators.
// Audit records can reveal operational details and must not be public or seller-visible.
@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditController {

    private final AuditLogService auditLogService;

    // Returns newest audit events first for administrator investigation.
    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public List<AuditLogResponse> getAuditLogs() {
        return auditLogService.getAll();
    }
}
