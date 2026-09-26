import controller.CancellationController;
import controller.MatchController;
import controller.OrderController;
import controller.SeatController;
import controller.TicketController;
import model.Match;
import model.Seat;
import model.SeatSection;
import model.Stadium;
import model.Team;
import model.User;
import view.MainView;
import view.MatchView;
import view.OrderView;
import view.SeatView;
import view.TicketView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        User buyer = createBuyer();
        Match match = createMatch();
        List<Match> matches = new ArrayList<>();
        matches.add(match);

        MainView mainView = new MainView(scanner);
        MatchView matchView = new MatchView();
        SeatView seatView = new SeatView();
        OrderView orderView = new OrderView();
        TicketView ticketView = new TicketView();

        MatchController matchController = new MatchController(matches, matchView);
        SeatController seatController = new SeatController(seatView);
        OrderController orderController = new OrderController(orderView);
        TicketController ticketController = new TicketController(ticketView);
        CancellationController cancellationController = new CancellationController(ticketView);

        int choice;
        do {
            mainView.showMenu();
            choice = mainView.readInt("Enter choice: ");

            switch (choice) {
                case 1:
                    int searchType = mainView.readInt("1. Search by name\n2. Search by date\nChoice: ");
                    if (searchType == 1) {
                        String keyword = mainView.readString("Enter match name: ");
                        matchController.searchByName(keyword);
                    } else if (searchType == 2) {
                        String dateText = mainView.readString("Enter date (yyyy-MM-dd): ");
                        try {
                            matchController.searchByDate(LocalDate.parse(dateText));
                        } catch (Exception e) {
                            System.out.println("Invalid date format.");
                        }
                    } else {
                        System.out.println("Invalid search option.");
                    }
                    break;

                case 2:
                    matchController.viewMatch(match.getId());
                    break;

                case 3:
                    seatController.viewSeatMap(match);
                    break;

                case 4:
                    seatController.viewSeatMap(match);
                    String row = mainView.readString("Enter row: ");
                    String number = mainView.readString("Enter seat number: ");
                    Seat seat = seatController.selectSeat(match, row, number);
                    if (seat != null) {
                        model.Order order = orderController.createOrder(buyer, match, seat);
                        String confirm = mainView.readString("Confirm order? (Y/N): ");
                        if ("Y".equalsIgnoreCase(confirm)) {
                            orderController.confirmOrder(order);
                            ticketController.createTicket(order);
                            ticketController.viewTicket();
                        } else {
                            seatController.unselectSeat(seat);
                            System.out.println("Order cancelled.");
                        }
                    }
                    break;

                case 5:
                    orderController.viewPurchaseHistory(buyer);
                    break;

                case 6:
                    ticketController.viewTicket();
                    break;

                case 7:
                    if (ticketController.getLatestTicket() == null) {
                        System.out.println("No ticket found.");
                    } else {
                        String reason = mainView.readString("Enter cancellation reason: ");
                        cancellationController.createRequest(ticketController.getLatestTicket(), reason);
                    }
                    break;

                case 0:
                    System.out.println("Program ended.");
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        } while (choice != 0);

        scanner.close();
    }

    private static User createBuyer() {
        return new User(1L, "Dang Dang Khoa", "khoa@gmail.com", "123456", "0901234567", "avatar.jpg");
    }

    private static Match createMatch() {
        Team team1 = new Team(1L, "Manchester United", "mu.png", "England");
        Team team2 = new Team(2L, "Liverpool", "liverpool.png", "England");

        Stadium stadium = new Stadium(1L, "Old Trafford", "Sir Matt Busby Way", "Manchester");
        SeatSection sectionA = new SeatSection(1L, "A", 700000, 20);
        SeatSection vip = new SeatSection(2L, "VIP", 1000000, 10);

        sectionA.addSeat(new Seat(1L, "A", "01", 700000));
        sectionA.addSeat(new Seat(2L, "A", "02", 700000));
        sectionA.addSeat(new Seat(3L, "A", "03", 700000));
        vip.addSeat(new Seat(4L, "V", "01", 1000000));
        vip.addSeat(new Seat(5L, "V", "02", 1000000));

        stadium.addSection(sectionA);
        stadium.addSection(vip);

        return new Match(1L, "MU vs Liverpool", "Premier League", LocalDate.of(2026, 10, 10),
                LocalTime.of(19, 30), team1, team2, stadium);
    }
}
