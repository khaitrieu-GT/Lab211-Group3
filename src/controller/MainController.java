package controller;

import exception.BusinessException;
import java.util.List;
import model.Notification;
import model.User;
import model.enums.Role;
import model.enums.UserStatus;
import repository.UserRepository;
import service.AuditService;
import service.NotificationService;
import util.PasswordUtil;

/**
 * Dieu phoi chung: dang nhap, dang ky, dang xuat, thong bao (Minh: MainController).
 */
public class MainController {

    private final UserRepository userRepo = new UserRepository();
    private final NotificationService notificationService = new NotificationService();
    private final AuditService auditService = new AuditService();
    private User currentUser;

    /** Minh: handleLogin - kiem tra tai khoan trong users.csv. */
    public User handleLogin(String username, String password) {
        User user = this.userRepo.findByUsername(username);
        if (user == null || !user.login(username, password)) {
            this.auditService.log(username, "LOGIN", AuditService.FAILED);
            throw new BusinessException("Sai ten dang nhap hoac mat khau!");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            this.auditService.log(username, "LOGIN (tai khoan " + user.getStatus() + ")", AuditService.FAILED);
            throw new BusinessException("Tai khoan dang o trang thai " + user.getStatus() + ", vui long lien he Admin.");
        }
        this.currentUser = user;
        this.auditService.log(user.getUsername(), "LOGIN", AuditService.SUCCESS);
        triggerNotification(user.getId(), "Chuc mung ban da dang nhap thanh cong!");
        return user;
    }

    /** Dang ky tai khoan Buyer moi. */
    public User handleRegister(String username, String password, String fullName, String email, String phone) {
        if (this.userRepo.findByUsername(username) != null) {
            throw new BusinessException("Ten dang nhap da ton tai.");
        }
        if (password == null || password.length() < 6) {
            throw new BusinessException("Mat khau phai co it nhat 6 ky tu.");
        }
        User user = User.create(Role.BUYER, null, username, PasswordUtil.hash(password), fullName, email, phone,
                "avatar.png", UserStatus.ACTIVE);
        if (!user.register()) {
            throw new BusinessException("Thong tin dang ky khong hop le (username 3-30 ky tu chu/so, email hop le).");
        }
        this.userRepo.insert(user);
        this.auditService.log(username, "REGISTER " + user.getId(), AuditService.SUCCESS);
        triggerNotification(user.getId(), "Chao mung " + fullName + " den voi Stadium Ticket Booking!");
        return user;
    }

    public void logout() {
        if (this.currentUser != null) {
            this.auditService.log(this.currentUser.getUsername(), "LOGOUT", AuditService.SUCCESS);
        }
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    /** Minh: triggerNotification. */
    public void triggerNotification(String userId, String message) {
        this.notificationService.send(userId, message);
    }

    public List<Notification> getNotifications(String userId) {
        return this.notificationService.getByUser(userId);
    }

    public int countUnreadNotifications(String userId) {
        return this.notificationService.countUnread(userId);
    }

    public void markNotificationsAsRead(String userId) {
        this.notificationService.markAllAsRead(userId);
    }
}
