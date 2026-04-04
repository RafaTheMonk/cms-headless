package com.cms.handler;

import com.cms.model.News;
import com.cms.model.enums.ContentStatus;
import com.cms.repository.NewsRepository;
import com.cms.server.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Endpoints:
 *   GET    /api/news              — lista todas (query ?status=published)
 *   GET    /api/news/{id}         — busca por ID
 *   POST   /api/news              — cria
 *   PUT    /api/news/{id}         — atualiza
 *   DELETE /api/news/{id}         — remove
 */
public class NewsHandler implements HttpHandler {

    private final NewsRepository repo = new NewsRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JsonUtil.setCorsHeaders(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        try {
            String method = exchange.getRequestMethod().toUpperCase();
            String path   = exchange.getRequestURI().getPath();
            String id     = extractId(path);

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
            System.err.println("[NewsHandler] " + e.getMessage());
            JsonUtil.sendError(exchange, 500, "Erro interno");
        }
    }

    private void handleGetAll(HttpExchange exchange) throws SQLException, IOException {
        String query     = exchange.getRequestURI().getQuery();
        List<News> items;

        if (query != null && query.startsWith("status=")) {
            String statusStr = query.substring(7);
            try {
                ContentStatus status = ContentStatus.valueOf(statusStr);
                items = repo.findByStatus(status);
            } catch (IllegalArgumentException e) {
                JsonUtil.sendError(exchange, 400, "Status inválido: " + statusStr);
                return;
            }
        } else {
            items = repo.findAll();
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(items.get(i).toJson());
        }
        sb.append("]");
        JsonUtil.sendJson(exchange, 200, sb.toString());
    }

    private void handleGetById(HttpExchange exchange, String id) throws SQLException, IOException {
        Optional<News> news = repo.findById(id);
        if (news.isPresent()) JsonUtil.sendJson(exchange, 200, news.get().toJson());
        else JsonUtil.sendError(exchange, 404, "Notícia não encontrada");
    }

    private void handlePost(HttpExchange exchange) throws SQLException, IOException {
        String body     = JsonUtil.readBody(exchange);
        String title    = JsonUtil.extractField(body, "title");
        String slug     = JsonUtil.extractField(body, "slug");
        String newsBody = JsonUtil.extractField(body, "body");
        String authorId = JsonUtil.extractField(body, "authorId");

        if (title == null || slug == null || newsBody == null || authorId == null) {
            JsonUtil.sendError(exchange, 400, "title, slug, body e authorId são obrigatórios");
            return;
        }

        News news = new News(title, slug, newsBody, authorId);
        String subtitle = JsonUtil.extractField(body, "subtitle");
        if (subtitle != null) news.setSubtitle(subtitle);
        String coverUrl = JsonUtil.extractField(body, "coverImageUrl");
        if (coverUrl != null) news.setCoverImageUrl(coverUrl);
        String courseId = JsonUtil.extractField(body, "courseId");
        if (courseId != null) news.setCourseId(courseId);
        String statusStr = JsonUtil.extractField(body, "status");
        if (statusStr != null) {
            try { news.setStatus(ContentStatus.valueOf(statusStr)); }
            catch (IllegalArgumentException ignored) {}
        }

        repo.save(news);
        JsonUtil.sendJson(exchange, 201, news.toJson());
    }

    private void handlePut(HttpExchange exchange, String id) throws SQLException, IOException {
        Optional<News> existing = repo.findById(id);
        if (existing.isEmpty()) {
            JsonUtil.sendError(exchange, 404, "Notícia não encontrada");
            return;
        }

        String body = JsonUtil.readBody(exchange);
        News news   = existing.get();

        String title = JsonUtil.extractField(body, "title");
        if (title != null) news.setTitle(title);
        String slug = JsonUtil.extractField(body, "slug");
        if (slug != null) news.setSlug(slug);
        String subtitle = JsonUtil.extractField(body, "subtitle");
        if (subtitle != null) news.setSubtitle(subtitle);
        String newsBody = JsonUtil.extractField(body, "body");
        if (newsBody != null) news.setBody(newsBody);
        String coverUrl = JsonUtil.extractField(body, "coverImageUrl");
        if (coverUrl != null) news.setCoverImageUrl(coverUrl);
        String courseId = JsonUtil.extractField(body, "courseId");
        if (courseId != null) news.setCourseId(courseId);
        String statusStr = JsonUtil.extractField(body, "status");
        if (statusStr != null) {
            try { news.setStatus(ContentStatus.valueOf(statusStr)); }
            catch (IllegalArgumentException ignored) {}
        }

        repo.update(news);
        JsonUtil.sendJson(exchange, 200, news.toJson());
    }

    private void handleDelete(HttpExchange exchange, String id) throws SQLException, IOException {
        boolean deleted = repo.delete(id);
        if (deleted) JsonUtil.sendJson(exchange, 200, "{\"deleted\":true}");
        else JsonUtil.sendError(exchange, 404, "Notícia não encontrada");
    }

    private String extractId(String path) {
        String[] parts = path.split("/");
        return parts.length >= 4 ? parts[3] : null;
    }
}
