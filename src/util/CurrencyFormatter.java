package util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility for formatting currency in Indian standard format (e.g. ₹15,000.00).
 */
public final class CurrencyFormatter {
    private CurrencyFormatter() {}

    private static final DecimalFormat INDIAN_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.of("en", "IN"));
        symbols.setCurrencySymbol("₹");
        INDIAN_FORMAT = new DecimalFormat("₹##,##,##0.00", symbols);
    }

    public static String format(double amount) {
        return INDIAN_FORMAT.format(amount);
    }

    public static String format(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        return INDIAN_FORMAT.format(amount);
    }

    public static String formatSigned(double amount, boolean isIncome) {
        String base = INDIAN_FORMAT.format(Math.abs(amount));
        if (isIncome) {
            return "+" + base;
        } else {
            return "-" + base;
        }
    }
}
