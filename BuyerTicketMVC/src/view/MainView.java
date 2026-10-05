package view;

import java.util.Scanner;

public class MainView {
    private Scanner scanner;
    public MainView(Scanner scanner) { this.scanner = scanner; }

    public void showMenu() {
        System.out.println("\n========================================");
        System.out.println("          BUYER TICKET SYSTEM");
        System.out.println("========================================");
        System.out.println("1. Search Match");
        System.out.println("2. View Match Information");
        System.out.println("3. View Stadium Map");
        System.out.println("4. Select Seat & Create Order");
        System.out.println("5. Purchase History");
        System.out.println("6. View E-Ticket");
        System.out.println("7. Cancel Ticket");
        System.out.println("0. Exit");
        System.out.println("========================================");
    }

    public int readInt(String message) {
        while (true) {
            try {
                System.out.print(message);
                int value = Integer.parseInt(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    public String readString(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }
}
