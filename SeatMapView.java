package view;

public class SeatMapView {
    public void displaySeatMap() {
        System.out.println("\n---SO DO GHE NGOI (KHOA VIP) ---");
        System.out.println("Chu thich: [A] = Available (Trong), [L] = Locked (Dang chon), [B] = Booked (Da dat)");

        for (int row = 1; row <=3; row++) {
            System.out.print("Hang " + row + ":");
            for (int seatNum = 1; seatNum <= 5; seatNum++) {
                String status = "[A]";
                if (row == 1 && seatNum == 2) status = "[B]";
                if (row == 2 && seatNum == 4) status = "[L]";

                System.out.print(status + " ");
            }
            System.out.println();
        }
        System.out.println("----------------------------------");
    }
}
