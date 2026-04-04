package com.cms.model;

import com.cms.server.JsonUtil;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Professor {

    private String id;
    private String userId;
    private String registration;
    private String title;
    private String lattesUrl;
    private List<String> researchAreas;
    private String createdAt;
    private String updatedAt;

    public Professor() {
        this.id        = UUID.randomUUID().toString();
        this.createdAt = Instant.now().toString();
        this.updatedAt = this.createdAt;
    }

    public Professor(String userId) {
        this();
        this.userId = userId;
    }

    public String toJson() {
        return "{"
            + "\"id\":"             + q(id)           + ","
            + "\"userId\":"         + q(userId)        + ","
            + "\"registration\":"   + q(registration)  + ","
            + "\"title\":"          + q(title)         + ","
            + "\"lattesUrl\":"      + q(lattesUrl)     + ","
            + "\"researchAreas\":"  + toJsonArray(researchAreas) + ","
            + "\"createdAt\":"      + q(createdAt)     + ","
            + "\"updatedAt\":"      + q(updatedAt)
            + "}";
    }

    private static String q(Object v) {
        if (v == null) return "null";
        return "\"" + JsonUtil.escapeJson(v.toString()) + "\"";
    }

    private static String toJsonArray(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(JsonUtil.escapeJson(list.get(i))).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    // Getters and Setters
    public String getId()                              { return id; }
    public void setId(String id)                       { this.id = id; }

    public String getUserId()                          { return userId; }
    public void setUserId(String userId)               { this.userId = userId; }

    public String getRegistration()                    { return registration; }
    public void setRegistration(String registration)   { this.registration = registration; }

    public String getTitle()                           { return title; }
    public void setTitle(String title)                 { this.title = title; }

    public String getLattesUrl()                       { return lattesUrl; }
    public void setLattesUrl(String lattesUrl)         { this.lattesUrl = lattesUrl; }

    public List<String> getResearchAreas()             { return researchAreas; }
    public void setResearchAreas(List<String> areas)   { this.researchAreas = areas; }

    public String getCreatedAt()                       { return createdAt; }

    public String getUpdatedAt()                       { return updatedAt; }
    public void setUpdatedAt(String updatedAt)         { this.updatedAt = updatedAt; }
}
