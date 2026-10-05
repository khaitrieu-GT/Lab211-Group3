package controller;

import command.ChangePasswordCommand;
import command.UpdateProfileCommand;
import exception.BusinessException;
import model.User;
import repository.UserRepository;
import service.AuditService;

/**
 * Quan ly ho so ca nhan cho moi vai tro (Trieu: UpdateProfileCommand, ChangePasswordCommand).
 */
public class ProfileController {

    private final UserRepository userRepo = new UserRepository();
    private final AuditService auditService = new AuditService();

    /** Nap lai ban moi nhat tu file. */
    public User getProfile(String userId) {
        User user = this.userRepo.findById(userId);
        if (user == null) {
            throw new BusinessException("Khong tim thay nguoi dung " + userId);
        }
        return user;
    }

    public User updateProfile(String userId, String fullName, String email, String phone, String avatar) {
        User user = getProfile(userId);
        new UpdateProfileCommand(user, fullName, email, phone, avatar).execute();
        this.userRepo.update(user);
        this.auditService.log(user.getUsername(), "UPDATE_PROFILE", AuditService.SUCCESS);
        return user;
    }

    public void changePassword(String userId, String oldPassword, String newPassword) {
        User user = getProfile(userId);
        try {
            new ChangePasswordCommand(user, oldPassword, newPassword).execute();
        } catch (RuntimeException e) {
            this.auditService.log(user.getUsername(), "CHANGE_PASSWORD", AuditService.FAILED);
            throw e;
        }
        this.userRepo.update(user);
        this.auditService.log(user.getUsername(), "CHANGE_PASSWORD", AuditService.SUCCESS);
    }
}
