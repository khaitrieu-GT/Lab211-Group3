package view;

import controller.MainController;
import model.User;

/**
 * Man hinh chinh: dang nhap / dang ky, sau do chuyen sang giao dien theo vai tro
 * (Minh: view.Main + MainController.handleLogin, Khoa: MainView).
 */
public class MainView extends BaseView {

    private final MainController mainController = new MainController();

    public void start() {
        while (true) {
            printHeader("STADIUM TICKET BOOKING SYSTEM - LAB211 GROUP 3");
            System.out.println("1. Dang nhap");
            System.out.println("2. Dang ky tai khoan Buyer");
            System.out.println("0. Thoat");
            int choice = this.input.readInt("Lua chon: ");
            switch (choice) {
                case 1: safely(this::handleLogin); break;
                case 2: safely(this::handleRegister); break;
                case 0: return;
                default: printError("Lua chon khong hop le!");
            }
        }
    }

    private void handleLogin() {
        String username = this.input.readNonEmpty("Username: ");
        String password = this.input.readNonEmpty("Password: ");
        User user = this.mainController.handleLogin(username, password);
        printSuccess("Dang nhap thanh cong! Xin chao " + user.getFullName() + " (" + user.getRole() + ")");
        try {
            openDashboard(user);
        } finally {
            this.mainController.logout();
            printSuccess("Da dang xuat.");
        }
    }

    /** Dieu huong theo vai tro (phan quyen). */
    private void openDashboard(User user) {
        switch (user.getRole()) {
            case ADMIN:
                new AdminView(user, this.mainController).displayMenu();
                break;
            case SELLER:
                new SellerView(user, this.mainController).showMenu();
                break;
            default:
                new BuyerView(user, this.mainController).showMenu();
                break;
        }
    }

    private void handleRegister() {
        printSection("DANG KY TAI KHOAN BUYER");
        String username = this.input.readNonEmpty("Username (3-30 ky tu chu/so): ");
        String password = this.input.readNonEmpty("Password (>= 6 ky tu): ");
        String fullName = this.input.readNonEmpty("Ho ten: ");
        String email = this.input.readNonEmpty("Email: ");
        String phone = this.input.readString("Dien thoai: ");
        User user = this.mainController.handleRegister(username, password, fullName, email, phone);
        printSuccess("Dang ky thanh cong! Ma tai khoan: " + user.getId() + ". Hay dang nhap de mua ve.");
    }
}
