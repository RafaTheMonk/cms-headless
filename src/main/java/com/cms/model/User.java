package com.cms.model;

import com.cms.model.enums.UserRole;
import com.cms.server.JsonUtil;
import java.time.Instant;
import java.util.UUID;

public class User {

    private String id;
    private String fullName;
    private String email;
    private String passwordHash;
    private UserRole role;
    private String avatarUrl;
    private String bio;
    private boolean isActive;
    private String lastLoginAt;
    private String createdAt;
    private String updatedAt;

    public User() {
        this.id        = UUID.randomUUID().toString();
        this.role      = UserRole.viewer;
        this.isActive  = true;
        this.createdAt = Instant.now().toString();
        this.updatedAt = this.createdAt;
    }

    public User(String fullName, String email, String passwordHash) {
        this();
        this.fullName     = fullName;
        this.email        = email;
        this.passwordHash = passwordHash;
    }

    public String toJson() {
        return "{"
            + "\"id\":"          + q(id)           + ","
            + "\"fullName\":"    + q(fullName)      + ","
            + "\"email\":"       + q(email)         + ","
            + "\"role\":"        + q(role)          + ","
            + "\"avatarUrl\":"   + q(avatarUrl)     + ","
            + "\"bio\":"         + q(bio)           + ","
            + "\"isActive\":"    + isActive         + ","
            + "\"lastLoginAt\":" + q(lastLoginAt)   + ","
            + "\"createdAt\":"   + q(createdAt)     + ","
            + "\"updatedAt\":"   + q(updatedAt)
            + "}";
    }

    private static String q(Object v) {
        if (v == null) return "null";
        return "\"" + JsonUtil.escapeJson(v.toString()) + "\"";
    }

    // Getters and Setters
    public String getId()                        { return id; }
    public void setId(String id)                 { this.id = id; }

    public String getFullName()                  { return fullName; }
    public void setFullName(String fullName)     { this.fullName = fullName; }

    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }

    public String getPasswordHash()              { return passwordHash; }
    public void setPasswordHash(String h)        { this.passwordHash = h; }

    public UserRole getRole()                    { return role; }
    public void setRole(UserRole role)           { this.role = role; }

    public String getAvatarUrl()                 { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl)   { this.avatarUrl = avatarUrl; }

    public String getBio()                       { return bio; }
    public void setBio(String bio)               { this.bio = bio; }

    public boolean isActive()                    { return isActive; }
    public void setActive(boolean active)        { this.isActive = active; }

    public String getLastLoginAt()               { return lastLoginAt; }
    public void setLastLoginAt(String t)         { this.lastLoginAt = t; }

    public String getCreatedAt()                 { return createdAt; }

    public String getUpdatedAt()                 { return updatedAt; }
    public void setUpdatedAt(String updatedAt)   { this.updatedAt = updatedAt; }
}
