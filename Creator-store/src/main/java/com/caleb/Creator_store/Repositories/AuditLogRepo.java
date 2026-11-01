package com.caleb.Creator_store.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caleb.Creator_store.Entities.AuditLog;

// Provides append and administrator-read persistence for the marketplace audit trail.
public interface AuditLogRepo extends JpaRepository<AuditLog, Long> {

    // Returns newest audit records first for administrative investigation.
    List<AuditLog> findAllByOrderByCreatedAtDesc();
}
