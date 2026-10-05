package util;

import java.math.BigDecimal;

public final class MoneyUtil {

    private MoneyUtil() {
    }

    public static String format(BigDecimal amount) {
        return String.format("%,.0f VND", amount == null ? BigDecimal.ZERO : amount);
    }
}
