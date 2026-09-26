package controller;

import model.Match;
import model.Seat;
import model.SeatSection;
import view.SeatView;

public class SeatController {
    private SeatView view;
    public SeatController(SeatView view) { this.view = view; }

    public void viewSeatMap(Match match) { view.displaySeatMap(match.getStadium()); }

    public Seat selectSeat(Match match, String row, String number) {
        for (SeatSection section : match.getStadium().getSections()) {
            for (Seat seat : section.getSeats()) {
                if (seat.getRow().equalsIgnoreCase(row) && seat.getNumber().equalsIgnoreCase(number)) {
                    boolean success = seat.select();
                    view.displaySelectionResult(success);
                    return success ? seat : null;
                }
            }
        }
        System.out.println("Seat not found.");
        return null;
    }

    public void unselectSeat(Seat seat) {
        if (seat != null) {
            seat.unselect();
            System.out.println("Seat unselected.");
        }
    }
}
