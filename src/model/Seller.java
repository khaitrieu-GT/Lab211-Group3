package model;

import java.util.Arrays;
import java.util.List;
import model.enums.Role;
import model.enums.UserStatus;

/**
 * Nguoi ban ve (Khanh: Seller). Cac thao tac createMatch/updateMatch/deleteMatch/viewMatch,
 * createTicket/updateTicket/sellTicket duoc thuc hien qua controller.SellerController.
 */
public class Seller extends User {

    public Seller(String id, String username, String password, String fullName,
                  String email, String phone, String avatar, UserStatus status) {
        super(id, username, password, fullName, email, phone, avatar, status);
    }

    @Override
    public Role getRole() {
        return Role.SELLER;
    }

    @Override
    public List<String> getPermissions() {
        return Arrays.asList("Tao/sua/xoa tran dau cua minh", "Mo/dong ban ve", "Tao va dinh gia ve",
                "Ban ve tai quay", "Soat ve (check-in)", "Xem doanh thu ban hang");
    }
}
