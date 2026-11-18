package service;

import dao.SessionDAO;
import model.User;
import model.UserSession;

import java.net.InetAddress;
import java.util.*;
import java.util.logging.Logger;

/**
 * Service managing user authentication sessions, heartbeat activity, timeout enforcement,
 * and administrative session revocation.
 */
public class SessionService {
    private static final Logger LOGGER = Logger.getLogger(SessionService.class.getName());
    private static final SessionService INSTANCE = new SessionService();

    private final SessionDAO sessionDAO;
    private final AuditLogService auditLogService;
    private static String currentSessionId = null;
    private static UserSession currentSession = null;

    public SessionService() {
        this.sessionDAO = new SessionDAO();
        this.auditLogService = new AuditLogService();
    }

    public SessionService(SessionDAO sessionDAO, AuditLogService auditLogService) {
        this.sessionDAO = sessionDAO;
        this.auditLogService = auditLogService;
    }

    public static SessionService getInstance() {
        return INSTANCE;
    }

    public static String getCurrentSessionId() {
        return currentSessionId;
    }

    public static UserSession getCurrentSession() {
        return currentSession;
    }

    /**
     * Resolves local workstation client info.
     */
    public static String getClientInfo() {
        try {
            String host = InetAddress.getLocalHost().getHostName();
            String os = System.getProperty("os.name");
            return os + " / " + host;
        } catch (Exception e) {
            return "Java Desktop / Localhost";
        }
    }

    /**
     * Creates a new session upon successful authentication.
     */
    public synchronized UserSession createSession(User user, String clientInfo) {
        if (user == null) return null;

        int timeoutMinutes = 60;
        try {
            String val = ApplicationTextService.getInstance().getText("session.timeout_minutes", "60");
            timeoutMinutes = Integer.parseInt(val);
        } catch (Exception ignored) {}

        String info = (clientInfo != null && !clientInfo.trim().isEmpty()) ? clientInfo : getClientInfo();
        UserSession session = sessionDAO.createSession(
                user.getUserId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                timeoutMinutes,
                info
        );

        if (session != null) {
            currentSessionId = session.getSessionId();
            currentSession = session;
            auditLogService.log(user.getUserId(), user.getEmail(), "SESSION_CREATED", "SESSION",
                    session.getSessionId(), "SUCCESS",
                    "Session established: " + session.getMaskedSessionId() + " (" + session.getRole() + ")");
        }
        return session;
    }

    /**
     * Verifies if the active session is valid and active.
     */
    public synchronized boolean validateCurrentSession() {
        if (currentSessionId == null) {
            return false;
        }
        UserSession session = sessionDAO.findSessionById(currentSessionId);
        if (session == null || !session.isActive()) {
            currentSessionId = null;
            currentSession = null;
            return false;
        }
        sessionDAO.updateLastActivity(currentSessionId);
        currentSession = session;
        return true;
    }

    /**
     * Touches active session to refresh heartbeat.
     */
    public void touchSession() {
        if (currentSessionId != null) {
            sessionDAO.updateLastActivity(currentSessionId);
        }
    }

    /**
     * Ends the current local session upon user logout.
     */
    public synchronized void logoutCurrentSession(User actor) {
        if (currentSessionId != null) {
            sessionDAO.revokeSession(currentSessionId);
            if (actor != null) {
                auditLogService.log(actor.getUserId(), actor.getEmail(), "SESSION_LOGOUT", "SESSION",
                        currentSessionId, "SUCCESS", "User logged out, session terminated.");
            }
            currentSessionId = null;
            currentSession = null;
        }
    }

    /**
     * Administratively revokes a single target session.
     */
    public boolean revokeSession(String sessionId, User actor) {
        if (actor == null || !actor.isAdmin()) {
            throw new SecurityException("Only administrators can revoke user sessions.");
        }
        boolean ok = sessionDAO.revokeSession(sessionId);
        if (ok) {
            auditLogService.log(actor.getUserId(), actor.getEmail(), "SESSION_REVOKED", "SESSION",
                    sessionId, "SUCCESS", "Admin " + actor.getEmail() + " revoked session: " + sessionId);
            if (sessionId.equals(currentSessionId)) {
                currentSessionId = null;
                currentSession = null;
            }
        }
        return ok;
    }

    /**
     * Administratively revokes all active sessions for a given user.
     */
    public int revokeAllForUser(int userId, User actor) {
        if (actor != null && !actor.isAdmin() && actor.getUserId() != userId) {
            throw new SecurityException("Unauthorized session revocation attempt.");
        }
        int count = sessionDAO.revokeAllForUser(userId);
        if (actor != null) {
            auditLogService.log(actor.getUserId(), actor.getEmail(), "SESSION_REVOKE_USER", "SESSION",
                    String.valueOf(userId), "SUCCESS",
                    "Revoked " + count + " active session(s) for user ID: " + userId);
        }
        if (currentSession != null && currentSession.getUserId() == userId) {
            currentSessionId = null;
            currentSession = null;
        }
        return count;
    }

    /**
     * Administratively revokes all other admin sessions, preserving the calling admin's session.
     */
    public int revokeOtherAdmins(String keepSessionId, User actor) {
        if (actor == null || !actor.isAdmin()) {
            throw new SecurityException("Only administrators can revoke other admin sessions.");
        }
        String preserveId = keepSessionId != null ? keepSessionId : currentSessionId;
        int count = sessionDAO.revokeOtherAdminSessions(preserveId);
        auditLogService.log(actor.getUserId(), actor.getEmail(), "SESSION_REVOKE_ADMINS", "SESSION",
                preserveId, "SUCCESS",
                "Admin " + actor.getEmail() + " revoked " + count + " other admin session(s).");
        return count;
    }

    public List<UserSession> getAllSessions() {
        return sessionDAO.getAllSessions();
    }

    public List<UserSession> getActiveSessions() {
        return sessionDAO.getActiveSessions();
    }

    public Map<String, Integer> getSessionMetrics() {
        List<UserSession> all = sessionDAO.getAllSessions();
        int total = all.size();
        int active = 0;
        int expired = 0;
        int revoked = 0;
        int adminSessions = 0;

        for (UserSession s : all) {
            if (s.isActive()) {
                active++;
                if (User.ROLE_ADMIN.equalsIgnoreCase(s.getRole())) {
                    adminSessions++;
                }
            } else if (UserSession.STATUS_EXPIRED.equalsIgnoreCase(s.getStatus())) {
                expired++;
            } else if (UserSession.STATUS_REVOKED.equalsIgnoreCase(s.getStatus())) {
                revoked++;
            }
        }

        Map<String, Integer> metrics = new LinkedHashMap<>();
        metrics.put("total", total);
        metrics.put("active", active);
        metrics.put("expired", expired);
        metrics.put("revoked", revoked);
        metrics.put("adminSessions", adminSessions);
        return metrics;
    }
}
