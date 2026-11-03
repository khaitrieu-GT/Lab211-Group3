package view;

import java.util.List;
import model.User;

/**
 * Giao dien quan ly nguoi dung (Nhat: AdminView.renderUserList).
 */
public class UserView extends BaseView {

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

    public void displayUsers(List<User> users) {
        this.printSection("DANH SACH NGUOI DUNG");
        System.out.println(String.format("%-6s | %-12s | %-22s | %-24s | %-11s | %-6s | %s",
                "ID", "Username", "Ho ten", "Email", "Dien thoai", "Role", "Status"));
        this.printList(users, "Chua co nguoi dung.");
    }
}
