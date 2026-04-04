package com.cms.handler;

import com.cms.model.Project;
import com.cms.model.enums.ContentStatus;
import com.cms.repository.ProjectRepository;
import com.cms.server.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Endpoints:
 *   GET    /api/projects          — lista todos
 *   GET    /api/projects/{id}     — busca por ID
 *   POST   /api/projects          — cria
 *   PUT    /api/projects/{id}     — atualiza
 *   DELETE /api/projects/{id}     — remove
 */
public class ProjectHandler implements HttpHandler {

    private final ProjectRepository repo = new ProjectRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JsonUtil.setCorsHeaders(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        try {
            String method = exchange.getRequestMethod().toUpperCase();
            String id     = extractId(exchange.getRequestURI().getPath());

            switch (method) {
                case "GET":
                    if (id != null) handleGetById(exchange, id);
                    else            handleGetAll(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "PUT":
                    if (id != null) handlePut(exchange, id);
                    else JsonUtil.sendError(exchange, 400, "ID obrigatório para PUT");
                    break;
                case "DELETE":
                    if (id != null) handleDelete(exchange, id);
                    else JsonUtil.sendError(exchange, 400, "ID obrigatório para DELETE");
                    break;
                default:
                    JsonUtil.sendError(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            System.err.println("[ProjectHandler] " + e.getMessage());
            JsonUtil.sendError(exchange, 500, "Erro interno");
        }
    }

    private void handleGetAll(HttpExchange exchange) throws SQLException, IOException {
        List<Project> items = repo.findAll();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(items.get(i).toJson());
        }
        sb.append("]");
        JsonUtil.sendJson(exchange, 200, sb.toString());
    }

    private void handleGetById(HttpExchange exchange, String id) throws SQLException, IOException {
        Optional<Project> project = repo.findById(id);
        if (project.isPresent()) JsonUtil.sendJson(exchange, 200, project.get().toJson());
        else JsonUtil.sendError(exchange, 404, "Projeto não encontrado");
    }

    private void handlePost(HttpExchange exchange) throws SQLException, IOException {
        String body  = JsonUtil.readBody(exchange);
        String title = JsonUtil.extractField(body, "title");
        String slug  = JsonUtil.extractField(body, "slug");
        String desc  = JsonUtil.extractField(body, "description");

        if (title == null || slug == null || desc == null) {
            JsonUtil.sendError(exchange, 400, "title, slug e description são obrigatórios");
            return;
        }

        Project project = new Project(title, slug, desc);
        String coverUrl = JsonUtil.extractField(body, "coverImageUrl");
        if (coverUrl != null) project.setCoverImageUrl(coverUrl);
        String courseId = JsonUtil.extractField(body, "courseId");
        if (courseId != null) project.setCourseId(courseId);
        String coordId = JsonUtil.extractField(body, "coordinatorId");
        if (coordId != null) project.setCoordinatorId(coordId);
        String startDate = JsonUtil.extractField(body, "startDate");
        if (startDate != null) project.setStartDate(startDate);
        String endDate = JsonUtil.extractField(body, "endDate");
        if (endDate != null) project.setEndDate(endDate);
        String statusStr = JsonUtil.extractField(body, "status");
        if (statusStr != null) {
            try { project.setStatus(ContentStatus.valueOf(statusStr)); }
            catch (IllegalArgumentException ignored) {}
        }

        repo.save(project);
        JsonUtil.sendJson(exchange, 201, project.toJson());
    }

    private void handlePut(HttpExchange exchange, String id) throws SQLException, IOException {
        Optional<Project> existing = repo.findById(id);
        if (existing.isEmpty()) {
            JsonUtil.sendError(exchange, 404, "Projeto não encontrado");
            return;
        }

        String body      = JsonUtil.readBody(exchange);
        Project project  = existing.get();

        String title = JsonUtil.extractField(body, "title");
        if (title != null) project.setTitle(title);
        String slug = JsonUtil.extractField(body, "slug");
        if (slug != null) project.setSlug(slug);
        String desc = JsonUtil.extractField(body, "description");
        if (desc != null) project.setDescription(desc);
        String coverUrl = JsonUtil.extractField(body, "coverImageUrl");
        if (coverUrl != null) project.setCoverImageUrl(coverUrl);
        String courseId = JsonUtil.extractField(body, "courseId");
        if (courseId != null) project.setCourseId(courseId);
        String coordId = JsonUtil.extractField(body, "coordinatorId");
        if (coordId != null) project.setCoordinatorId(coordId);
        String startDate = JsonUtil.extractField(body, "startDate");
        if (startDate != null) project.setStartDate(startDate);
        String endDate = JsonUtil.extractField(body, "endDate");
        if (endDate != null) project.setEndDate(endDate);
        String statusStr = JsonUtil.extractField(body, "status");
        if (statusStr != null) {
            try { project.setStatus(ContentStatus.valueOf(statusStr)); }
            catch (IllegalArgumentException ignored) {}
        }

        repo.update(project);
        JsonUtil.sendJson(exchange, 200, project.toJson());
    }

    private void handleDelete(HttpExchange exchange, String id) throws SQLException, IOException {
        boolean deleted = repo.delete(id);
        if (deleted) JsonUtil.sendJson(exchange, 200, "{\"deleted\":true}");
        else JsonUtil.sendError(exchange, 404, "Projeto não encontrado");
    }

    private String extractId(String path) {
        String[] parts = path.split("/");
        return parts.length >= 4 ? parts[3] : null;
    }
}
