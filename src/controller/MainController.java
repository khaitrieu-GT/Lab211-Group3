package controller;

import exception.BusinessException;
import java.util.NoSuchElementException;
import model.User;
import model.enums.Role;
import model.enums.UserStatus;
import repository.UserRepository;
import service.AccountService;
import service.AuditService;
import service.NotificationService;
import view.MainView;

/**
 * Controller khoi dau cua chuong trinh (Minh: MainController).
 * Main -> MainController.start() -> dang nhap / dang ky -> chuyen sang controller theo vai tro:
 * AdminController, SellerController, BuyerController. Moi thao tac nhap/xuat deu qua MainView.
 */
public class MainController {

    private final MainView mainView = new MainView();
    private final UserRepository userRepo = new UserRepository();
    private final NotificationService notificationService = new NotificationService();
    private final AuditService auditService = new AuditService();
    private final AccountService accountService = new AccountService();
    private User currentUser;

    // ============================ LUONG (FLOW) ============================

    /** Vong lap menu chinh cua toan he thong. */
    public void start() {
        this.mainView.showWelcome();
        try {
            this.runMainMenu();
        } catch (NoSuchElementException e) {
            this.mainView.showEndOfInput();
        }
        this.mainView.showGoodbye();
    }

    private void runMainMenu() {
        while (true) {
            int choice = this.mainView.showMainMenu();
            switch (choice) {
                case 1: this.runSafely(this::handleLogin); break;
                case 2: this.runSafely(this::handleRegister); break;
                case 0: return;
                default: this.mainView.printError("Lua chon khong hop le!");
            }
        }
    }

    private void handleLogin() {
        String username = this.mainView.inputNonEmpty("Username: ");
        String password = this.mainView.inputNonEmpty("Password: ");
        User user = this.login(username, password);
        this.mainView.printSuccess("Dang nhap thanh cong! Xin chao " + user.getFullName() + " (" + user.getRole() + ")");
        try {
            this.openDashboard(user);
        } finally {
            this.logout();
            this.mainView.printSuccess("Da dang xuat.");
        }
    }

    /** Dieu huong theo vai tro (phan quyen): moi vai tro co controller rieng. */
    private void openDashboard(User user) {
        switch (user.getRole()) {
            case ADMIN:
                new AdminController(user).start();
                break;
            case SELLER:
                new SellerController(user).start();
                break;
            default:
                new BuyerController(user).start();
                break;
        }
    }

    private void handleRegister() {
        this.mainView.printSection("DANG KY TAI KHOAN BUYER");
        String username = this.mainView.inputNonEmpty("Username (3-30 ky tu chu/so): ");
        String password = this.mainView.inputNonEmpty("Password (>= 6 ky tu): ");
        String fullName = this.mainView.inputNonEmpty("Ho ten: ");
        String email = this.mainView.inputNonEmpty("Email: ");
        String phone = this.mainView.inputString("Dien thoai: ");
        User user = this.register(username, password, fullName, email, phone);
        this.mainView.printSuccess("Dang ky thanh cong! Ma tai khoan: " + user.getId() + ". Hay dang nhap de mua ve.");
    }

    private void runSafely(Runnable action) {
        try {
            action.run();
        } catch (NoSuchElementException e) {
            throw e; // het du lieu vao: ket thuc chuong trinh
        } catch (RuntimeException e) {
            this.mainView.printError(e.getMessage());
        }
    }

    // ============================ NGHIEP VU ============================

    /** Minh: handleLogin - kiem tra tai khoan trong users.csv. */
    public User login(String username, String password) {
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
        this.notificationService.send(user.getId(), "Chuc mung ban da dang nhap thanh cong!");
        return user;
    }

    /** Dang ky tai khoan Buyer moi. */
    public User register(String username, String password, String fullName, String email, String phone) {
        User user = this.accountService.createAccount(Role.BUYER, username, password, fullName, email, phone, "avatar.png");
        this.auditService.log(username, "REGISTER " + user.getId(), AuditService.SUCCESS);
        this.notificationService.send(user.getId(), "Chao mung " + fullName + " den voi Stadium Ticket Booking!");
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
}
