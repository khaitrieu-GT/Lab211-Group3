package command;

import model.SeatHold;

/** Huy phien giu ghe (Trieu). */
public class ReleaseHoldCommand implements Command {

    private final SeatHold hold;

    public ReleaseHoldCommand(SeatHold hold) {
        this.hold = hold;
    }

    @Override
    public void execute() {
        this.hold.releaseHold();
    }
}