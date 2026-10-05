package model;

public class User {
    private String userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String role;
    private int version;

    
    public User(String userId, String username, String passwordHash, String fullName, String email, String role, int version) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.version = version;
    }

    public boolean register() { return true;}
    public boolean login(String username, String pass) { return true;}
    public void updateProfile() {}

    public String toCsvLine() {
        return userId +","+ username +","+ passwordHash +","+ fullName +","+ email +","+ role +","+ version;
    }

    public static User fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < 7) return null;
        return new User(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], Integer.parseInt(parts[6]));
    }
    public String getUserId() {return userId;}
    public String getUsername() {return username;}
    public String getRole() {return role;}
}