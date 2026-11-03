package view;

import java.util.List;
import model.Notification;
import model.User;

/** Ho so ca nhan va thong bao - dung chung cho Admin, Seller, Buyer. */
public class ProfileView extends BaseView {

    public int showProfileMenu() {
        this.printHeader("HO SO CA NHAN");
        System.out.println("1. Xem ho so");
        System.out.println("2. Cap nhat ho so");
        System.out.println("3. Doi mat khau");
        System.out.println("0. Quay lai");
        return this.inputChoice();
    }

    public void displayNotifications(List<Notification> notifications) {
        this.printSection("THONG BAO ([*] = chua doc)");
        this.printList(notifications, "Khong co thong bao.");
    }

    public void displayProfile(User user) {
        this.printSection("THONG TIN TAI KHOAN");
        System.out.println("Ma         : " + user.getId());
        System.out.println("Username   : " + user.getUsername());
        System.out.println("Ho ten     : " + user.getFullName());
        System.out.println("Email      : " + user.getEmail());
        System.out.println("Dien thoai : " + user.getPhone());
        System.out.println("Avatar     : " + user.getAvatar());
        System.out.println("Vai tro    : " + user.getRole());
        System.out.println("Trang thai : " + user.getStatus());
        System.out.println("Quyen      : " + user.getPermissions());
    }
}
