package view;
import controller.MainController;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        MainController controller = new MainController();
        BookingView bookingView = new BookingView(controller);
        ReportView reportView = new ReportView();

        System.out.println("==========================================");
        System.out.println(" CHAO MUNG DEN VOI STADIUM TICKET BOOKING ");
        System.out.println("==========================================");
        
        System.out.print("Nhap ten dang nhap cua nguoi dung: ");
        String user = scanner.nextLine();
        System.out.print("Nhap mat khau: ");
        String pass = scanner.nextLine();

        boolean loginResult = controller.handleLogin(user, pass);

        if (loginResult) {
            reportView.displayUserDashboard(user, "Buyer/Fan");
            controller.triggerNotification("U001", "Chao mung " + user + " da dang nhap he thong!");

            bookingView.showBookingMenu("U001");
        } else {
            System.out.println("Dang nhap that bai. Chuong trinh ket thuc.");
        }
        scanner.close();
    }
}