package command;

import model.SeatHold;

/** Gia han giu ghe (Trieu). */
public class ExtendHoldCommand implements Command {

    private final SeatHold hold;

    public ExtendHoldCommand(SeatHold hold) {
        this.hold = hold;
    }

    @Override
    public void execute() {
        this.hold.extendHold();
    }
}