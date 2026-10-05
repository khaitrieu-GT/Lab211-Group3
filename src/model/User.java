package model;

import java.time.LocalDateTime;
import java.util.List;
import model.enums.Role;
import model.enums.UserStatus;
import util.CsvUtil;
import util.PasswordUtil;

/**
 * Tai khoan nguoi dung - hop nhat User cua Nhat, Minh, Khoa, Trieu.
 * Lop truu tuong: moi vai tro (Admin, Seller, Buyer) la mot lop con.
 */
public abstract class User extends BaseEntity {
    private String username;
    private String password;       // Luu dang SHA-256 (Minh: passwordHash)
    private String fullName;
    private String email;
    private String phone;
    private String avatar;
    private UserStatus status;
    private int version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected User() {
        super();
    }

    protected User(String id, String username, String password, String fullName,
                   String email, String phone, String avatar, UserStatus status) {
        super(id);
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.avatar = avatar;
        this.status = status == null ? UserStatus.ACTIVE : status;
        this.version = 1;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /** Tao dung lop con theo vai tro (Factory Method). */
    public static User create(Role role, String id, String username, String password, String fullName,
                              String email, String phone, String avatar, UserStatus status) {
        switch (role) {
            case ADMIN:
                return new Admin(id, username, password, fullName, email, phone, avatar, status);
            case SELLER:
                return new Seller(id, username, password, fullName, email, phone, avatar, status);
            default:
                return new Buyer(id, username, password, fullName, email, phone, avatar, status);
        }
    }

    public abstract Role getRole();

    /** Danh sach quyen cua vai tro (phuc vu phan quyen va hien thi ho so). */
    public abstract List<String> getPermissions();

    /** Kiem tra du lieu dang ky hop le (Minh: register). */
    public boolean register() {
        if (this.username == null || !this.username.matches("[A-Za-z0-9_.]{3,30}")) {
            return false;
        }
        if (this.password == null || this.password.isEmpty()) {
            return false;
        }
        if (this.fullName == null || this.fullName.trim().isEmpty()) {
            return false;
        }
        if (this.email == null || !this.email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            return false;
        }
        this.status = UserStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        return true;
    }

    /** Kiem tra thong tin dang nhap (Minh: login). */
    public boolean login(String username, String rawPassword) {
        return this.username.equalsIgnoreCase(username) && PasswordUtil.matches(rawPassword, this.password);
    }

    /** Thong tin ho so dang chuoi (Trieu: getProfile). */
    public String getProfile() {
        return "User{id=" + getId() + ", username='" + this.username + "', name='" + this.fullName
                + "', email='" + this.email + "', phone='" + this.phone + "', role=" + getRole()
                + ", status=" + this.status + "}";
    }

    /** Cap nhat ho so (Trieu + Minh: updateProfile). */
    public void updateProfile(String fullName, String email, String phone, String avatar) {
        if (this.status == UserStatus.DELETED) {
            throw new IllegalStateException("Deleted user cannot update profile");
        }
        if (fullName != null && !fullName.trim().isEmpty()) {
            this.fullName = fullName.trim();
        }
        if (email != null && !email.trim().isEmpty()) {
            if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
                throw new IllegalArgumentException("Email khong hop le");
            }
            this.email = email.trim();
        }
        if (phone != null && !phone.trim().isEmpty()) {
            this.phone = phone.trim();
        }
        if (avatar != null && !avatar.trim().isEmpty()) {
            this.avatar = avatar.trim();
        }
        this.version++;
        this.updatedAt = LocalDateTime.now();
    }

    /** Doi mat khau (Trieu: changePassword). */
    public void changePassword(String oldRawPassword, String newRawPassword) {
        if (this.status != UserStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE user can change password");
        }
        if (!PasswordUtil.matches(oldRawPassword, this.password)) {
            throw new IllegalArgumentException("Mat khau cu khong dung");
        }
        if (newRawPassword == null || newRawPassword.length() < 6) {
            throw new IllegalArgumentException("Mat khau moi phai co it nhat 6 ky tu");
        }
        this.password = PasswordUtil.hash(newRawPassword);
        this.version++;
        this.updatedAt = LocalDateTime.now();
    }

    /** Admin khoa tai khoan. */
    public void lock() {
        if (this.status == UserStatus.DELETED) {
            throw new IllegalStateException("Deleted user cannot be locked");
        }
        this.status = UserStatus.LOCKED;
        this.version++;
        this.updatedAt = LocalDateTime.now();
    }

    /** Admin mo khoa tai khoan. */
    public void unlock() {
        if (this.status == UserStatus.DELETED) {
            throw new IllegalStateException("Deleted user cannot be unlocked");
        }
        this.status = UserStatus.ACTIVE;
        this.version++;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public String getUsername() { return this.username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return this.fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return this.phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatar() { return this.avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public UserStatus getStatus() { return this.status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public int getVersion() { return this.version; }
    public void setVersion(int version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return this.createdAt; }
    public LocalDateTime getUpdatedAt() { return this.updatedAt; }

    @Override
    public String toCsvLine() {
        return CsvUtil.join(getId(), this.username, this.password, this.fullName, this.email, this.phone,
                this.avatar, getRole(), this.status, this.version, this.createdAt, this.updatedAt);
    }

    public static User fromCsvLine(String csvLine) {
        String[] p = CsvUtil.split(csvLine);
        if (p.length < 12) {
            return null;
        }
        User user = create(CsvUtil.toEnum(Role.class, p[7]), p[0], p[1], p[2], p[3], p[4], p[5], p[6],
                CsvUtil.toEnum(UserStatus.class, p[8]));
        user.version = CsvUtil.toInt(p[9]);
        user.createdAt = CsvUtil.toDateTime(p[10]);
        user.updatedAt = CsvUtil.toDateTime(p[11]);
        return user;
    }

    @Override
    public String toString() {
        return String.format("%-6s | %-12s | %-22s | %-24s | %-11s | %-6s | %s",
                getId(), this.username, this.fullName, this.email, this.phone, getRole(), this.status);
    }
}
