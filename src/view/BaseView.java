package view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Lop cha cua cac View. View chi lam nhiem vu NHAP / XUAT tren console:
 * khong goi Controller, khong chua nghiep vu. Controller goi View de hien thi va lay du lieu nhap.
 */
public abstract class BaseView {

    public static final String LINE = "==========================================================================";

    private final ConsoleInput input = ConsoleInput.getInstance();

    // ============================ XUAT ============================

    public void printHeader(String title) {
        System.out.println();
        System.out.println(LINE);
        System.out.println("  " + title);
        System.out.println(LINE);
    }

    public void printSection(String title) {
        System.out.println();
        System.out.println("---------- " + title + " ----------");
    }

    public void printList(List<?> items, String emptyMessage) {
        if (items == null || items.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        for (Object item : items) {
            System.out.println(item);
        }
    }

    public void printSuccess(String message) {
        System.out.println(">> " + message);
    }

    public void printError(String message) {
        System.out.println("!! " + message);
    }

    /** Khanh: SellerView.showMessage. */
    public void showMessage(String message) {
        System.out.println(message);
    }

    /** Tieu de menu kem so thong bao chua doc. */
    public String withUnread(String title, int unread) {
        return title + (unread > 0 ? "   (" + unread + " thong bao moi)" : "");
    }

    // ============================ NHAP ============================

    public int inputChoice() {
        return this.input.readInt("Lua chon: ");
    }

    public String inputString(String label) {
        return this.input.readString(label);
    }

    public String inputNonEmpty(String label) {
        return this.input.readNonEmpty(label);
    }

    /** Ma doi tuong (vd M001, TK0001) luon viet hoa. */
    public String inputId(String label) {
        return this.input.readNonEmpty(label).toUpperCase();
    }

    public int inputInt(String label) {
        return this.input.readInt(label);
    }

    public int inputIntInRange(String label, int min, int max) {
        return this.input.readIntInRange(label, min, max);
    }

    public BigDecimal inputMoney(String label) {
        return this.input.readMoney(label);
    }

    public BigDecimal inputOptionalMoney(String label) {
        return this.input.readOptionalMoney(label);
    }

    public LocalDate inputDate(String label) {
        return this.input.readDate(label);
    }

    public LocalDate inputOptionalDate(String label) {
        return this.input.readOptionalDate(label);
    }

    public LocalTime inputTime(String label) {
        return this.input.readTime(label);
    }

    public LocalTime inputOptionalTime(String label) {
        return this.input.readOptionalTime(label);
    }

    public boolean confirm(String message) {
        return this.input.readYesNo(message);
    }
}
