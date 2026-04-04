package com.cms.model;

import com.cms.server.JsonUtil;
import java.time.Instant;
import java.util.UUID;

// Named FileEntity to avoid collision with java.io.File
public class FileEntity {

    private String id;
    private String originalName;
    private String storedPath;
    private String mimeType;
    private long sizeBytes;
    private String description;
    private String uploadedBy;
    private String createdAt;

    public FileEntity() {
        this.id        = UUID.randomUUID().toString();
        this.createdAt = Instant.now().toString();
    }

    public FileEntity(String originalName, String storedPath, String mimeType, long sizeBytes, String uploadedBy) {
        this();
        this.originalName = originalName;
        this.storedPath   = storedPath;
        this.mimeType     = mimeType;
        this.sizeBytes    = sizeBytes;
        this.uploadedBy   = uploadedBy;
    }

    public String toJson() {
        return "{"
            + "\"id\":"           + q(id)           + ","
            + "\"originalName\":" + q(originalName) + ","
            + "\"storedPath\":"   + q(storedPath)   + ","
            + "\"mimeType\":"     + q(mimeType)     + ","
            + "\"sizeBytes\":"    + sizeBytes       + ","
            + "\"description\":"  + q(description)  + ","
            + "\"uploadedBy\":"   + q(uploadedBy)   + ","
            + "\"createdAt\":"    + q(createdAt)
            + "}";
    }

    private static String q(Object v) {
        if (v == null) return "null";
        return "\"" + JsonUtil.escapeJson(v.toString()) + "\"";
    }

    // Getters and Setters
    public String getId()                              { return id; }
    public void setId(String id)                       { this.id = id; }

    public String getOriginalName()                    { return originalName; }
    public void setOriginalName(String originalName)   { this.originalName = originalName; }

    public String getStoredPath()                      { return storedPath; }
    public void setStoredPath(String storedPath)       { this.storedPath = storedPath; }

    public String getMimeType()                        { return mimeType; }
    public void setMimeType(String mimeType)           { this.mimeType = mimeType; }

    public long getSizeBytes()                         { return sizeBytes; }
    public void setSizeBytes(long sizeBytes)           { this.sizeBytes = sizeBytes; }

    public String getDescription()                     { return description; }
    public void setDescription(String description)     { this.description = description; }

    public String getUploadedBy()                      { return uploadedBy; }
    public void setUploadedBy(String uploadedBy)       { this.uploadedBy = uploadedBy; }

    public String getCreatedAt()                       { return createdAt; }
}
