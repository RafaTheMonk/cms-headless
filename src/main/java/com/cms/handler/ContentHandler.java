package com.cms.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.cms.model.Content;
import com.cms.server.JsonUtil;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles all requests to /api/content.
 * 
 * This is the CONTROLLER layer — it receives HTTP requests,
 * delegates to the model, and returns HTTP responses.
 * 
 * Supported operations:
 *   GET  /api/content       → list all content
 *   GET  /api/content/{id}  → fetch one by ID
 *   POST /api/content       → create new content
 * 
 * The data is stored in-memory for now (a List).
 * Later we'll replace this with a database repository.
 */
public class ContentHandler implements HttpHandler {

    // In-memory storage — will be replaced by a database later
    private final List<Content> contents = new ArrayList<>();

    public ContentHandler() {
        // Seed with sample data so GET works immediately
        contents.add(new Content(
                "Welcome to the CMS",
                "welcome-to-the-cms",
                "This is your first piece of content."
        ));
        contents.add(new Content(
                "Getting Started",
                "getting-started",
                "Learn how to use the headless CMS API."
        ));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS preflight — browsers send OPTIONS before POST/PUT
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleCors(exchange);
            return;
        }

        try {
            String method = exchange.getRequestMethod().toUpperCase();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    handleGet(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "PUT":
                    handlePut(exchange, path);
                    break;
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    JsonUtil.sendError(exchange, 405, "Method not allowed: " + method);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error handling request: " + e.getMessage());
            e.printStackTrace();
            JsonUtil.sendError(exchange, 500, "Internal server error");
        }
    }

    // =========================================================
    //  GET — Fetch content
    // =========================================================

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        // Extract what comes after "/api/content"
        // Examples:
        //   "/api/content"      → id = ""   → list all
        //   "/api/content/"     → id = ""   → list all
        //   "/api/content/abc"  → id = "abc" → fetch one
        String id = extractIdFromPath(path);

        if (id.isEmpty()) {
            handleGetAll(exchange);
        } else {
            handleGetById(exchange, id);
        }
    }

    /**
     * GET /api/content → returns ALL content as a JSON array.
     */
    private void handleGetAll(HttpExchange exchange) throws IOException {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < contents.size(); i++) {
            if (i > 0) json.append(",");
            json.append(contents.get(i).toJson());
        }
        json.append("]");

        JsonUtil.sendJson(exchange, 200, json.toString());
    }

    /**
     * GET /api/content/{id} → returns ONE content item.
     * Returns 404 if not found.
     */
    private void handleGetById(HttpExchange exchange, String id) throws IOException {
        Optional<Content> found = contents.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();

        if (found.isPresent()) {
            JsonUtil.sendJson(exchange, 200, found.get().toJson());
        } else {
            JsonUtil.sendError(exchange, 404, "Content not found with id: " + id);
        }
    }

    // =========================================================
    //  POST — Create new content
    // =========================================================

    /**
     * POST /api/content → creates a new content item.
     * 
     * Expects JSON body: {"title": "...", "slug": "...", "body": "..."}
     * - title is REQUIRED
     * - slug is auto-generated from title if not provided
     * - body defaults to empty string
     * 
     * Returns: 201 Created + the new content as JSON
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = JsonUtil.readRequestBody(exchange);

        String title = JsonUtil.extractJsonValue(requestBody, "title");
        String slug = JsonUtil.extractJsonValue(requestBody, "slug");
        String body = JsonUtil.extractJsonValue(requestBody, "body");

        if (title == null || title.isBlank()) {
            JsonUtil.sendError(exchange, 400, "Field 'title' is required");
            return;
        }

        // Auto-generate slug if not provided
        if (slug == null || slug.isBlank()) {
            slug = Content.generateSlug(title);
        }

        Content content = new Content(title, slug, body != null ? body : "");
        contents.add(content);

        JsonUtil.sendJson(exchange, 201, content.toJson());
    }

    // =========================================================
    //  PUT — Update existing content
    // =========================================================

    /**
     * PUT /api/content/{id} → updates an existing content item.
     * 
     * Only updates the fields that are present in the request body.
     * This is called a "partial update" (some APIs use PATCH for this).
     * 
     * Returns: 200 OK + the updated content
     *          404 if not found
     */
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        String id = extractIdFromPath(path);

        if (id.isEmpty()) {
            JsonUtil.sendError(exchange, 400, "PUT requires an ID: /api/content/{id}");
            return;
        }

        Optional<Content> found = contents.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();

        if (found.isEmpty()) {
            JsonUtil.sendError(exchange, 404, "Content not found with id: " + id);
            return;
        }

        Content content = found.get();
        String requestBody = JsonUtil.readRequestBody(exchange);

        // Only update fields that were sent in the request
        String title = JsonUtil.extractJsonValue(requestBody, "title");
        String slug = JsonUtil.extractJsonValue(requestBody, "slug");
        String body = JsonUtil.extractJsonValue(requestBody, "body");
        String status = JsonUtil.extractJsonValue(requestBody, "status");

        if (title != null && !title.isBlank()) content.setTitle(title);
        if (slug != null && !slug.isBlank()) content.setSlug(slug);
        if (body != null) content.setBody(body);
        if (status != null && !status.isBlank()) content.setStatus(status);

        content.setUpdatedAt(Instant.now().toString());

        JsonUtil.sendJson(exchange, 200, content.toJson());
    }

    // =========================================================
    //  DELETE — Remove content
    // =========================================================

    /**
     * DELETE /api/content/{id} → removes a content item.
     * 
     * Returns: 200 OK + confirmation message
     *          404 if not found
     */
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        String id = extractIdFromPath(path);

        if (id.isEmpty()) {
            JsonUtil.sendError(exchange, 400, "DELETE requires an ID: /api/content/{id}");
            return;
        }

        boolean removed = contents.removeIf(c -> c.getId().equals(id));

        if (removed) {
            JsonUtil.sendJson(exchange, 200, "{\"message\":\"Content deleted successfully\"}");
        } else {
            JsonUtil.sendError(exchange, 404, "Content not found with id: " + id);
        }
    }

    // =========================================================
    //  Helpers
    // =========================================================

    /**
     * Extracts the ID from a URL path.
     * "/api/content/abc-123" → "abc-123"
     * "/api/content"         → ""
     */
    private String extractIdFromPath(String path) {
        String prefix = "/api/content/";
        if (path.startsWith(prefix) && path.length() > prefix.length()) {
            return path.substring(prefix.length());
        }
        return "";
    }

    /**
     * Handles CORS preflight requests.
     * Browsers send OPTIONS before POST/PUT/DELETE to check
     * if the server allows cross-origin requests.
     */
    private void handleCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(204, -1);
    }
}