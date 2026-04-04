package com.cms.model;

import com.cms.model.enums.ContentStatus;
import com.cms.server.JsonUtil;
import java.time.Instant;
import java.util.UUID;

public class Article {

    private String id;
    private String title;
    private String slug;
    private String abstractText;
    private String body;
    private String coverImageUrl;
    private ContentStatus status;
    private String authorId;
    private String courseId;
    private String publishedAt;
    private String createdAt;
    private String updatedAt;

    public Article() {
        this.id        = UUID.randomUUID().toString();
        this.status    = ContentStatus.draft;
        this.createdAt = Instant.now().toString();
        this.updatedAt = this.createdAt;
    }

    public Article(String title, String slug, String body, String authorId) {
        this();
        this.title    = title;
        this.slug     = slug;
        this.body     = body;
        this.authorId = authorId;
    }

    public String toJson() {
        return "{"
            + "\"id\":"            + q(id)            + ","
            + "\"title\":"         + q(title)          + ","
            + "\"slug\":"          + q(slug)           + ","
            + "\"abstract\":"      + q(abstractText)   + ","
            + "\"body\":"          + q(body)           + ","
            + "\"coverImageUrl\":" + q(coverImageUrl)  + ","
            + "\"status\":"        + q(status)         + ","
            + "\"authorId\":"      + q(authorId)       + ","
            + "\"courseId\":"      + q(courseId)       + ","
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
    public String getId()                            { return id; }
    public void setId(String id)                     { this.id = id; }

    public String getTitle()                         { return title; }
    public void setTitle(String title)               { this.title = title; }

    public String getSlug()                          { return slug; }
    public void setSlug(String slug)                 { this.slug = slug; }

    public String getAbstractText()                  { return abstractText; }
    public void setAbstractText(String abstractText) { this.abstractText = abstractText; }

    public String getBody()                          { return body; }
    public void setBody(String body)                 { this.body = body; }

    public String getCoverImageUrl()                 { return coverImageUrl; }
    public void setCoverImageUrl(String url)         { this.coverImageUrl = url; }

    public ContentStatus getStatus()                 { return status; }
    public void setStatus(ContentStatus status)      { this.status = status; }

    public String getAuthorId()                      { return authorId; }
    public void setAuthorId(String authorId)         { this.authorId = authorId; }

    public String getCourseId()                      { return courseId; }
    public void setCourseId(String courseId)         { this.courseId = courseId; }

    public String getPublishedAt()                   { return publishedAt; }
    public void setPublishedAt(String publishedAt)   { this.publishedAt = publishedAt; }

    public String getCreatedAt()                     { return createdAt; }

    public String getUpdatedAt()                     { return updatedAt; }
    public void setUpdatedAt(String updatedAt)       { this.updatedAt = updatedAt; }
}
