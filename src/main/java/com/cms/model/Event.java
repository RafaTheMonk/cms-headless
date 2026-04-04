package com.cms.model;

import com.cms.model.enums.ContentStatus;
import com.cms.model.enums.EventModality;
import com.cms.server.JsonUtil;
import java.time.Instant;
import java.util.UUID;

public class Event {

    private String id;
    private String title;
    private String slug;
    private String description;
    private String coverImageUrl;
    private ContentStatus status;
    private EventModality modality;
    private String location;
    private String onlineUrl;
    private String startsAt;
    private String endsAt;
    private String registrationUrl;
    private Integer maxParticipants;
    private String organizerId;
    private String courseId;
    private boolean isFeatured;
    private String publishedAt;
    private String createdAt;
    private String updatedAt;

    public Event() {
        this.id        = UUID.randomUUID().toString();
        this.status    = ContentStatus.draft;
        this.modality  = EventModality.presencial;
        this.isFeatured = false;
        this.createdAt = Instant.now().toString();
        this.updatedAt = this.createdAt;
    }

    public Event(String title, String slug, String description, String startsAt) {
        this();
        this.title       = title;
        this.slug        = slug;
        this.description = description;
        this.startsAt    = startsAt;
    }

    public String toJson() {
        return "{"
            + "\"id\":"               + q(id)               + ","
            + "\"title\":"            + q(title)             + ","
            + "\"slug\":"             + q(slug)              + ","
            + "\"description\":"      + q(description)       + ","
            + "\"coverImageUrl\":"    + q(coverImageUrl)     + ","
            + "\"status\":"           + q(status)            + ","
            + "\"modality\":"         + q(modality)          + ","
            + "\"location\":"         + q(location)          + ","
            + "\"onlineUrl\":"        + q(onlineUrl)         + ","
            + "\"startsAt\":"         + q(startsAt)          + ","
            + "\"endsAt\":"           + q(endsAt)            + ","
            + "\"registrationUrl\":"  + q(registrationUrl)   + ","
            + "\"maxParticipants\":"  + maxParticipants      + ","
            + "\"organizerId\":"      + q(organizerId)       + ","
            + "\"courseId\":"         + q(courseId)          + ","
            + "\"isFeatured\":"       + isFeatured           + ","
            + "\"publishedAt\":"      + q(publishedAt)       + ","
            + "\"createdAt\":"        + q(createdAt)         + ","
            + "\"updatedAt\":"        + q(updatedAt)
            + "}";
    }

    private static String q(Object v) {
        if (v == null) return "null";
        return "\"" + JsonUtil.escapeJson(v.toString()) + "\"";
    }

    // Getters and Setters
    public String getId()                                { return id; }
    public void setId(String id)                         { this.id = id; }

    public String getTitle()                             { return title; }
    public void setTitle(String title)                   { this.title = title; }

    public String getSlug()                              { return slug; }
    public void setSlug(String slug)                     { this.slug = slug; }

    public String getDescription()                       { return description; }
    public void setDescription(String description)       { this.description = description; }

    public String getCoverImageUrl()                     { return coverImageUrl; }
    public void setCoverImageUrl(String url)             { this.coverImageUrl = url; }

    public ContentStatus getStatus()                     { return status; }
    public void setStatus(ContentStatus status)          { this.status = status; }

    public EventModality getModality()                   { return modality; }
    public void setModality(EventModality modality)      { this.modality = modality; }

    public String getLocation()                          { return location; }
    public void setLocation(String location)             { this.location = location; }

    public String getOnlineUrl()                         { return onlineUrl; }
    public void setOnlineUrl(String onlineUrl)           { this.onlineUrl = onlineUrl; }

    public String getStartsAt()                          { return startsAt; }
    public void setStartsAt(String startsAt)             { this.startsAt = startsAt; }

    public String getEndsAt()                            { return endsAt; }
    public void setEndsAt(String endsAt)                 { this.endsAt = endsAt; }

    public String getRegistrationUrl()                   { return registrationUrl; }
    public void setRegistrationUrl(String url)           { this.registrationUrl = url; }

    public Integer getMaxParticipants()                  { return maxParticipants; }
    public void setMaxParticipants(Integer max)          { this.maxParticipants = max; }

    public String getOrganizerId()                       { return organizerId; }
    public void setOrganizerId(String organizerId)       { this.organizerId = organizerId; }

    public String getCourseId()                          { return courseId; }
    public void setCourseId(String courseId)             { this.courseId = courseId; }

    public boolean isFeatured()                          { return isFeatured; }
    public void setFeatured(boolean featured)            { this.isFeatured = featured; }

    public String getPublishedAt()                       { return publishedAt; }
    public void setPublishedAt(String publishedAt)       { this.publishedAt = publishedAt; }

    public String getCreatedAt()                         { return createdAt; }

    public String getUpdatedAt()                         { return updatedAt; }
    public void setUpdatedAt(String updatedAt)           { this.updatedAt = updatedAt; }
}
