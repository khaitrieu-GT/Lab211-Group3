package service;

import exception.BusinessException;
import model.User;
import model.enums.Role;
import model.enums.UserStatus;
import repository.UserRepository;
import util.PasswordUtil;
import util.ValidationUtil;

/**
 * Tao tai khoan moi - dung chung cho Buyer tu dang ky va Admin tao tai khoan Seller.
 */
public class AccountService {

    private final UserRepository userRepo = new UserRepository();

    /** Username va email khong duoc trung, mat khau >= 6 ky tu, so dien thoai hop le. */
    public User createAccount(Role role, String username, String password, String fullName,
                              String email, String phone, String avatar) {
        if (this.userRepo.findByUsername(username) != null) {
            throw new BusinessException("Ten dang nhap da ton tai.");
        }
        if (!ValidationUtil.isBlank(email) && this.userRepo.findByEmail(email) != null) {
            throw new BusinessException("Email da duoc tai khoan khac su dung.");
        }
        if (password == null || password.length() < 6) {
            throw new BusinessException("Mat khau phai co it nhat 6 ky tu.");
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            throw new BusinessException("So dien thoai phai gom 10 chu so va bat dau bang 0.");
        }
        User user = User.create(role, null, username.trim(), PasswordUtil.hash(password),
                fullName == null ? null : fullName.trim(), email == null ? null : email.trim(),
                phone == null ? "" : phone.trim(), avatar, UserStatus.ACTIVE);
        if (!user.register()) {
            throw new BusinessException("Thong tin khong hop le (username 3-30 ky tu chu/so, email hop le).");
        }
        this.userRepo.insert(user);
        return user;
    }
}
