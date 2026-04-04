package com.cms.repository;

import com.cms.db.Database;
import com.cms.model.News;
import com.cms.model.enums.ContentStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NewsRepository {

    public List<News> findAll() throws SQLException {
        List<News> list = new ArrayList<>();
        String sql = "SELECT id, title, slug, subtitle, body, cover_image_url, status, " +
                     "is_featured, author_id, course_id, published_at, created_at, updated_at " +
                     "FROM news ORDER BY created_at DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<News> findByStatus(ContentStatus status) throws SQLException {
        List<News> list = new ArrayList<>();
        String sql = "SELECT id, title, slug, subtitle, body, cover_image_url, status, " +
                     "is_featured, author_id, course_id, published_at, created_at, updated_at " +
                     "FROM news WHERE status=?::content_status ORDER BY published_at DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<News> findById(String id) throws SQLException {
        String sql = "SELECT id, title, slug, subtitle, body, cover_image_url, status, " +
                     "is_featured, author_id, course_id, published_at, created_at, updated_at " +
                     "FROM news WHERE id=?::uuid";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<News> findBySlug(String slug) throws SQLException {
        String sql = "SELECT id, title, slug, subtitle, body, cover_image_url, status, " +
                     "is_featured, author_id, course_id, published_at, created_at, updated_at " +
                     "FROM news WHERE slug=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slug);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public News save(News news) throws SQLException {
        String sql = "INSERT INTO news (title, slug, subtitle, body, cover_image_url, status, " +
                     "is_featured, author_id, course_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?::content_status, ?, ?::uuid, ?::uuid) " +
                     "RETURNING id, created_at, updated_at";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, news.getTitle());
            ps.setString(2, news.getSlug());
            ps.setString(3, news.getSubtitle());
            ps.setString(4, news.getBody());
            ps.setString(5, news.getCoverImageUrl());
            ps.setString(6, news.getStatus().name());
            ps.setBoolean(7, news.isFeatured());
            ps.setString(8, news.getAuthorId());
            ps.setString(9, news.getCourseId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    news.setId(rs.getString("id"));
                    news.setUpdatedAt(rs.getTimestamp("updated_at").toInstant().toString());
                }
            }
        }
        return news;
    }

    public boolean update(News news) throws SQLException {
        String sql = "UPDATE news SET title=?, slug=?, subtitle=?, body=?, cover_image_url=?, " +
                     "status=?::content_status, is_featured=?, course_id=?::uuid, updated_at=NOW() " +
                     "WHERE id=?::uuid";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, news.getTitle());
            ps.setString(2, news.getSlug());
            ps.setString(3, news.getSubtitle());
            ps.setString(4, news.getBody());
            ps.setString(5, news.getCoverImageUrl());
            ps.setString(6, news.getStatus().name());
            ps.setBoolean(7, news.isFeatured());
            ps.setString(8, news.getCourseId());
            ps.setString(9, news.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(String id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM news WHERE id=?::uuid")) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private News map(ResultSet rs) throws SQLException {
        News n = new News();
        n.setId(rs.getString("id"));
        n.setTitle(rs.getString("title"));
        n.setSlug(rs.getString("slug"));
        n.setSubtitle(rs.getString("subtitle"));
        n.setBody(rs.getString("body"));
        n.setCoverImageUrl(rs.getString("cover_image_url"));
        n.setStatus(ContentStatus.valueOf(rs.getString("status")));
        n.setFeatured(rs.getBoolean("is_featured"));
        n.setAuthorId(rs.getString("author_id"));
        n.setCourseId(rs.getString("course_id"));
        Timestamp pub = rs.getTimestamp("published_at");
        if (pub != null) n.setPublishedAt(pub.toInstant().toString());
        n.setUpdatedAt(rs.getTimestamp("updated_at").toInstant().toString());
        return n;
    }
}
