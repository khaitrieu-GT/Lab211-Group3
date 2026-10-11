package view;

public class ReportView {
    public void displayUserDashboard(String username, String role) {
        System.out.println("\n========================================");
        System.out.println("           THONG TIN TAI KHOAN            ");
        System.out.println("\n========================================");
        System.out.println("Tai khoan: " + username);
        System.out.println("Vai tro: " + role);
        System.out.println("Trang thai: Hoat dong binh thuong");
        System.out.println("\n========================================");
    }

    public  void displayNotificationBanner(String message) {
        System.out.println("\n[THONG BAO HE THONG]: " + message);
    }
}
