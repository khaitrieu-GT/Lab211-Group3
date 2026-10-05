package model;

public class User extends BaseEntity {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String role;   // ADMIN, SELLER, BUYER
    private String status; // ACTIVE, LOCKED

    public User() {
        super();
    }

    public User(String id, String username, String password, String fullName, String email, String role, String status) {
        super(id);
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public String getUsername() { return this.username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return this.fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return this.role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return this.status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toCsvLine() {
        return String.join(",", this.getId(), this.username, this.password, this.fullName, this.email, this.role, this.status);
    }

    public static User fromCsvLine(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length < 7) return null;
        return new User(
            parts[0].trim(),
            parts[1].trim(),
            parts[2].trim(),
            parts[3].trim(),
            parts[4].trim(),
            parts[5].trim(),
            parts[6].trim()
        );
    }
}