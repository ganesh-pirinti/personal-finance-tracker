package model;

import java.time.LocalDateTime;

/**
 * Encapsulates an immutable record of an administrative or security-sensitive operation.
 */
public class AdminAuditEvent {
    private int eventId;
    private int actorUserId;
    private String actorEmail;
    private String actionType;   // e.g. "USER_ROLE_CHANGE", "USER_STATUS_CHANGE", "THEME_PUBLISH", "LOGIN_SUCCESS", "LOGIN_FAILED", "BOOTSTRAP_ADMIN"
    private String targetType;   // e.g. "USER", "SYSTEM", "THEME", "CONFIG"
    private String targetId;     // e.g. "12", "DARK_THEME", "app.name"
    private String outcome;      // "SUCCESS", "FAILURE", "BLOCKED"
    private String description;
    private LocalDateTime timestamp;

    public AdminAuditEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public AdminAuditEvent(int actorUserId, String actorEmail, String actionType,
                            String targetType, String targetId, String outcome,
                            String description) {
        this(0, actorUserId, actorEmail, actionType, targetType, targetId, outcome, description, LocalDateTime.now());
    }

    public AdminAuditEvent(int eventId, int actorUserId, String actorEmail, String actionType,
                            String targetType, String targetId, String outcome,
                            String description, LocalDateTime timestamp) {
        this.eventId = eventId;
        this.actorUserId = actorUserId;
        this.actorEmail = (actorEmail != null) ? actorEmail : "SYSTEM";
        this.actionType = actionType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.outcome = outcome;
        this.description = description;
        this.timestamp = (timestamp != null) ? timestamp : LocalDateTime.now();
    }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public int getActorUserId() { return actorUserId; }
    public void setActorUserId(int actorUserId) { this.actorUserId = actorUserId; }

    public String getActorEmail() { return actorEmail; }
    public void setActorEmail(String actorEmail) { this.actorEmail = actorEmail; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "AdminAuditEvent{" +
                "id=" + eventId +
                ", actor='" + actorEmail + '\'' +
                ", action='" + actionType + '\'' +
                ", target='" + targetType + ":" + targetId + '\'' +
                ", outcome='" + outcome + '\'' +
                ", time=" + timestamp +
                '}';
    }
}
