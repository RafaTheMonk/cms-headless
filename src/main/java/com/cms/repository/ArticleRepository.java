package com.cms.repository;

import com.cms.db.Database;
import com.cms.model.Article;
import com.cms.model.enums.ContentStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArticleRepository {

    public List<Article> findAll() throws SQLException {
        List<Article> list = new ArrayList<>();
        String sql = "SELECT id, title, slug, abstract, body, cover_image_url, status, " +
                     "author_id, course_id, published_at, created_at, updated_at " +
                     "FROM articles ORDER BY created_at DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Article> findByStatus(ContentStatus status) throws SQLException {
        List<Article> list = new ArrayList<>();
        String sql = "SELECT id, title, slug, abstract, body, cover_image_url, status, " +
                     "author_id, course_id, published_at, created_at, updated_at " +
                     "FROM articles WHERE status=?::content_status ORDER BY published_at DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<Article> findById(String id) throws SQLException {
        String sql = "SELECT id, title, slug, abstract, body, cover_image_url, status, " +
                     "author_id, course_id, published_at, created_at, updated_at " +
                     "FROM articles WHERE id=?::uuid";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public Article save(Article article) throws SQLException {
        String sql = "INSERT INTO articles (title, slug, abstract, body, cover_image_url, status, author_id, course_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?::content_status, ?::uuid, ?::uuid) " +
                     "RETURNING id, created_at, updated_at";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, article.getTitle());
            ps.setString(2, article.getSlug());
            ps.setString(3, article.getAbstractText());
            ps.setString(4, article.getBody());
            ps.setString(5, article.getCoverImageUrl());
            ps.setString(6, article.getStatus().name());
            ps.setString(7, article.getAuthorId());
            ps.setString(8, article.getCourseId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    article.setId(rs.getString("id"));
                    article.setUpdatedAt(rs.getTimestamp("updated_at").toInstant().toString());
                }
            }
        }
        return article;
    }

    public boolean update(Article article) throws SQLException {
        String sql = "UPDATE articles SET title=?, slug=?, abstract=?, body=?, cover_image_url=?, " +
                     "status=?::content_status, course_id=?::uuid, updated_at=NOW() WHERE id=?::uuid";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, article.getTitle());
            ps.setString(2, article.getSlug());
            ps.setString(3, article.getAbstractText());
            ps.setString(4, article.getBody());
            ps.setString(5, article.getCoverImageUrl());
            ps.setString(6, article.getStatus().name());
            ps.setString(7, article.getCourseId());
            ps.setString(8, article.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(String id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM articles WHERE id=?::uuid")) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Article map(ResultSet rs) throws SQLException {
        Article a = new Article();
        a.setId(rs.getString("id"));
        a.setTitle(rs.getString("title"));
        a.setSlug(rs.getString("slug"));
        a.setAbstractText(rs.getString("abstract"));
        a.setBody(rs.getString("body"));
        a.setCoverImageUrl(rs.getString("cover_image_url"));
        a.setStatus(ContentStatus.valueOf(rs.getString("status")));
        a.setAuthorId(rs.getString("author_id"));
        a.setCourseId(rs.getString("course_id"));
        Timestamp pub = rs.getTimestamp("published_at");
        if (pub != null) a.setPublishedAt(pub.toInstant().toString());
        a.setUpdatedAt(rs.getTimestamp("updated_at").toInstant().toString());
        return a;
    }
}
