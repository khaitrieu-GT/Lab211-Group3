package view;

import java.util.List;

/**
 * Lop cha cua cac View: ham hien thi dung chung va bat loi nghiep vu de in ra man hinh.
 */
public abstract class BaseView {

    protected static final String LINE = "==========================================================================";

    protected final ConsoleInput input = ConsoleInput.getInstance();

    protected void printHeader(String title) {
        System.out.println();
        System.out.println(LINE);
        System.out.println("  " + title);
        System.out.println(LINE);
    }

    protected void printSection(String title) {
        System.out.println();
        System.out.println("---------- " + title + " ----------");
    }

    protected void printList(List<?> items, String emptyMessage) {
        if (items == null || items.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        for (Object item : items) {
            System.out.println(item);
        }
    }

    protected void printSuccess(String message) {
        System.out.println(">> " + message);
    }

    protected void printError(String message) {
        System.out.println("!! " + message);
    }

    /** Khanh: SellerView.showMessage. */
    public void showMessage(String message) {
        System.out.println(message);
    }

    /** Chay mot thao tac; loi nghiep vu duoc in ra thay vi lam dung chuong trinh. */
    protected void safely(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            if (e instanceof java.util.NoSuchElementException) {
                throw e; // het du lieu vao: de Main ket thuc chuong trinh
            }
            printError(e.getMessage());
        }
    }
}
