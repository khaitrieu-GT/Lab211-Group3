package repository;

import model.User;
import java.io.*;
import java.util.*;

public class UserRepository {
    private final String filePath = "data/users.csv";

    public UserRepository() {}

    public List findAll() {
        List list = new ArrayList();
        File file = new File(this.filePath);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("id")) continue;
                User u = User.fromCsvLine(line);
                if (u != null) list.add(u);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean saveAll(List users) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.filePath))) {
            bw.write("id,username,password,fullName,email,role,status");
            bw.newLine();
            for (User u : users) {
                bw.write(u.toCsvLine());
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User findById(String id) {
        List users = this.findAll();
        for (User u : users) {
            if (u.getId().equalsIgnoreCase(id)) {
                return u;
            }
        }
        return null;
    }

    public boolean updateUserStatus(String userId, String newStatus) {
        List users = this.findAll();
        boolean found = false;
        for (User u : users) {
            if (u.getId().equalsIgnoreCase(userId)) {
                u.setStatus(newStatus);
                found = true;
                break;
            }
        }
        return found && this.saveAll(users);
    }
}