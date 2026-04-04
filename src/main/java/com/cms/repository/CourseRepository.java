package com.cms.repository;

import com.cms.db.Database;
import com.cms.model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CourseRepository {

    public List<Course> findAll() throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT id, name, slug, code, description, duration_semesters, " +
                     "coordinator_id, is_active, created_at, updated_at FROM courses ORDER BY name";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Optional<Course> findById(String id) throws SQLException {
        String sql = "SELECT id, name, slug, code, description, duration_semesters, " +
                     "coordinator_id, is_active, created_at, updated_at FROM courses WHERE id=?::uuid";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Course> findBySlug(String slug) throws SQLException {
        String sql = "SELECT id, name, slug, code, description, duration_semesters, " +
                     "coordinator_id, is_active, created_at, updated_at FROM courses WHERE slug=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slug);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public Course save(Course course) throws SQLException {
        String sql = "INSERT INTO courses (name, slug, code, description, duration_semesters, coordinator_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?::uuid) RETURNING id, created_at, updated_at";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, course.getName());
            ps.setString(2, course.getSlug());
            ps.setString(3, course.getCode());
            ps.setString(4, course.getDescription());
            if (course.getDurationSemesters() != null)
                ps.setInt(5, course.getDurationSemesters());
            else
                ps.setNull(5, Types.INTEGER);
            ps.setString(6, course.getCoordinatorId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    course.setId(rs.getString("id"));
                    course.setUpdatedAt(rs.getTimestamp("updated_at").toInstant().toString());
                }
            }
        }
        return course;
    }

    public boolean update(Course course) throws SQLException {
        String sql = "UPDATE courses SET name=?, slug=?, code=?, description=?, " +
                     "duration_semesters=?, coordinator_id=?::uuid, is_active=?, updated_at=NOW() " +
                     "WHERE id=?::uuid";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, course.getName());
            ps.setString(2, course.getSlug());
            ps.setString(3, course.getCode());
            ps.setString(4, course.getDescription());
            if (course.getDurationSemesters() != null)
                ps.setInt(5, course.getDurationSemesters());
            else
                ps.setNull(5, Types.INTEGER);
            ps.setString(6, course.getCoordinatorId());
            ps.setBoolean(7, course.isActive());
            ps.setString(8, course.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(String id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM courses WHERE id=?::uuid")) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Course map(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setId(rs.getString("id"));
        c.setName(rs.getString("name"));
        c.setSlug(rs.getString("slug"));
        c.setCode(rs.getString("code"));
        c.setDescription(rs.getString("description"));
        int dur = rs.getInt("duration_semesters");
        if (!rs.wasNull()) c.setDurationSemesters(dur);
        c.setCoordinatorId(rs.getString("coordinator_id"));
        c.setActive(rs.getBoolean("is_active"));
        c.setUpdatedAt(rs.getTimestamp("updated_at").toInstant().toString());
        return c;
    }
}
