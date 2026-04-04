package com.cms.model;

import com.cms.server.JsonUtil;
import java.time.Instant;
import java.util.UUID;

public class Course {

    private String id;
    private String name;
    private String slug;
    private String code;
    private String description;
    private Integer durationSemesters;
    private String coordinatorId;
    private boolean isActive;
    private String createdAt;
    private String updatedAt;

    public Course() {
        this.id        = UUID.randomUUID().toString();
        this.isActive  = true;
        this.createdAt = Instant.now().toString();
        this.updatedAt = this.createdAt;
    }

    public Course(String name, String slug) {
        this();
        this.name = name;
        this.slug = slug;
    }

    public String toJson() {
        return "{"
            + "\"id\":"                  + q(id)                + ","
            + "\"name\":"               + q(name)              + ","
            + "\"slug\":"               + q(slug)              + ","
            + "\"code\":"               + q(code)              + ","
            + "\"description\":"        + q(description)       + ","
            + "\"durationSemesters\":"  + durationSemesters    + ","
            + "\"coordinatorId\":"      + q(coordinatorId)     + ","
            + "\"isActive\":"           + isActive             + ","
            + "\"createdAt\":"          + q(createdAt)         + ","
            + "\"updatedAt\":"          + q(updatedAt)
            + "}";
    }

    private static String q(Object v) {
        if (v == null) return "null";
        return "\"" + JsonUtil.escapeJson(v.toString()) + "\"";
    }

    // Getters and Setters
    public String getId()                            { return id; }
    public void setId(String id)                     { this.id = id; }

    public String getName()                          { return name; }
    public void setName(String name)                 { this.name = name; }

    public String getSlug()                          { return slug; }
    public void setSlug(String slug)                 { this.slug = slug; }

    public String getCode()                          { return code; }
    public void setCode(String code)                 { this.code = code; }

    public String getDescription()                   { return description; }
    public void setDescription(String description)   { this.description = description; }

    public Integer getDurationSemesters()            { return durationSemesters; }
    public void setDurationSemesters(Integer d)      { this.durationSemesters = d; }

    public String getCoordinatorId()                 { return coordinatorId; }
    public void setCoordinatorId(String id)          { this.coordinatorId = id; }

    public boolean isActive()                        { return isActive; }
    public void setActive(boolean active)            { this.isActive = active; }

    public String getCreatedAt()                     { return createdAt; }

    public String getUpdatedAt()                     { return updatedAt; }
    public void setUpdatedAt(String updatedAt)       { this.updatedAt = updatedAt; }
}
