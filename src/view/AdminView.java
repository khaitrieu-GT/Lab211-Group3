package view;

import java.util.List;
import model.AuditLog;

/**
 * Giao dien quan tri (Nhat: AdminView): chi hien thi menu, danh sach va nhan lua chon.
 * Luong nghiep vu do controller.AdminController dieu khien.
 */
public class AdminView extends BaseView {

    /** Nhat: displayMenu. */
    public int showMainMenu(String title) {
        this.printHeader(title);
        System.out.println("1. Quan ly nguoi dung & Seller");
        System.out.println("2. Quan ly san van dong, khu vuc, ghe");
        System.out.println("3. Quan ly doi bong");
        System.out.println("4. Xem tat ca tran dau");
        System.out.println("5. Giam sat booking / thanh toan / giao dich");
        System.out.println("6. Xu ly yeu cau huy ve");
        System.out.println("7. Bao cao doanh thu");
        System.out.println("8. Nhat ky he thong (audit log)");
        System.out.println("9. Thong bao");
        System.out.println("10. Ho so ca nhan");
        System.out.println("0. Dang xuat");
        return this.inputInt("Lua chon cua ban: ");
    }

    public int showMonitorMenu() {
        this.printHeader("GIAM SAT GIAO DICH");
        System.out.println("1. Tat ca booking");
        System.out.println("2. Tat ca thanh toan");
        System.out.println("3. Tat ca giao dich cong thanh toan");
        System.out.println("0. Quay lai");
        return this.inputChoice();
    }

    public int showCancellationMenu() {
        this.printHeader("XU LY YEU CAU HUY VE");
        System.out.println("1. Yeu cau dang cho xu ly");
        System.out.println("2. Duyet yeu cau (huy ve + hoan tien)");
        System.out.println("3. Tu choi yeu cau");
        System.out.println("4. Tat ca yeu cau");
        System.out.println("0. Quay lai");
        return this.inputChoice();
    }

    /** Nhat ky moi nhat truoc, toi da 30 dong. */
    public void displayAuditLogs(List<AuditLog> logs) {
        this.printSection("NHAT KY HE THONG (moi nhat truoc, toi da 30 dong)");
        this.printList(logs.size() > 30 ? logs.subList(0, 30) : logs, "Chua co nhat ky.");
    }
}
