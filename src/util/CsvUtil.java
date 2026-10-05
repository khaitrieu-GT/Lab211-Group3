package util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Ham ho tro doc/ghi mot dong CSV.
 * Gia tri chua dau phay hoac dau nhay kep duoc boc trong "..." theo chuan CSV.
 */
public final class CsvUtil {

    private CsvUtil() {
    }

    public static String join(Object... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(clean(values[i]));
        }
        return sb.toString();
    }

    public static String[] split(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else if (c == '"') {
                    quoted = false;
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                parts.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        parts.add(current.toString().trim());
        return parts.toArray(new String[0]);
    }

    public static String clean(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).withNano(0).toString();
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toPlainString();
        }
        String text = value.toString().replace("\r", " ").replace("\n", " ").trim();
        if (text.contains(",") || text.contains("\"")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    public static String text(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    public static int toInt(String value) {
        return value == null || value.isEmpty() ? 0 : Integer.parseInt(value);
    }

    public static BigDecimal toMoney(String value) {
        return value == null || value.isEmpty() ? BigDecimal.ZERO : new BigDecimal(value);
    }

    public static LocalDateTime toDateTime(String value) {
        return value == null || value.isEmpty() ? null : LocalDateTime.parse(value);
    }

    public static LocalDate toDate(String value) {
        return value == null || value.isEmpty() ? null : LocalDate.parse(value);
    }

    public static LocalTime toTime(String value) {
        return value == null || value.isEmpty() ? null : LocalTime.parse(value);
    }

    public static <E extends Enum<E>> E toEnum(Class<E> type, String value) {
        return value == null || value.isEmpty() ? null : Enum.valueOf(type, value.toUpperCase());
    }
}
