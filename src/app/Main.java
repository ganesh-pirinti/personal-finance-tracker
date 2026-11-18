package app;

/**
 * Main application launcher for Personal Finance Tracker.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=============================================================");
        System.out.println(" Personal Finance Tracker — Premium Java Swing & AWT Desktop");
        System.out.println(" Version: 2.1.0 (Advanced Java Programming Academic Edition)");
        System.out.println(" Engine: Java Swing, Java AWT, Graphics2D, JDBC, MySQL");
        System.out.println("=============================================================");

        Application app = new Application();
        app.start();
    }
}
