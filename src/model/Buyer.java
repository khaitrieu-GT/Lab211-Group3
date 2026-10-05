package model;

import java.util.Arrays;
import java.util.List;
import model.enums.Role;
import model.enums.UserStatus;

/** Nguoi mua ve (Khoa: User trong BuyerTicketMVC). */
public class Buyer extends User {

    public Buyer(String id, String username, String password, String fullName,
                 String email, String phone, String avatar, UserStatus status) {
        super(id, username, password, fullName, email, phone, avatar, status);
    }

    @Override
    public Role getRole() {
        return Role.BUYER;
    }

    @Override
    public List<String> getPermissions() {
        return Arrays.asList("Tim kiem tran dau", "Xem so do san", "Chon/giu ghe", "Dat ve va thanh toan",
                "Xem ve dien tu", "Xem lich su mua", "Yeu cau huy ve");
    }
}
