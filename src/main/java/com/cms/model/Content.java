package com.cms.model;

import com.cms.server.JsonUtil;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a piece of content in the CMS.
 * 
 * This is your DOMAIN MODEL — the core concept your application
 * revolves around. Everything else (handlers, server, database)
 * exists to serve this model.
 * 
 * Key design decisions:
 *   - UUID for IDs: globally unique, no database needed to generate
 *   - slug: URL-friendly identifier ("my-post" instead of "abc-123")
 *   - status: controls publishing workflow (draft → published)
 *   - timestamps: track when content was created and last modified
 */
public class Content {

    private String id;
    private String title;
    private String slug;
    private String body;
    private String status;
    private String createdAt;
    private String updatedAt;

    /**
     * Default constructor — sets up auto-generated fields.
     * Called by all other constructors via this().
     */
    public Content() {
        this.id = UUID.randomUUID().toString();
        this.status = "draft";
        this.createdAt = Instant.now().toString();
        this.updatedAt = this.createdAt;
    }

    /**
     * Convenience constructor for creating content with data.
     */
    public Content(String title, String slug, String body) {
        this();
        this.title = title;
        this.slug = slug;
        this.body = body;
    }

    /**
     * Serializes this object to a JSON string.
     * 
     * "Serialize" means converting an in-memory object into a
     * text format that can be sent over the network.
     * 
     * Uses JsonUtil.escapeJson() to handle special characters
     * in user-provided content (titles with quotes, etc.)
     */
    public String toJson() {
        return "{"
                + "\"id\":\"" + id + "\","
                + "\"title\":\"" + JsonUtil.escapeJson(title) + "\","
                + "\"slug\":\"" + JsonUtil.escapeJson(slug) + "\","
                + "\"body\":\"" + JsonUtil.escapeJson(body) + "\","
                + "\"status\":\"" + status + "\","
                + "\"createdAt\":\"" + createdAt + "\","
                + "\"updatedAt\":\"" + updatedAt + "\""
                + "}";
    }

    /**
     * Generates a URL-friendly slug from the title.
     * Example: "My First Post!" → "my-first-post"
     */
    public static String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")  // remove special chars
                .replaceAll("\\s+", "-")            // spaces → hyphens
                .replaceAll("-+", "-")              // collapse multiple hyphens
                .replaceAll("^-|-$", "");           // trim leading/trailing hyphens
    }

    // --- Getters and Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}