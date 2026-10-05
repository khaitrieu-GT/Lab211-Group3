package command;

import model.Transaction;

/** Xu ly giao dich cong thanh toan (Trieu). */
public class ProcessTransactionCommand implements Command {

    private final Transaction transaction;

    public ProcessTransactionCommand(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public void execute() {
        this.transaction.process();
    }
}