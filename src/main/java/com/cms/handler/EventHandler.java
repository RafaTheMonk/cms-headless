package com.cms.handler;

import com.cms.model.Event;
import com.cms.model.enums.ContentStatus;
import com.cms.model.enums.EventModality;
import com.cms.repository.EventRepository;
import com.cms.server.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Endpoints:
 *   GET    /api/events          — lista todos
 *   GET    /api/events/{id}     — busca por ID
 *   POST   /api/events          — cria
 *   PUT    /api/events/{id}     — atualiza
 *   DELETE /api/events/{id}     — remove
 */
public class EventHandler implements HttpHandler {

    private final EventRepository repo = new EventRepository();

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
            System.err.println("[EventHandler] " + e.getMessage());
            JsonUtil.sendError(exchange, 500, "Erro interno");
        }
    }

    private void handleGetAll(HttpExchange exchange) throws SQLException, IOException {
        List<Event> items = repo.findAll();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(items.get(i).toJson());
        }
        sb.append("]");
        JsonUtil.sendJson(exchange, 200, sb.toString());
    }

    private void handleGetById(HttpExchange exchange, String id) throws SQLException, IOException {
        Optional<Event> event = repo.findById(id);
        if (event.isPresent()) JsonUtil.sendJson(exchange, 200, event.get().toJson());
        else JsonUtil.sendError(exchange, 404, "Evento não encontrado");
    }

    private void handlePost(HttpExchange exchange) throws SQLException, IOException {
        String body     = JsonUtil.readBody(exchange);
        String title    = JsonUtil.extractField(body, "title");
        String slug     = JsonUtil.extractField(body, "slug");
        String desc     = JsonUtil.extractField(body, "description");
        String startsAt = JsonUtil.extractField(body, "startsAt");

        if (title == null || slug == null || desc == null || startsAt == null) {
            JsonUtil.sendError(exchange, 400, "title, slug, description e startsAt são obrigatórios");
            return;
        }

        Event event = new Event(title, slug, desc, startsAt);
        String coverUrl = JsonUtil.extractField(body, "coverImageUrl");
        if (coverUrl != null) event.setCoverImageUrl(coverUrl);
        String endsAt = JsonUtil.extractField(body, "endsAt");
        if (endsAt != null) event.setEndsAt(endsAt);
        String location = JsonUtil.extractField(body, "location");
        if (location != null) event.setLocation(location);
        String onlineUrl = JsonUtil.extractField(body, "onlineUrl");
        if (onlineUrl != null) event.setOnlineUrl(onlineUrl);
        String regUrl = JsonUtil.extractField(body, "registrationUrl");
        if (regUrl != null) event.setRegistrationUrl(regUrl);
        String maxStr = JsonUtil.extractField(body, "maxParticipants");
        if (maxStr != null) event.setMaxParticipants(Integer.parseInt(maxStr));
        String organizerId = JsonUtil.extractField(body, "organizerId");
        if (organizerId != null) event.setOrganizerId(organizerId);
        String courseId = JsonUtil.extractField(body, "courseId");
        if (courseId != null) event.setCourseId(courseId);
        String modalityStr = JsonUtil.extractField(body, "modality");
        if (modalityStr != null) {
            try { event.setModality(EventModality.valueOf(modalityStr)); }
            catch (IllegalArgumentException ignored) {}
        }
        String statusStr = JsonUtil.extractField(body, "status");
        if (statusStr != null) {
            try { event.setStatus(ContentStatus.valueOf(statusStr)); }
            catch (IllegalArgumentException ignored) {}
        }

        repo.save(event);
        JsonUtil.sendJson(exchange, 201, event.toJson());
    }

    private void handlePut(HttpExchange exchange, String id) throws SQLException, IOException {
        Optional<Event> existing = repo.findById(id);
        if (existing.isEmpty()) {
            JsonUtil.sendError(exchange, 404, "Evento não encontrado");
            return;
        }

        String body  = JsonUtil.readBody(exchange);
        Event event  = existing.get();

        String title = JsonUtil.extractField(body, "title");
        if (title != null) event.setTitle(title);
        String slug = JsonUtil.extractField(body, "slug");
        if (slug != null) event.setSlug(slug);
        String desc = JsonUtil.extractField(body, "description");
        if (desc != null) event.setDescription(desc);
        String startsAt = JsonUtil.extractField(body, "startsAt");
        if (startsAt != null) event.setStartsAt(startsAt);
        String endsAt = JsonUtil.extractField(body, "endsAt");
        if (endsAt != null) event.setEndsAt(endsAt);
        String location = JsonUtil.extractField(body, "location");
        if (location != null) event.setLocation(location);
        String onlineUrl = JsonUtil.extractField(body, "onlineUrl");
        if (onlineUrl != null) event.setOnlineUrl(onlineUrl);
        String regUrl = JsonUtil.extractField(body, "registrationUrl");
        if (regUrl != null) event.setRegistrationUrl(regUrl);
        String maxStr = JsonUtil.extractField(body, "maxParticipants");
        if (maxStr != null) event.setMaxParticipants(Integer.parseInt(maxStr));
        String organizerId = JsonUtil.extractField(body, "organizerId");
        if (organizerId != null) event.setOrganizerId(organizerId);
        String courseId = JsonUtil.extractField(body, "courseId");
        if (courseId != null) event.setCourseId(courseId);
        String modalityStr = JsonUtil.extractField(body, "modality");
        if (modalityStr != null) {
            try { event.setModality(EventModality.valueOf(modalityStr)); }
            catch (IllegalArgumentException ignored) {}
        }
        String statusStr = JsonUtil.extractField(body, "status");
        if (statusStr != null) {
            try { event.setStatus(ContentStatus.valueOf(statusStr)); }
            catch (IllegalArgumentException ignored) {}
        }

        repo.update(event);
        JsonUtil.sendJson(exchange, 200, event.toJson());
    }

    private void handleDelete(HttpExchange exchange, String id) throws SQLException, IOException {
        boolean deleted = repo.delete(id);
        if (deleted) JsonUtil.sendJson(exchange, 200, "{\"deleted\":true}");
        else JsonUtil.sendError(exchange, 404, "Evento não encontrado");
    }

    private String extractId(String path) {
        String[] parts = path.split("/");
        return parts.length >= 4 ? parts[3] : null;
    }
}
