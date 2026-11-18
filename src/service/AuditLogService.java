package service;

import dao.AuditLogDAO;
import model.AdminAuditEvent;

import java.util.List;

/**
 * Service orchestrating administrative audit logging and retrieval.
 */
public class AuditLogService {
    private final AuditLogDAO auditLogDAO;

    public AuditLogService() {
        this.auditLogDAO = new AuditLogDAO();
    }

    public AuditLogService(AuditLogDAO dao) {
        this.auditLogDAO = dao;
    }

    public void log(int actorUserId, String actorEmail, String actionType,
                    String targetType, String targetId, String outcome, String description) {
        try {
            AdminAuditEvent event = new AdminAuditEvent(
                actorUserId, actorEmail, actionType, targetType, targetId, outcome, description
            );
            auditLogDAO.insert(event);
        } catch (Exception e) {
            System.err.println("Failed to write audit log: " + e.getMessage());
        }
    }

    public List<AdminAuditEvent> getRecentLogs(int limit, String actionFilter, String search) {
        return auditLogDAO.findRecent(limit, actionFilter, search);
    }

    public int getTotalCount() {
        return auditLogDAO.countTotalLogs();
    }
}
