package controller;

import command.ChangePasswordCommand;
import command.UpdateProfileCommand;
import exception.BusinessException;
import java.util.List;
import java.util.NoSuchElementException;
import model.Notification;
import model.User;
import repository.UserRepository;
import service.AuditService;
import service.NotificationService;
import view.ProfileView;

/**
 * Ho so ca nhan va thong bao cho moi vai tro (Trieu: UpdateProfileCommand, ChangePasswordCommand;
 * Minh: triggerNotification). Controller dieu khien luong, ProfileView chi nhap/xuat.
 */
public class ProfileController {

    private final User user;
    private final ProfileView profileView = new ProfileView();
    private final UserRepository userRepo = new UserRepository();
    private final NotificationService notificationService = new NotificationService();
    private final AuditService auditService = new AuditService();

    public ProfileController(User user) {
        this.user = user;
    }

    // ============================ LUONG (FLOW) ============================

    public void showProfileMenu() {
        while (true) {
            int choice = this.profileView.showProfileMenu();
            switch (choice) {
                case 1: this.runSafely(this::handleViewProfile); break;
                case 2: this.runSafely(this::handleUpdateProfile); break;
                case 3: this.runSafely(this::handleChangePassword); break;
                case 0: return;
                default: this.profileView.printError("Lua chon khong hop le!");
            }
        }
    }

    /** Hien thi thong bao roi danh dau da doc. */
    public void showNotifications() {
        this.profileView.displayNotifications(this.notificationService.getByUser(this.user.getId()));
        this.notificationService.markAllAsRead(this.user.getId());
    }

    private void handleViewProfile() {
        this.profileView.displayProfile(this.getProfile());
    }

    private void handleUpdateProfile() {
        this.profileView.showMessage("(Bo trong de giu nguyen gia tri cu)");
        String name = this.profileView.inputString("Ho ten moi: ");
        String email = this.profileView.inputString("Email moi: ");
        String phone = this.profileView.inputString("So dien thoai moi: ");
        String avatar = this.profileView.inputString("Avatar moi: ");
        User updated = this.updateProfile(name, email, phone, avatar);
        this.user.updateProfile(updated.getFullName(), updated.getEmail(), updated.getPhone(), updated.getAvatar());
        this.profileView.printSuccess("Cap nhat ho so thanh cong!");
    }

    private void handleChangePassword() {
        String oldPassword = this.profileView.inputNonEmpty("Mat khau cu: ");
        String newPassword = this.profileView.inputNonEmpty("Mat khau moi (>= 6 ky tu): ");
        String confirm = this.profileView.inputNonEmpty("Nhap lai mat khau moi: ");
        if (!newPassword.equals(confirm)) {
            this.profileView.printError("Mat khau nhap lai khong khop.");
            return;
        }
        this.changePassword(oldPassword, newPassword);
        this.profileView.printSuccess("Doi mat khau thanh cong!");
    }

    private void runSafely(Runnable action) {
        try {
            action.run();
        } catch (NoSuchElementException e) {
            throw e; // het du lieu vao: de MainController ket thuc chuong trinh
        } catch (RuntimeException e) {
            this.profileView.printError(e.getMessage());
        }
    }

    // ============================ NGHIEP VU ============================

    /** Nap lai ban moi nhat tu file. */
    public User getProfile() {
        User fresh = this.userRepo.findById(this.user.getId());
        if (fresh == null) {
            throw new BusinessException("Khong tim thay nguoi dung " + this.user.getId());
        }
        return fresh;
    }

    public User updateProfile(String fullName, String email, String phone, String avatar) {
        User fresh = this.getProfile();
        if (email != null && !email.trim().isEmpty()) {
            User owner = this.userRepo.findByEmail(email);
            if (owner != null && !owner.getId().equalsIgnoreCase(fresh.getId())) {
                throw new BusinessException("Email da duoc tai khoan khac su dung.");
            }
        }
        new UpdateProfileCommand(fresh, fullName, email, phone, avatar).execute();
        this.userRepo.update(fresh);
        this.auditService.log(fresh.getUsername(), "UPDATE_PROFILE", AuditService.SUCCESS);
        return fresh;
    }

    public void changePassword(String oldPassword, String newPassword) {
        User fresh = this.getProfile();
        try {
            new ChangePasswordCommand(fresh, oldPassword, newPassword).execute();
        } catch (RuntimeException e) {
            this.auditService.log(fresh.getUsername(), "CHANGE_PASSWORD", AuditService.FAILED);
            throw e;
        }
        this.userRepo.update(fresh);
        this.auditService.log(fresh.getUsername(), "CHANGE_PASSWORD", AuditService.SUCCESS);
    }

    public List<Notification> getNotifications() {
        return this.notificationService.getByUser(this.user.getId());
    }

    public int countUnreadNotifications() {
        return this.notificationService.countUnread(this.user.getId());
    }
}
