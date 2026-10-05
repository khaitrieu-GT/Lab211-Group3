package view;

import controller.MainController;
import controller.ProfileController;
import java.util.List;
import model.Notification;
import model.User;

/** Ho so ca nhan va thong bao - dung chung cho Admin, Seller, Buyer. */
public class ProfileView extends BaseView {

    private final User user;
    private final ProfileController profileController = new ProfileController();
    private final MainController mainController;

    public ProfileView(User user, MainController mainController) {
        this.user = user;
        this.mainController = mainController;
    }

    public void showProfileMenu() {
        while (true) {
            printHeader("HO SO CA NHAN");
            System.out.println("1. Xem ho so");
            System.out.println("2. Cap nhat ho so");
            System.out.println("3. Doi mat khau");
            System.out.println("0. Quay lai");
            int choice = this.input.readInt("Lua chon: ");
            switch (choice) {
                case 1:
                    safely(this::renderProfile);
                    break;
                case 2:
                    safely(this::handleUpdateProfile);
                    break;
                case 3:
                    safely(this::handleChangePassword);
                    break;
                case 0:
                    return;
                default:
                    printError("Lua chon khong hop le!");
            }
        }
    }

    public void showNotifications() {
        List<Notification> list = this.mainController.getNotifications(this.user.getId());
        printSection("THONG BAO ([*] = chua doc)");
        printList(list, "Khong co thong bao.");
        this.mainController.markNotificationsAsRead(this.user.getId());
    }

    private void renderProfile() {
        User fresh = this.profileController.getProfile(this.user.getId());
        printSection("THONG TIN TAI KHOAN");
        System.out.println("Ma         : " + fresh.getId());
        System.out.println("Username   : " + fresh.getUsername());
        System.out.println("Ho ten     : " + fresh.getFullName());
        System.out.println("Email      : " + fresh.getEmail());
        System.out.println("Dien thoai : " + fresh.getPhone());
        System.out.println("Avatar     : " + fresh.getAvatar());
        System.out.println("Vai tro    : " + fresh.getRole());
        System.out.println("Trang thai : " + fresh.getStatus());
        System.out.println("Quyen      : " + fresh.getPermissions());
    }

    private void handleUpdateProfile() {
        System.out.println("(Bo trong de giu nguyen gia tri cu)");
        String name = this.input.readString("Ho ten moi: ");
        String email = this.input.readString("Email moi: ");
        String phone = this.input.readString("So dien thoai moi: ");
        String avatar = this.input.readString("Avatar moi: ");
        User updated = this.profileController.updateProfile(this.user.getId(), name, email, phone, avatar);
        this.user.updateProfile(updated.getFullName(), updated.getEmail(), updated.getPhone(), updated.getAvatar());
        printSuccess("Cap nhat ho so thanh cong!");
    }

    private void handleChangePassword() {
        String oldPassword = this.input.readNonEmpty("Mat khau cu: ");
        String newPassword = this.input.readNonEmpty("Mat khau moi (>= 6 ky tu): ");
        String confirm = this.input.readNonEmpty("Nhap lai mat khau moi: ");
        if (!newPassword.equals(confirm)) {
            printError("Mat khau nhap lai khong khop.");
            return;
        }
        this.profileController.changePassword(this.user.getId(), oldPassword, newPassword);
        printSuccess("Doi mat khau thanh cong!");
    }
}
