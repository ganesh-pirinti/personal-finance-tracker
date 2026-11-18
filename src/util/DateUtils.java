package util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility functions for date manipulation, validation, and formatting.
 */
public final class DateUtils {
    private DateUtils() {}

    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(java.time.format.ResolverStyle.STRICT);
    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    public static final DateTimeFormatter MONTH_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    public static final DateTimeFormatter MONTH_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");
    public static final DateTimeFormatter SHORT_MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM");

    public static String formatIso(LocalDate date) {
        if (date == null) return "";
        return date.format(ISO_FORMATTER);
    }

    public static String formatDisplay(LocalDate date) {
        if (date == null) return "";
        return date.format(DISPLAY_FORMATTER);
    }

    public static String getCurrentMonthKey() {
        return YearMonth.now().format(MONTH_KEY_FORMATTER);
    }

    public static String formatMonthKeyDisplay(String monthKey) {
        if (monthKey == null || monthKey.trim().isEmpty()) return "";
        try {
            YearMonth ym = YearMonth.parse(monthKey, MONTH_KEY_FORMATTER);
            return ym.format(MONTH_DISPLAY_FORMATTER);
        } catch (Exception e) {
            return monthKey;
        }
    }

    public static LocalDate parseIso(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(text.trim(), ISO_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static boolean isValidDate(String text) {
        return parseIso(text) != null;
    }

    public static boolean isValidIsoDate(String text) {
        return parseIso(text) != null;
    }

    /**
     * Returns a list of the last N month keys (e.g. ["2026-06", "2026-07", ..., "2026-11"]).
     */
    public static List<String> getLastMonths(int count) {
        List<String> list = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int i = count - 1; i >= 0; i--) {
            list.add(current.minusMonths(i).format(MONTH_KEY_FORMATTER));
        }
        return list;
    }

    /**
     * Returns short display names for the last N months (e.g. ["Jun", "Jul", ..., "Nov"]).
     */
    public static List<String> getLastMonthLabels(int count) {
        List<String> list = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int i = count - 1; i >= 0; i--) {
            list.add(current.minusMonths(i).format(SHORT_MONTH_FORMATTER));
        }
        return list;
    }
}
