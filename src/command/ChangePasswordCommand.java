package command;

import model.User;

/** Doi mat khau (Trieu). */
public class ChangePasswordCommand implements Command {

    private final User user;
    private final String oldPassword;
    private final String newPassword;

    public ChangePasswordCommand(User user, String oldPassword, String newPassword) {
        this.user = user;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    @Override
    public void execute() {
        this.user.changePassword(this.oldPassword, this.newPassword);
    }
}
