package com.example.MicroInvestApp.exception.user;

public class AuditLogNotFoundException extends RuntimeException {
    public AuditLogNotFoundException(String message) {
        super(message);
    }
    public AuditLogNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuditLogNotFoundException(Long auditLogId) {
        super("Audit log not found with ID: " + auditLogId);
    }
}
