package controller;

import exception.BusinessException;
import java.util.ArrayList;
import java.util.List;
import model.Match;
import model.User;
import model.enums.MatchStatus;
import model.enums.Role;
import model.enums.UserStatus;
import repository.MatchRepository;
import repository.UserRepository;
import service.AccountService;
import service.AuditService;
import service.NotificationService;
import util.ValidationUtil;

/**
 * Quan ly nguoi dung (Nhat: AdminController): xem, tim, khoa/mo khoa, tao Seller, sua, xoa mem.
 */
public class UserController {

    private final UserRepository userRepo = new UserRepository();
    private final MatchRepository matchRepo = new MatchRepository();
    private final AccountService accountService = new AccountService();
    private final NotificationService notificationService = new NotificationService();
    private final AuditService auditService = new AuditService();
    private final User admin;

    public UserController(User admin) {
        this.admin = admin;
    }

    /** Nhat: getAllUsers. */
    public List<User> getAllUsers() {
        return this.userRepo.findAll();
    }

    /** Tim theo ma, username, ho ten hoac email (khong phan biet hoa thuong). */
    public List<User> searchUsers(String keyword) {
        String key = keyword == null ? "" : keyword.trim().toLowerCase();
        List<User> result = new ArrayList<>();
        for (User user : this.userRepo.findAll()) {
            String text = (user.getId() + " " + user.getUsername() + " " + user.getFullName() + " "
                    + user.getEmail()).toLowerCase();
            if (text.contains(key)) {
                result.add(user);
            }
        }
        return result;
    }

    /** Nhat: changeUserStatus - khoa/mo khoa tai khoan. */
    public boolean changeUserStatus(String userId, UserStatus status) {
        if (this.admin.getId().equalsIgnoreCase(userId)) {
            throw new BusinessException("Khong the tu doi trang thai tai khoan cua chinh minh.");
        }
        User user = this.userRepo.findById(userId);
        if (user != null && user.getStatus() == UserStatus.DELETED) {
            throw new BusinessException("Tai khoan da bi xoa, khong the doi trang thai.");
        }
        boolean ok = this.userRepo.updateUserStatus(userId, status);
        this.auditService.log(this.admin.getUsername(), "USER_STATUS " + userId + " -> " + status,
                ok ? AuditService.SUCCESS : AuditService.FAILED);
        if (ok) {
            this.notificationService.send(userId, "Tai khoan cua ban da duoc chuyen sang trang thai " + status + ".");
        }
        return ok;
    }

    /** Admin cap tai khoan Seller. */
    public User createSellerAccount(String username, String password, String fullName, String email, String phone) {
        User seller = this.accountService.createAccount(Role.SELLER, username, password, fullName, email, phone, "seller.png");
        this.auditService.log(this.admin.getUsername(), "CREATE_SELLER " + seller.getId(), AuditService.SUCCESS);
        return seller;
    }

    /** Admin sua ho ten / email / dien thoai cua nguoi dung. */
    public User updateUserInfo(String userId, String fullName, String email, String phone) {
        User user = this.requireUser(userId);
        if (!ValidationUtil.isBlank(email)) {
            User owner = this.userRepo.findByEmail(email);
            if (owner != null && !owner.getId().equalsIgnoreCase(user.getId())) {
                throw new BusinessException("Email da duoc tai khoan khac su dung.");
            }
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            throw new BusinessException("So dien thoai phai gom 10 chu so va bat dau bang 0.");
        }
        user.updateProfile(fullName, email, phone, null);
        this.userRepo.update(user);
        this.auditService.log(this.admin.getUsername(), "UPDATE_USER " + userId, AuditService.SUCCESS);
        return user;
    }

    /**
     * Xoa tai khoan (xoa mem): chuyen trang thai DELETED, khong dang nhap duoc nua
     * nhung van giu lich su booking/ve de doi soat. Khong xoa chinh minh va Admin cuoi cung.
     */
    public void deleteUser(String userId) {
        User user = this.requireUser(userId);
        if (this.admin.getId().equalsIgnoreCase(user.getId())) {
            throw new BusinessException("Khong the tu xoa tai khoan cua chinh minh.");
        }
        if (user.getStatus() == UserStatus.DELETED) {
            throw new BusinessException("Tai khoan da bi xoa truoc do.");
        }
        if (user.getRole() == Role.SELLER) {
            for (Match match : this.matchRepo.findBySellerId(user.getId())) {
                if (match.getStatus() == MatchStatus.OPEN && !match.hasStarted()) {
                    throw new BusinessException("Seller dang co tran " + match.getId() + " mo ban, hay dong ban truoc.");
                }
            }
        }
        user.markDeleted();
        this.userRepo.update(user);
        this.auditService.log(this.admin.getUsername(), "DELETE_USER " + userId, AuditService.SUCCESS);
    }

    private User requireUser(String userId) {
        User user = this.userRepo.findById(userId);
        if (user == null) {
            throw new BusinessException("Khong tim thay nguoi dung " + userId);
        }
        return user;
    }
}
