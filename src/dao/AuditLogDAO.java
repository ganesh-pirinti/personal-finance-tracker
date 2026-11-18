package dao;

import database.DatabaseConnection;
import model.AdminAuditEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data Access Object for append-only administrative and security audit events.
 * Provides immutable auditing of sensitive configuration, user role, and security operations.
 */
public class AuditLogDAO {
    private static final List<AdminAuditEvent> memoryLogs = new ArrayList<>();
    private static int memoryLogIdCounter = 1;

    public void insert(AdminAuditEvent event) throws SQLException {
        if (event == null) return;
        if (service.AuthenticationService.isDemoMode()) {
            synchronized (memoryLogs) {
                event.setEventId(memoryLogIdCounter++);
                memoryLogs.add(event);
            }
            return;
        }
        try {
            String sql = "INSERT INTO admin_audit_logs (actor_user_id, actor_email, action_type, " +
                         "target_type, target_id, outcome, description, timestamp) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, event.getActorUserId());
                ps.setString(2, event.getActorEmail());
                ps.setString(3, event.getActionType());
                ps.setString(4, event.getTargetType());
                ps.setString(5, event.getTargetId());
                ps.setString(6, event.getOutcome());
                ps.setString(7, event.getDescription());
                ps.setTimestamp(8, Timestamp.valueOf(event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now()));

                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        event.setEventId(keys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            // Offline fallback
            synchronized (memoryLogs) {
                event.setEventId(memoryLogIdCounter++);
                memoryLogs.add(event);
            }
            return;
        }

        synchronized (memoryLogs) {
            memoryLogs.add(event);
        }
    }

    public List<AdminAuditEvent> findRecent(int limit, String actionFilter, String search) {
        List<AdminAuditEvent> list = new ArrayList<>();
        if (service.AuthenticationService.isDemoMode()) {
            synchronized (memoryLogs) {
                List<AdminAuditEvent> reversed = new ArrayList<>(memoryLogs);
                Collections.reverse(reversed);
                for (AdminAuditEvent log : reversed) {
                    boolean matchAction = (actionFilter == null || actionFilter.equalsIgnoreCase("ALL") ||
                                          log.getActionType().equalsIgnoreCase(actionFilter));
                    boolean matchSearch = (search == null || search.trim().isEmpty() ||
                            log.getDescription().toLowerCase().contains(search.toLowerCase()) ||
                            log.getActorEmail().toLowerCase().contains(search.toLowerCase()) ||
                            (log.getTargetId() != null && log.getTargetId().toLowerCase().contains(search.toLowerCase())));
                    if (matchAction && matchSearch) {
                        list.add(log);
                        if (list.size() >= limit) break;
                    }
                }
            }
            return list;
        }
        try {
            StringBuilder sb = new StringBuilder(
                "SELECT event_id, actor_user_id, actor_email, action_type, target_type, " +
                "target_id, outcome, description, timestamp FROM admin_audit_logs WHERE 1=1 ");

            boolean hasAction = actionFilter != null && !actionFilter.equalsIgnoreCase("ALL");
            boolean hasSearch = search != null && !search.trim().isEmpty();

            if (hasAction) sb.append("AND action_type = ? ");
            if (hasSearch) sb.append("AND (LOWER(actor_email) LIKE ? OR LOWER(description) LIKE ? OR LOWER(target_id) LIKE ?) ");
            sb.append("ORDER BY timestamp DESC LIMIT ?");

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sb.toString())) {
                int idx = 1;
                if (hasAction) {
                    ps.setString(idx++, actionFilter.toUpperCase());
                }
                if (hasSearch) {
                    String query = "%" + search.trim().toLowerCase() + "%";
                    ps.setString(idx++, query);
                    ps.setString(idx++, query);
                    ps.setString(idx++, query);
                }
                ps.setInt(idx, limit > 0 ? limit : 100);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    return list;
                }
            }
        } catch (SQLException e) {
            // Offline in-memory filter
            synchronized (memoryLogs) {
                List<AdminAuditEvent> reversed = new ArrayList<>(memoryLogs);
                Collections.reverse(reversed);
                for (AdminAuditEvent log : reversed) {
                    boolean matchAction = (actionFilter == null || actionFilter.equalsIgnoreCase("ALL") ||
                                          log.getActionType().equalsIgnoreCase(actionFilter));
                    boolean matchSearch = (search == null || search.trim().isEmpty() ||
                            log.getDescription().toLowerCase().contains(search.toLowerCase()) ||
                            log.getActorEmail().toLowerCase().contains(search.toLowerCase()) ||
                            (log.getTargetId() != null && log.getTargetId().toLowerCase().contains(search.toLowerCase())));

                    if (matchAction && matchSearch) {
                        list.add(log);
                        if (list.size() >= limit) break;
                    }
                }
            }
        }
        return list;
    }

    public int countTotalLogs() {
        try {
            String sql = "SELECT COUNT(*) FROM admin_audit_logs";
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            synchronized (memoryLogs) {
                return memoryLogs.size();
            }
        }
        return 0;
    }

    private AdminAuditEvent mapRow(ResultSet rs) throws SQLException {
        AdminAuditEvent event = new AdminAuditEvent();
        event.setEventId(rs.getInt("event_id"));
        event.setActorUserId(rs.getInt("actor_user_id"));
        event.setActorEmail(rs.getString("actor_email"));
        event.setActionType(rs.getString("action_type"));
        event.setTargetType(rs.getString("target_type"));
        event.setTargetId(rs.getString("target_id"));
        event.setOutcome(rs.getString("outcome"));
        event.setDescription(rs.getString("description"));
        Timestamp ts = rs.getTimestamp("timestamp");
        if (ts != null) {
            event.setTimestamp(ts.toLocalDateTime());
        }
        return event;
    }
}
