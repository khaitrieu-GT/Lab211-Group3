package model;

import java.util.Arrays;
import java.util.List;
import model.enums.Role;
import model.enums.UserStatus;

/** Quan tri vien: toan quyen quan ly he thong. */
public class Admin extends User {

    public Admin(String id, String username, String password, String fullName,
                 String email, String phone, String avatar, UserStatus status) {
        super(id, username, password, fullName, email, phone, avatar, status);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

    @Override
    public List<String> getPermissions() {
        return Arrays.asList("Quan ly nguoi dung", "Quan ly san/khu vuc/ghe", "Quan ly doi bong",
                "Giam sat don hang/thanh toan", "Xu ly yeu cau huy ve", "Bao cao doanh thu", "Nhat ky he thong");
    }
}
