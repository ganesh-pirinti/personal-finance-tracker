package service;

import dao.AdminDAO;
import model.SystemMetrics;
import model.User;

/**
 * Service providing aggregated financial intelligence, user analytics, and platform observability
 * for the Super Admin Portal.
 */
public class AdminAnalyticsService {
    private final AdminDAO adminDAO;

    public AdminAnalyticsService() {
        this.adminDAO = new AdminDAO();
    }

    public AdminAnalyticsService(AdminDAO adminDAO) {
        this.adminDAO = adminDAO;
    }

    public SystemMetrics getSystemMetrics(User actor) {
        if (actor == null || !actor.isAdmin()) {
            throw new SecurityException("Unauthorized: Admin credentials required to access system metrics.");
        }
        return adminDAO.computeSystemMetrics();
    }
}
