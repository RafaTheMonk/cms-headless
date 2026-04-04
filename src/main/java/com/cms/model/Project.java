package com.cms.model;

import com.cms.model.enums.ContentStatus;
import com.cms.server.JsonUtil;
import java.time.Instant;
import java.util.UUID;

public class Project {

    private String id;
    private String title;
    private String slug;
    private String description;
    private String coverImageUrl;
    private ContentStatus status;
    private String courseId;
    private String coordinatorId;
    private String startDate;
    private String endDate;
    private boolean isFeatured;
    private String publishedAt;
    private String createdAt;
    private String updatedAt;

    public Project() {
        this.id        = UUID.randomUUID().toString();
        this.status    = ContentStatus.draft;
        this.isFeatured = false;
        this.createdAt = Instant.now().toString();
        this.updatedAt = this.createdAt;
    }

    public Project(String title, String slug, String description) {
        this();
        this.title       = title;
        this.slug        = slug;
        this.description = description;
    }

    public String toJson() {
        return "{"
            + "\"id\":"            + q(id)            + ","
            + "\"title\":"         + q(title)          + ","
            + "\"slug\":"          + q(slug)           + ","
            + "\"description\":"   + q(description)    + ","
            + "\"coverImageUrl\":" + q(coverImageUrl)  + ","
            + "\"status\":"        + q(status)         + ","
            + "\"courseId\":"      + q(courseId)       + ","
            + "\"coordinatorId\":" + q(coordinatorId)  + ","
            + "\"startDate\":"     + q(startDate)      + ","
            + "\"endDate\":"       + q(endDate)        + ","
            + "\"isFeatured\":"    + isFeatured        + ","
            + "\"publishedAt\":"   + q(publishedAt)    + ","
            + "\"createdAt\":"     + q(createdAt)      + ","
            + "\"updatedAt\":"     + q(updatedAt)
            + "}";
    }

    private static String q(Object v) {
        if (v == null) return "null";
        return "\"" + JsonUtil.escapeJson(v.toString()) + "\"";
    }

    // Getters and Setters
    public String getId()                              { return id; }
    public void setId(String id)                       { this.id = id; }

    public String getTitle()                           { return title; }
    public void setTitle(String title)                 { this.title = title; }

    public String getSlug()                            { return slug; }
    public void setSlug(String slug)                   { this.slug = slug; }

    public String getDescription()                     { return description; }
    public void setDescription(String description)     { this.description = description; }

    public String getCoverImageUrl()                   { return coverImageUrl; }
    public void setCoverImageUrl(String url)           { this.coverImageUrl = url; }

    public ContentStatus getStatus()                   { return status; }
    public void setStatus(ContentStatus status)        { this.status = status; }

    public String getCourseId()                        { return courseId; }
    public void setCourseId(String courseId)           { this.courseId = courseId; }

    public String getCoordinatorId()                   { return coordinatorId; }
    public void setCoordinatorId(String coordinatorId) { this.coordinatorId = coordinatorId; }

    public String getStartDate()                       { return startDate; }
    public void setStartDate(String startDate)         { this.startDate = startDate; }

    public String getEndDate()                         { return endDate; }
    public void setEndDate(String endDate)             { this.endDate = endDate; }

    public boolean isFeatured()                        { return isFeatured; }
    public void setFeatured(boolean featured)          { this.isFeatured = featured; }

    public String getPublishedAt()                     { return publishedAt; }
    public void setPublishedAt(String publishedAt)     { this.publishedAt = publishedAt; }

    public String getCreatedAt()                       { return createdAt; }

    public String getUpdatedAt()                       { return updatedAt; }
    public void setUpdatedAt(String updatedAt)         { this.updatedAt = updatedAt; }
}
