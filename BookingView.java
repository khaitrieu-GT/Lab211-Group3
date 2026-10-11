package view;
import controller.MainController;
import java.util.Scanner;

public class BookingView {
    private MainController controller;
    private Scanner scanner;
    private SeatMapView seatMapView;

    public BookingView(MainController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
        this.seatMapView = new SeatMapView();
    }

    public void showBookingMenu(String currentUserId) {
        int choice = 0;
        do {
            System.out.println("\n===== MENU DAT VE SAN VAN DONG =====");
            System.out.println("1. Xem so do ghe (Seat Map)");
            System.out.println("2. Tien hanh chon ghe va dat ve");
            System.out.println("3. Quay lai menu chinh");
            System.out.print("Nhap lua chon cua ban: ");

            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine();
            } else {
                scanner.nextLine();
                System.out.println("Vui long nhap so hop le!");
                continue;
            }

            switch (choice) {
                case 1:
                    seatMapView.displaySeatMap();
                    break;
                case 2:
                    handleBookingProcess(currentUserId);
                    break;
                case 3:
                    System.out.println("Quay lai menu chinh");
                    break;
                default:
                    System.out.println("Lua chon khong hop le. Vui long chon lai!");
            }
        } while (choice != 3); 
    }

    private void handleBookingProcess(String userId) {
        seatMapView.displaySeatMap();
        System.out.print("Nhap so hang ghe ban muon chon (VD: 1, 2, 3): ");
        int row = scanner.nextInt();
        System.out.print("Nhap so ghe (VD: 1 den 5): ");
        int seatNum = scanner.nextInt();
        scanner.nextLine();

        System.out.println("-> [View] Dang gui yeu cau dat ghe [Hang " + row + ", Ghe " + seatNum + "] xuong Controller...");

        controller.triggerNotification(userId, "Ban da dat thanh cong ghe tai Hang " + row + " - Ghe " + seatNum);
    }
}
