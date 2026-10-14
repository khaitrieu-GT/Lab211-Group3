package view;

import java.util.List;
import model.AuditLog;
import model.Seat;
import model.Section;
import model.Stadium;
import model.User;

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
        System.out.println("9. Kiem thu chong Double Booking");
        System.out.println("10. Thong bao");
        System.out.println("11. Ho so ca nhan");
        System.out.println("0. Dang xuat");
        return this.inputInt("Lua chon cua ban: ");
    }

    public int showUserMenu() {
        this.printHeader("QUAN LY NGUOI DUNG (CRUD)");
        System.out.println("1. Xem danh sach nguoi dung & Seller");
        System.out.println("2. Khoa / Mo khoa tai khoan");
        System.out.println("3. Tao tai khoan Seller");
        System.out.println("4. Tim kiem nguoi dung");
        System.out.println("5. Cap nhat thong tin nguoi dung");
        System.out.println("6. Xoa tai khoan");
        System.out.println("0. Quay lai");
        return this.inputChoice();
    }

    public int showStadiumMenu() {
        this.printHeader("QUAN LY SAN VAN DONG (CRUD)");
        System.out.println("1. Xem danh sach san van dong");
        System.out.println("2. Them san van dong moi");
        System.out.println("3. Xem khu vuc & ghe cua san");
        System.out.println("4. Them khu vuc");
        System.out.println("5. Sua khu vuc (ten, suc chua)");
        System.out.println("6. Mo / Dong khu vuc");
        System.out.println("7. Sinh hang ghe cho khu vuc");
        System.out.println("8. Quan ly trang thai ghe (Khoa/Mo bao tri)");
        System.out.println("9. Khoa / Mo khoa ghe");
        System.out.println("10. Sua thong tin san van dong");
        System.out.println("11. Xoa san van dong");
        System.out.println("12. Xoa khu vuc");
        System.out.println("13. Xoa ghe");
        System.out.println("0. Quay lai");
        return this.inputChoice();
    }

    public int showTeamMenu() {
        this.printHeader("QUAN LY DOI BONG (CRUD)");
        System.out.println("1. Xem danh sach doi bong");
        System.out.println("2. Them doi bong");
        System.out.println("3. Sua doi bong");
        System.out.println("4. Xoa doi bong");
        System.out.println("0. Quay lai");
        return this.inputChoice();
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

    public void displayUsers(List<User> users) {
        this.printSection("DANH SACH NGUOI DUNG");
        System.out.println(String.format("%-6s | %-12s | %-22s | %-24s | %-11s | %-6s | %s",
                "ID", "Username", "Ho ten", "Email", "Dien thoai", "Role", "Status"));
        this.printList(users, "Chua co nguoi dung.");
    }

    public void displayStadiums(List<Stadium> stadiums) {
        this.printSection("DANH SACH SAN VAN DONG");
        this.printList(stadiums, "Chua co san van dong.");
    }

    public void displayStadiumLayout(Stadium stadium) {
        this.printSection(stadium.getName() + " - suc chua " + stadium.getCapacity()
                + ", da phan bo " + stadium.getAllocatedCapacity());
        for (Section section : stadium.getSections()) {
            System.out.println(section);
            for (Seat seat : section.getSeats()) {
                System.out.println("    " + seat);
            }
        }
    }

    /** Nhat ky moi nhat truoc, toi da 30 dong. */
    public void displayAuditLogs(List<AuditLog> logs) {
        this.printSection("NHAT KY HE THONG (moi nhat truoc, toi da 30 dong)");
        this.printList(logs.size() > 30 ? logs.subList(0, 30) : logs, "Chua co nhat ky.");
    }

    public void displayDoubleBookingResult(String ticketId, List<String> lines) {
        this.printSection("2 BUYER CUNG LUC GIU GHE " + ticketId);
        for (String line : lines) {
            System.out.println(line);
        }
    }
}
