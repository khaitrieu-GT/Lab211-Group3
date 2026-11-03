package view;

/**
 * Man hinh chinh (Minh: view.Main, Khoa: MainView): chi hien thi menu va nhan lua chon.
 * Luong dang nhap / dang ky do controller.MainController dieu khien.
 */
public class MainView extends BaseView {

    public void showWelcome() {
        System.out.println("==================================================");
        System.out.println("  KHOI DONG HE THONG BAN VE SAN VAN DONG (LAB211)");
        System.out.println("==================================================");
    }

    public void showGoodbye() {
        System.out.println("\nCAM ON BAN DA SU DUNG!");
    }

    public void showEndOfInput() {
        System.out.println();
        System.out.println("Ket thuc du lieu dau vao.");
    }

    public int showMainMenu() {
        this.printHeader("STADIUM TICKET BOOKING SYSTEM - LAB211 GROUP 3");
        System.out.println("1. Dang nhap");
        System.out.println("2. Dang ky tai khoan Buyer");
        System.out.println("0. Thoat");
        return this.inputChoice();
    }
}
