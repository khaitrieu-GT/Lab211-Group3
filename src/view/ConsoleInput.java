package view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Doc du lieu tu ban phim, dung chung mot Scanner (Khoa: MainView.readInt/readString).
 */
public final class ConsoleInput {

    private static final ConsoleInput INSTANCE = new ConsoleInput();

    private final Scanner scanner = new Scanner(System.in);

    private ConsoleInput() {
    }

    public static ConsoleInput getInstance() {
        return INSTANCE;
    }

    /** Khoa: readString. */
    public String readString(String message) {
        System.out.print(message);
        // Bo ky tu BOM (xuat hien khi chuyen huong input tu file/PowerShell tren Windows)
        return this.scanner.nextLine().replace("﻿", "").trim();
    }

    public String readNonEmpty(String message) {
        while (true) {
            String value = readString(message);
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Khong duoc de trong.");
        }
    }

    /** Khoa: readInt. */
    public int readInt(String message) {
        while (true) {
            try {
                return Integer.parseInt(readString(message));
            } catch (NumberFormatException e) {
                System.out.println("Vui long nhap so nguyen hop le.");
            }
        }
    }

    public int readIntInRange(String message, int min, int max) {
        while (true) {
            int value = readInt(message);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("Vui long nhap so tu " + min + " den " + max + ".");
        }
    }

    public BigDecimal readMoney(String message) {
        while (true) {
            try {
                BigDecimal value = new BigDecimal(readString(message).replace(",", ""));
                if (value.signum() >= 0) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // nhap lai
            }
            System.out.println("Vui long nhap so tien hop le (>= 0).");
        }
    }

    /** Bo trong de tra ve null (dung gia mac dinh). */
    public BigDecimal readOptionalMoney(String message) {
        while (true) {
            String text = readString(message).replace(",", "");
            if (text.isEmpty()) {
                return null;
            }
            try {
                BigDecimal value = new BigDecimal(text);
                if (value.signum() >= 0) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // nhap lai
            }
            System.out.println("Vui long nhap so tien hop le hoac bo trong.");
        }
    }

    public LocalDate readDate(String message) {
        while (true) {
            try {
                return LocalDate.parse(readString(message));
            } catch (DateTimeParseException e) {
                System.out.println("Ngay khong hop le, dinh dang yyyy-MM-dd.");
            }
        }
    }

    public LocalDate readOptionalDate(String message) {
        while (true) {
            String text = readString(message);
            if (text.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(text);
            } catch (DateTimeParseException e) {
                System.out.println("Ngay khong hop le, dinh dang yyyy-MM-dd.");
            }
        }
    }

    public LocalTime readTime(String message) {
        while (true) {
            try {
                return LocalTime.parse(readString(message));
            } catch (DateTimeParseException e) {
                System.out.println("Gio khong hop le, dinh dang HH:mm.");
            }
        }
    }

    public LocalTime readOptionalTime(String message) {
        while (true) {
            String text = readString(message);
            if (text.isEmpty()) {
                return null;
            }
            try {
                return LocalTime.parse(text);
            } catch (DateTimeParseException e) {
                System.out.println("Gio khong hop le, dinh dang HH:mm.");
            }
        }
    }

    public boolean readYesNo(String message) {
        while (true) {
            String value = readString(message + " (Y/N): ");
            if ("Y".equalsIgnoreCase(value)) {
                return true;
            }
            if ("N".equalsIgnoreCase(value)) {
                return false;
            }
            System.out.println("Vui long nhap Y hoac N.");
        }
    }
}
