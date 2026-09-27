package com.tv4.booking.command;

import com.tv4.booking.model.User;

public class ChangePasswordCommand implements Command {

    private final User user;

    public ChangePasswordCommand(User user) {
        this.user = user;
    }

    @Override
    public void execute() {
        user.changePassword();
    }
}