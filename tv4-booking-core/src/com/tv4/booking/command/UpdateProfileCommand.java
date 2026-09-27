package com.tv4.booking.command;

import com.tv4.booking.model.User;

public class UpdateProfileCommand implements Command {

    private final User user;
    private final String name;
    private final String email;
    private final String phone;
    private final String avatar;

    public UpdateProfileCommand(
            User user,
            String name,
            String email,
            String phone,
            String avatar
    ) {
        this.user = user;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.avatar = avatar;
    }

    @Override
    public void execute() {
        user.updateProfile(name, email, phone);
    }
}