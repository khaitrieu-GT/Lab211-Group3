package command;

import model.User;

/** Cap nhat ho so (Trieu - da sua loi truyen sai tham so). */
public class UpdateProfileCommand implements Command {

    private final User user;
    private final String name;
    private final String email;
    private final String phone;
    private final String avatar;

    public UpdateProfileCommand(User user, String name, String email, String phone, String avatar) {
        this.user = user;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.avatar = avatar;
    }

    @Override
    public void execute() {
        this.user.updateProfile(this.name, this.email, this.phone, this.avatar);
    }
}
