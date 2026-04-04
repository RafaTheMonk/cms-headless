package com.cms.repository;

import com.cms.db.Database;
import com.cms.model.Project;
import com.cms.model.enums.ContentStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectRepository {

    public List<Project> findAll() throws SQLException {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT id, title, slug, description, cover_image_url, status, course_id, " +
                     "coordinator_id, start_date, end_date, is_featured, published_at, created_at, updated_at " +
                     "FROM projects ORDER BY created_at DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Optional<Project> findById(String id) throws SQLException {
        String sql = "SELECT id, title, slug, description, cover_image_url, status, course_id, " +
                     "coordinator_id, start_date, end_date, is_featured, published_at, created_at, updated_at " +
                     "FROM projects WHERE id=?::uuid";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public Project save(Project project) throws SQLException {
        String sql = "INSERT INTO projects (title, slug, description, cover_image_url, status, " +
                     "course_id, coordinator_id, start_date, end_date, is_featured) " +
                     "VALUES (?, ?, ?, ?, ?::content_status, ?::uuid, ?::uuid, ?::date, ?::date, ?) " +
                     "RETURNING id, created_at, updated_at";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, project.getTitle());
            ps.setString(2, project.getSlug());
            ps.setString(3, project.getDescription());
            ps.setString(4, project.getCoverImageUrl());
            ps.setString(5, project.getStatus().name());
            ps.setString(6, project.getCourseId());
            ps.setString(7, project.getCoordinatorId());
            ps.setString(8, project.getStartDate());
            ps.setString(9, project.getEndDate());
            ps.setBoolean(10, project.isFeatured());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    project.setId(rs.getString("id"));
                    project.setUpdatedAt(rs.getTimestamp("updated_at").toInstant().toString());
                }
            }
        }
        return project;
    }

    public boolean update(Project project) throws SQLException {
        String sql = "UPDATE projects SET title=?, slug=?, description=?, cover_image_url=?, " +
                     "status=?::content_status, course_id=?::uuid, coordinator_id=?::uuid, " +
                     "start_date=?::date, end_date=?::date, is_featured=?, updated_at=NOW() WHERE id=?::uuid";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, project.getTitle());
            ps.setString(2, project.getSlug());
            ps.setString(3, project.getDescription());
            ps.setString(4, project.getCoverImageUrl());
            ps.setString(5, project.getStatus().name());
            ps.setString(6, project.getCourseId());
            ps.setString(7, project.getCoordinatorId());
            ps.setString(8, project.getStartDate());
            ps.setString(9, project.getEndDate());
            ps.setBoolean(10, project.isFeatured());
            ps.setString(11, project.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(String id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM projects WHERE id=?::uuid")) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Project map(ResultSet rs) throws SQLException {
        Project p = new Project();
        p.setId(rs.getString("id"));
        p.setTitle(rs.getString("title"));
        p.setSlug(rs.getString("slug"));
        p.setDescription(rs.getString("description"));
        p.setCoverImageUrl(rs.getString("cover_image_url"));
        p.setStatus(ContentStatus.valueOf(rs.getString("status")));
        p.setCourseId(rs.getString("course_id"));
        p.setCoordinatorId(rs.getString("coordinator_id"));
        Date sd = rs.getDate("start_date");
        if (sd != null) p.setStartDate(sd.toString());
        Date ed = rs.getDate("end_date");
        if (ed != null) p.setEndDate(ed.toString());
        p.setFeatured(rs.getBoolean("is_featured"));
        Timestamp pub = rs.getTimestamp("published_at");
        if (pub != null) p.setPublishedAt(pub.toInstant().toString());
        p.setUpdatedAt(rs.getTimestamp("updated_at").toInstant().toString());
        return p;
    }
}
