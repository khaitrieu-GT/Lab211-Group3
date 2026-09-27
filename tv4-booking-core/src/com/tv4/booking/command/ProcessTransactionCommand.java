package com.tv4.booking.command;

import com.tv4.booking.model.Transaction;

public class ProcessTransactionCommand implements Command {

    private final Transaction transaction;

    public ProcessTransactionCommand(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public void execute() {
        transaction.process();
    }
}