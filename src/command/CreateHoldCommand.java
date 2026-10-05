package command;

import model.SeatHold;

/** Tao phien giu ghe (Trieu). */
public class CreateHoldCommand implements Command {

    private final SeatHold hold;

    public CreateHoldCommand(SeatHold hold) {
        this.hold = hold;
    }

    @Override
    public void execute() {
        this.hold.createHold();
    }
}