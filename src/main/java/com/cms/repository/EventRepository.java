package com.cms.repository;

import com.cms.db.Database;
import com.cms.model.Event;
import com.cms.model.enums.ContentStatus;
import com.cms.model.enums.EventModality;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventRepository {

    public List<Event> findAll() throws SQLException {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT id, title, slug, description, cover_image_url, status, modality, " +
                     "location, online_url, starts_at, ends_at, registration_url, max_participants, " +
                     "organizer_id, course_id, is_featured, published_at, created_at, updated_at " +
                     "FROM events ORDER BY starts_at DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Optional<Event> findById(String id) throws SQLException {
        String sql = "SELECT id, title, slug, description, cover_image_url, status, modality, " +
                     "location, online_url, starts_at, ends_at, registration_url, max_participants, " +
                     "organizer_id, course_id, is_featured, published_at, created_at, updated_at " +
                     "FROM events WHERE id=?::uuid";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public Event save(Event event) throws SQLException {
        String sql = "INSERT INTO events (title, slug, description, cover_image_url, status, modality, " +
                     "location, online_url, starts_at, ends_at, registration_url, max_participants, " +
                     "organizer_id, course_id, is_featured) " +
                     "VALUES (?, ?, ?, ?, ?::content_status, ?::event_modality, ?, ?, ?::timestamptz, " +
                     "?::timestamptz, ?, ?, ?::uuid, ?::uuid, ?) RETURNING id, created_at, updated_at";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getSlug());
            ps.setString(3, event.getDescription());
            ps.setString(4, event.getCoverImageUrl());
            ps.setString(5, event.getStatus().name());
            ps.setString(6, event.getModality().name());
            ps.setString(7, event.getLocation());
            ps.setString(8, event.getOnlineUrl());
            ps.setString(9, event.getStartsAt());
            ps.setString(10, event.getEndsAt());
            ps.setString(11, event.getRegistrationUrl());
            if (event.getMaxParticipants() != null)
                ps.setInt(12, event.getMaxParticipants());
            else
                ps.setNull(12, Types.INTEGER);
            ps.setString(13, event.getOrganizerId());
            ps.setString(14, event.getCourseId());
            ps.setBoolean(15, event.isFeatured());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    event.setId(rs.getString("id"));
                    event.setUpdatedAt(rs.getTimestamp("updated_at").toInstant().toString());
                }
            }
        }
        return event;
    }

    public boolean update(Event event) throws SQLException {
        String sql = "UPDATE events SET title=?, slug=?, description=?, cover_image_url=?, " +
                     "status=?::content_status, modality=?::event_modality, location=?, online_url=?, " +
                     "starts_at=?::timestamptz, ends_at=?::timestamptz, registration_url=?, " +
                     "max_participants=?, organizer_id=?::uuid, course_id=?::uuid, " +
                     "is_featured=?, updated_at=NOW() WHERE id=?::uuid";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getSlug());
            ps.setString(3, event.getDescription());
            ps.setString(4, event.getCoverImageUrl());
            ps.setString(5, event.getStatus().name());
            ps.setString(6, event.getModality().name());
            ps.setString(7, event.getLocation());
            ps.setString(8, event.getOnlineUrl());
            ps.setString(9, event.getStartsAt());
            ps.setString(10, event.getEndsAt());
            ps.setString(11, event.getRegistrationUrl());
            if (event.getMaxParticipants() != null)
                ps.setInt(12, event.getMaxParticipants());
            else
                ps.setNull(12, Types.INTEGER);
            ps.setString(13, event.getOrganizerId());
            ps.setString(14, event.getCourseId());
            ps.setBoolean(15, event.isFeatured());
            ps.setString(16, event.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(String id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM events WHERE id=?::uuid")) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Event map(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setId(rs.getString("id"));
        e.setTitle(rs.getString("title"));
        e.setSlug(rs.getString("slug"));
        e.setDescription(rs.getString("description"));
        e.setCoverImageUrl(rs.getString("cover_image_url"));
        e.setStatus(ContentStatus.valueOf(rs.getString("status")));
        e.setModality(EventModality.valueOf(rs.getString("modality")));
        e.setLocation(rs.getString("location"));
        e.setOnlineUrl(rs.getString("online_url"));
        e.setStartsAt(rs.getTimestamp("starts_at").toInstant().toString());
        Timestamp endsAt = rs.getTimestamp("ends_at");
        if (endsAt != null) e.setEndsAt(endsAt.toInstant().toString());
        e.setRegistrationUrl(rs.getString("registration_url"));
        int max = rs.getInt("max_participants");
        if (!rs.wasNull()) e.setMaxParticipants(max);
        e.setOrganizerId(rs.getString("organizer_id"));
        e.setCourseId(rs.getString("course_id"));
        e.setFeatured(rs.getBoolean("is_featured"));
        Timestamp pub = rs.getTimestamp("published_at");
        if (pub != null) e.setPublishedAt(pub.toInstant().toString());
        e.setUpdatedAt(rs.getTimestamp("updated_at").toInstant().toString());
        return e;
    }
}
