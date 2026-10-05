package command;

import model.Transaction;

/** Xac minh giao dich thanh cong (Trieu). */
public class VerifyTransactionCommand implements Command {

    private final Transaction transaction;

    public VerifyTransactionCommand(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public void execute() {
        this.transaction.verify();
    }
}