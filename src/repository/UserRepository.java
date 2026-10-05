package repository;

import java.util.ArrayList;
import java.util.List;
import model.User;
import model.enums.Role;
import model.enums.UserStatus;

/** Nhat: UserRepository. */
public class UserRepository extends CsvRepository<User> {

    public UserRepository() {
        super("users.csv", "id,username,password,fullName,email,phone,avatar,role,status,version,createdAt,updatedAt", "U", 3);
    }

    @Override
    protected User parse(String line) {
        return User.fromCsvLine(line);
    }

    public User findByUsername(String username) {
        for (User u : findAll()) {
            if (u.getUsername().equalsIgnoreCase(username.trim())) {
                return u;
            }
        }
        return null;
    }

    public List<User> findByRole(Role role) {
        List<User> result = new ArrayList<>();
        for (User u : findAll()) {
            if (u.getRole() == role) {
                result.add(u);
            }
        }
        return result;
    }

    /** Nhat: updateUserStatus. */
    public boolean updateUserStatus(String userId, UserStatus newStatus) {
        synchronized (this.lock) {
            User user = findById(userId);
            if (user == null) {
                return false;
            }
            if (newStatus == UserStatus.LOCKED) {
                user.lock();
            } else if (newStatus == UserStatus.ACTIVE) {
                user.unlock();
            } else {
                user.setStatus(newStatus);
            }
            return update(user);
        }
    }
}
