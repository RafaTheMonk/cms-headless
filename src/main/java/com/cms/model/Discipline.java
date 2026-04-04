package com.cms.model;

import com.cms.server.JsonUtil;
import java.time.Instant;
import java.util.UUID;

public class Discipline {

    private String id;
    private String name;
    private String code;
    private String description;
    private Integer workloadHours;
    private String syllabus;
    private String createdAt;
    private String updatedAt;

    public Discipline() {
        this.id        = UUID.randomUUID().toString();
        this.createdAt = Instant.now().toString();
        this.updatedAt = this.createdAt;
    }

    public Discipline(String name, String code) {
        this();
        this.name = name;
        this.code = code;
    }

    public String toJson() {
        return "{"
            + "\"id\":"            + q(id)           + ","
            + "\"name\":"          + q(name)          + ","
            + "\"code\":"          + q(code)          + ","
            + "\"description\":"   + q(description)   + ","
            + "\"workloadHours\":" + workloadHours    + ","
            + "\"syllabus\":"      + q(syllabus)      + ","
            + "\"createdAt\":"     + q(createdAt)     + ","
            + "\"updatedAt\":"     + q(updatedAt)
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

    public String getCode()                        { return code; }
    public void setCode(String code)               { this.code = code; }

    public String getDescription()                 { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getWorkloadHours()              { return workloadHours; }
    public void setWorkloadHours(Integer h)        { this.workloadHours = h; }

    public String getSyllabus()                    { return syllabus; }
    public void setSyllabus(String syllabus)       { this.syllabus = syllabus; }

    public String getCreatedAt()                   { return createdAt; }

    public String getUpdatedAt()                   { return updatedAt; }
    public void setUpdatedAt(String updatedAt)     { this.updatedAt = updatedAt; }
}
