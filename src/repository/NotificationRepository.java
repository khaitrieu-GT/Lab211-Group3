package repository;

import java.util.ArrayList;
import java.util.List;
import model.Notification;

public class NotificationRepository extends CsvRepository<Notification> {

    public NotificationRepository() {
        super("notifications.csv", "id,userId,message,createdAt,isRead", "N", 4);
    }

    @Override
    protected Notification parse(String line) {
        return Notification.fromCsvLine(line);
    }

    public List<Notification> findByUserId(String userId) {
        List<Notification> result = new ArrayList<>();
        for (Notification n : findAll()) {
            if (n.getUserId().equalsIgnoreCase(userId)) {
                result.add(n);
            }
        }
        return result;
    }

    public void markAllAsRead(String userId) {
        synchronized (this.lock) {
            List<Notification> all = findAll();
            for (Notification n : all) {
                if (n.getUserId().equalsIgnoreCase(userId)) {
                    n.markAsRead();
                }
            }
            saveAll(all);
        }
    }
}
