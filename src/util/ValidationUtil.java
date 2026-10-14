package util;

/**
 * Cac quy tac kiem tra du lieu dung chung (username, email, so dien thoai).
 */
public final class ValidationUtil {

    public static final String USERNAME_REGEX = "[A-Za-z0-9_.]{3,30}";
    public static final String EMAIL_REGEX = "[^@\\s]+@[^@\\s]+\\.[^@\\s]+";
    public static final String PHONE_REGEX = "0\\d{9}";

    private ValidationUtil() {
    }

    public static boolean isValidUsername(String username) {
        return username != null && username.matches(USERNAME_REGEX);
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.trim().matches(EMAIL_REGEX);
    }

    /** So dien thoai co the bo trong; neu nhap thi phai gom 10 chu so, bat dau bang 0. */
    public static boolean isValidPhone(String phone) {
        return phone == null || phone.trim().isEmpty() || phone.trim().matches(PHONE_REGEX);
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
