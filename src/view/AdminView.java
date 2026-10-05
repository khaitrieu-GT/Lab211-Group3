package view;

import controller.AdminController;
import model.Stadium;
import model.User;
import java.util.List;
import java.util.Scanner;

public class AdminView {
    private final AdminController controller;
    private final Scanner scanner;

    public AdminView() {
        this.controller = new AdminController();
        this.scanner = new Scanner(System.in);
    }

    public void displayMenu() {
        while (true) {
            System.out.println("\n==================================================");
            System.out.println("              HE THONG QUAN LY (ADMIN)");
            System.out.println("==================================================");
            System.out.println("1. Xem danh sach nguoi dung & Seller");
            System.out.println("2. Khoa / Mo khoa tai khoan nguoi dung");
            System.out.println("3. Xem danh sach san van dong");
            System.out.println("4. Them san van dong moi");
            System.out.println("5. Quan ly trang thai ghe (Khoa/Mo bao tri)");
            System.out.println("0. Thoat");
            System.out.println("==================================================");
            System.out.print("Lua chon cua ban: ");

            int choice = Integer.parseInt(this.scanner.nextLine().trim());
            switch (choice) {
                case 1:
                    this.renderUserList();
                    break;
                case 2:
                    this.handleUserLock();
                    break;
                case 3:
                    this.renderStadiumList();
                    break;
                case 4:
                    this.handleAddStadium();
                    break;
                case 5:
                    this.handleSeatMaintenance();
                    break;
                case 0:
                    System.out.println("Da thoat giao dien Admin.");
                    return;
                default:
                    System.out.println("Lua chon khong hop le!");
                    break;
            }
        }
    }

    private void renderUserList() {
        List users = this.controller.getAllUsers();
        System.out.println("\n--- DANH SACH NGUOI DUNG ---");
        // Ép kiểu tường minh (User) cho JDK 8 & 17
        for (Object obj : users) {
            User u = (User) obj;
            System.out.printf("ID: %s | User: %s | Role: %s | Status: %s\n",
                    u.getId(), u.getUsername(), u.getRole(), u.getStatus());
        }
    }

    private void handleUserLock() {
        System.out.print("Nhap User ID can doi trang thai: ");
        String id = this.scanner.nextLine().trim();
        System.out.print("Nhap trang thai moi (ACTIVE / LOCKED): ");
        String status = this.scanner.nextLine().trim();

        if (this.controller.changeUserStatus(id, status)) {
            System.out.println("Cap nhat trang thai User thanh cong!");
        } else {
            System.out.println("That bai! Khong tim thay User ID.");
        }
    }

    private void renderStadiumList() {
        List stadiums = this.controller.getAllStadiums();
        System.out.println("\n--- DANH SACH SAN VAN DONG ---");
        // ĐÃ SỬA LỖI DÒNG 86: Ép kiểu tường minh (Stadium) từ Object
        for (Object obj : stadiums) {
            Stadium s = (Stadium) obj;
            System.out.printf("ID: %s | Ten: %s | Dia chi: %s | Suc chua: %d\n",
                    s.getId(), s.getName(), s.getAddress(), s.getCapacity());
        }
    }

    private void handleAddStadium() {
        System.out.print("Nhap Stadium ID: ");
        String id = this.scanner.nextLine().trim();
        System.out.print("Nhap Ten san: ");
        String name = this.scanner.nextLine().trim();
        System.out.print("Nhap Dia chi: ");
        String address = this.scanner.nextLine().trim();
        System.out.print("Nhap Suc chua: ");
        int cap = Integer.parseInt(this.scanner.nextLine().trim());

        if (this.controller.createStadium(id, name, address, cap)) {
            System.out.println("Them San van dong thanh cong!");
        } else {
            System.out.println("Them San van dong that bai.");
        }
    }

    private void handleSeatMaintenance() {
        System.out.print("Nhap Seat ID: ");
        String seatId = this.scanner.nextLine().trim();
        System.out.print("Khoa bao tri? (true = MAINTENANCE / false = AVAILABLE): ");
        boolean isLock = Boolean.parseBoolean(this.scanner.nextLine().trim());

        if (this.controller.updateSeatMaintenance(seatId, isLock)) {
            System.out.println("Cap nhat trang thai bao tri ghe thanh cong!");
        } else {
            System.out.println("Cap nhat that bai.");
        }
    }
}