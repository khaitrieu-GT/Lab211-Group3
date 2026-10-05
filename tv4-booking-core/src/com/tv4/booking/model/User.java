package com.tv4.booking.model;

import com.tv4.booking.enums.*;
import java.time.LocalDateTime;

public class User {
    private final Long id;
    private String name;
    private String email;
    private String phone;
    private String avatar;
    private UserStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User(Long id, String name, String email, String phone, String avatar) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.avatar = avatar;
        this.status = UserStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;
    }

    public String getProfile() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + "', phone='" + phone + "', status=" + status
                + "}";
    }

    public void updateProfile(String name, String phone, String avatar) {
        if (status == UserStatus.DELETED) {
            throw new IllegalStateException("Deleted usser cannot update profile");
        }
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.avatar = avatar;
        updatedAt = LocalDateTime.now();
    }

    public void changePassword() {
        if (status != UserStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE user can change password");
        }
        updatedAt = LocalDateTime.now();
    }
}
