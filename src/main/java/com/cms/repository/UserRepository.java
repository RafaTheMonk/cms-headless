package com.cms.repository;

import com.cms.db.Database;
import com.cms.model.User;
import com.cms.model.enums.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, full_name, email, password_hash, role, avatar_url, bio, " +
                     "is_active, last_login_at, created_at, updated_at FROM users ORDER BY created_at DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Optional<User> findById(String id) throws SQLException {
        String sql = "SELECT id, full_name, email, password_hash, role, avatar_url, bio, " +
                     "is_active, last_login_at, created_at, updated_at FROM users WHERE id = ?::uuid";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT id, full_name, email, password_hash, role, avatar_url, bio, " +
                     "is_active, last_login_at, created_at, updated_at FROM users WHERE email = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public User save(User user) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, password_hash, role, avatar_url, bio) " +
                     "VALUES (?, ?, ?, ?::user_role, ?, ?) " +
                     "RETURNING id, created_at, updated_at";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole().name());
            ps.setString(5, user.getAvatarUrl());
            ps.setString(6, user.getBio());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getString("id"));
                    user.setUpdatedAt(rs.getString("updated_at"));
                }
            }
        }
        return user;
    }

    public boolean update(User user) throws SQLException {
        String sql = "UPDATE users SET full_name=?, avatar_url=?, bio=?, role=?::user_role, " +
                     "is_active=?, updated_at=NOW() WHERE id=?::uuid";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getAvatarUrl());
            ps.setString(3, user.getBio());
            ps.setString(4, user.getRole().name());
            ps.setBoolean(5, user.isActive());
            ps.setString(6, user.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(String id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id=?::uuid")) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getString("id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(UserRole.valueOf(rs.getString("role")));
        u.setAvatarUrl(rs.getString("avatar_url"));
        u.setBio(rs.getString("bio"));
        u.setActive(rs.getBoolean("is_active"));
        Timestamp lastLogin = rs.getTimestamp("last_login_at");
        if (lastLogin != null) u.setLastLoginAt(lastLogin.toInstant().toString());
        u.setUpdatedAt(rs.getTimestamp("updated_at").toInstant().toString());
        return u;
    }
}
