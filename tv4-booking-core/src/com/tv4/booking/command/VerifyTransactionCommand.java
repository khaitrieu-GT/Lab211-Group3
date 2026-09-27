package com.tv4.booking.command;

import com.tv4.booking.model.Transaction;

public class VerifyTransactionCommand implements Command {

    private final Transaction transaction;

    public VerifyTransactionCommand(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public void execute() {
        transaction.verify();
    }
}