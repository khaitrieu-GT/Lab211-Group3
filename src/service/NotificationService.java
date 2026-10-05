package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Notification;
import model.User;
import model.enums.Role;
import repository.NotificationRepository;
import repository.UserRepository;

/** Gui va doc thong bao (Minh: Notification + MainController.triggerNotification). */
public class NotificationService {

    private final NotificationRepository notificationRepo = new NotificationRepository();
    private final UserRepository userRepo = new UserRepository();

    public Notification send(String userId, String message) {
        Notification notification = new Notification(null, userId, message, null, false);
        notification.sendNotification();
        return this.notificationRepo.insert(notification);
    }

    /** Gui cho tat ca nguoi dung thuoc mot vai tro (vd: bao Admin co yeu cau huy ve). */
    public void sendToRole(Role role, String message) {
        for (User user : this.userRepo.findByRole(role)) {
            send(user.getId(), message);
        }
    }

    /** Thong bao moi nhat len dau. */
    public List<Notification> getByUser(String userId) {
        List<Notification> list = new ArrayList<>(this.notificationRepo.findByUserId(userId));
        Collections.reverse(list);
        return list;
    }

    public int countUnread(String userId) {
        int count = 0;
        for (Notification n : this.notificationRepo.findByUserId(userId)) {
            if (!n.isRead()) {
                count++;
            }
        }
        return count;
    }

    public void markAllAsRead(String userId) {
        this.notificationRepo.markAllAsRead(userId);
    }
}
