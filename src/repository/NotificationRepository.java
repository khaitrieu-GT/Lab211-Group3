package repository;

import java.util.ArrayList;
import java.util.List;
import model.Notification;

public class NotificationRepository extends CsvRepository<Notification> {

    public NotificationRepository() {
        super("notifications.csv", "id,userId,message,createdAt,isRead", "N", 4);
    }

    @Override
    public Notification parse(String line) {
        return Notification.fromCsvLine(line);
    }

    public List<Notification> findByUserId(String userId) {
        List<Notification> result = new ArrayList<>();
        for (Notification n : this.findAll()) {
            if (n.getUserId().equalsIgnoreCase(userId)) {
                result.add(n);
            }
        }
        return result;
    }

    public void markAllAsRead(String userId) {
        synchronized (this.getLock()) {
            List<Notification> all = this.findAll();
            for (Notification n : all) {
                if (n.getUserId().equalsIgnoreCase(userId)) {
                    n.markAsRead();
                }
            }
            this.saveAll(all);
        }
    }
}
