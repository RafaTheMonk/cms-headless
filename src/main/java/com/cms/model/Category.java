package com.cms.model;

import com.cms.server.JsonUtil;
import java.time.Instant;
import java.util.UUID;

public class Category {

    private String id;
    private String name;
    private String slug;
    private String description;
    private String parentId;
    private String createdAt;

    public Category() {
        this.id        = UUID.randomUUID().toString();
        this.createdAt = Instant.now().toString();
    }

    public Category(String name, String slug) {
        this();
        this.name = name;
        this.slug = slug;
    }

    public String toJson() {
        return "{"
            + "\"id\":"          + q(id)          + ","
            + "\"name\":"        + q(name)         + ","
            + "\"slug\":"        + q(slug)         + ","
            + "\"description\":" + q(description)  + ","
            + "\"parentId\":"    + q(parentId)     + ","
            + "\"createdAt\":"   + q(createdAt)
            + "}";
    }

    private static String q(Object v) {
        if (v == null) return "null";
        return "\"" + JsonUtil.escapeJson(v.toString()) + "\"";
    }

    // Getters and Setters
    public String getId()                          { return id; }
    public void setId(String id)                   { this.id = id; }

    public String getName()                        { return name; }
    public void setName(String name)               { this.name = name; }

    public String getSlug()                        { return slug; }
    public void setSlug(String slug)               { this.slug = slug; }

    public String getDescription()                 { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getParentId()                    { return parentId; }
    public void setParentId(String parentId)       { this.parentId = parentId; }

    public String getCreatedAt()                   { return createdAt; }
}
