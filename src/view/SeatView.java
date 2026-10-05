package view;

import dto.SeatMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import model.BookingSeat;
import model.Seat;
import model.Section;
import model.Stadium;
import model.Ticket;
import model.enums.TicketStatus;
import util.MoneyUtil;

/** So do san (Khoa: SeatView). */
public class SeatView extends BaseView {

    /** Khoa: displaySeatMap. */
    public void displaySeatMap(SeatMap map) {
        Stadium stadium = map.getStadium();
        printSection("SO DO SAN - " + map.getMatch().getName());
        System.out.println("San: " + stadium.getName() + " | " + stadium.getAddress() + ", " + stadium.getCity());
        System.out.println("Chu thich: [A1 ] trong   [A1 H] dang giu   [A1 X] da ban   [A1 -] khong ban/khoa");
        for (Section section : stadium.getSections()) {
            System.out.println();
            System.out.println("--- Khu " + section.getId() + ": " + section.getName() + " (" + section.getType() + ")"
                    + " | Gia goc " + MoneyUtil.format(section.getBasePrice())
                    + (section.isActive() ? "" : " | DONG"));
            Map<String, List<Seat>> rows = new LinkedHashMap<>();
            for (Seat seat : section.getSeats()) {
                if (!rows.containsKey(seat.getRowNumber())) {
                    rows.put(seat.getRowNumber(), new ArrayList<>());
                }
                rows.get(seat.getRowNumber()).add(seat);
            }
            int available = 0;
            for (Map.Entry<String, List<Seat>> row : rows.entrySet()) {
                StringBuilder line = new StringBuilder("  Hang " + row.getKey() + ": ");
                for (Seat seat : row.getValue()) {
                    Ticket ticket = map.getTicket(seat);
                    String mark = symbol(seat, ticket, section);
                    if (mark.equals(" ")) {
                        available++;
                    }
                    line.append(String.format("[%-3s%s] ", seat.getLabel(), mark));
                }
                System.out.println(line);
            }
            System.out.println("  Con trong: " + available + " ghe");
        }
    }

    /** Khoa: displaySelectionResult. */
    public void displaySelectionResult(BookingSeat item) {
        printSuccess("Da giu ghe " + item.getSeatLabel() + " (khu " + item.getSectionId() + ") - "
                + MoneyUtil.format(item.getPrice()));
    }

    private String symbol(Seat seat, Ticket ticket, Section section) {
        if (ticket == null || !seat.isAvailable() || !section.isActive()) {
            return "-";
        }
        if (ticket.getStatus() == TicketStatus.SOLD) {
            return "X";
        }
        if (ticket.getStatus() == TicketStatus.HOLD) {
            return "H";
        }
        return ticket.getStatus() == TicketStatus.AVAILABLE ? " " : "-";
    }
}
