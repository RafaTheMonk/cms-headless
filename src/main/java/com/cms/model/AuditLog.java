package com.cms.model;

import com.cms.server.JsonUtil;
import java.time.Instant;
import java.util.UUID;

public class AuditLog {

    private String id;
    private String userId;
    private String action;
    private String entityType;
    private String entityId;
    private String metadata;
    private String ipAddress;
    private String createdAt;

    public AuditLog() {
        this.id        = UUID.randomUUID().toString();
        this.createdAt = Instant.now().toString();
    }

    public AuditLog(String userId, String action, String entityType, String entityId) {
        this();
        this.userId     = userId;
        this.action     = action;
        this.entityType = entityType;
        this.entityId   = entityId;
    }

    public String toJson() {
        return "{"
            + "\"id\":"          + q(id)          + ","
            + "\"userId\":"      + q(userId)       + ","
            + "\"action\":"      + q(action)       + ","
            + "\"entityType\":"  + q(entityType)   + ","
            + "\"entityId\":"    + q(entityId)     + ","
            + "\"metadata\":"    + (metadata != null ? metadata : "null") + ","
            + "\"ipAddress\":"   + q(ipAddress)    + ","
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

    public String getUserId()                      { return userId; }
    public void setUserId(String userId)           { this.userId = userId; }

    public String getAction()                      { return action; }
    public void setAction(String action)           { this.action = action; }

    public String getEntityType()                  { return entityType; }
    public void setEntityType(String entityType)   { this.entityType = entityType; }

    public String getEntityId()                    { return entityId; }
    public void setEntityId(String entityId)       { this.entityId = entityId; }

    public String getMetadata()                    { return metadata; }
    public void setMetadata(String metadata)       { this.metadata = metadata; }

    public String getIpAddress()                   { return ipAddress; }
    public void setIpAddress(String ipAddress)     { this.ipAddress = ipAddress; }

    public String getCreatedAt()                   { return createdAt; }
}
