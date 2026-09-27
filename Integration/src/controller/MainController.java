package controller;

import model.User;
import model.Notification;
import model.AuditLog;
import java.time.LocalDateTime;

public class MainController {

    public boolean handleLogin(String username, String password) {
        if (username.equals("admin") && password.equals("123456")) {
            System.out.println("-> [Controller] Dang nhap thanh cong cho tai khoan: " + username);
            return true;
        }
        System.out.println("-> [Controller] Sai thong tin dang nhap!");
        return false;
    }

    public void triggerNotification(String userId, String message) {
        Notification notif = new Notification("N001", userId, message, LocalDateTime.now(), false);
        notif.sendNotification();
        System.out.println("-> [Controller] da gui thong bao toi user: " + userId);
    }
}