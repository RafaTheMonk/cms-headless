package com.cms.handler;

import com.cms.model.Article;
import com.cms.model.enums.ContentStatus;
import com.cms.repository.ArticleRepository;
import com.cms.server.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Endpoints:
 *   GET    /api/articles              — lista todos (query ?status=published)
 *   GET    /api/articles/{id}         — busca por ID
 *   POST   /api/articles              — cria
 *   PUT    /api/articles/{id}         — atualiza
 *   DELETE /api/articles/{id}         — remove
 */
public class ArticleHandler implements HttpHandler {

    private final ArticleRepository repo = new ArticleRepository();

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
            System.err.println("[ArticleHandler] " + e.getMessage());
            JsonUtil.sendError(exchange, 500, "Erro interno");
        }
    }

    private void handleGetAll(HttpExchange exchange) throws SQLException, IOException {
        String query = exchange.getRequestURI().getQuery();
        List<Article> items;

        if (query != null && query.startsWith("status=")) {
            String statusStr = query.substring(7);
            try {
                items = repo.findByStatus(ContentStatus.valueOf(statusStr));
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
        Optional<Article> article = repo.findById(id);
        if (article.isPresent()) JsonUtil.sendJson(exchange, 200, article.get().toJson());
        else JsonUtil.sendError(exchange, 404, "Artigo não encontrado");
    }

    private void handlePost(HttpExchange exchange) throws SQLException, IOException {
        String body     = JsonUtil.readBody(exchange);
        String title    = JsonUtil.extractField(body, "title");
        String slug     = JsonUtil.extractField(body, "slug");
        String artBody  = JsonUtil.extractField(body, "body");
        String authorId = JsonUtil.extractField(body, "authorId");

        if (title == null || slug == null || artBody == null || authorId == null) {
            JsonUtil.sendError(exchange, 400, "title, slug, body e authorId são obrigatórios");
            return;
        }

        Article article = new Article(title, slug, artBody, authorId);
        String abs = JsonUtil.extractField(body, "abstract");
        if (abs != null) article.setAbstractText(abs);
        String coverUrl = JsonUtil.extractField(body, "coverImageUrl");
        if (coverUrl != null) article.setCoverImageUrl(coverUrl);
        String courseId = JsonUtil.extractField(body, "courseId");
        if (courseId != null) article.setCourseId(courseId);
        String statusStr = JsonUtil.extractField(body, "status");
        if (statusStr != null) {
            try { article.setStatus(ContentStatus.valueOf(statusStr)); }
            catch (IllegalArgumentException ignored) {}
        }

        repo.save(article);
        JsonUtil.sendJson(exchange, 201, article.toJson());
    }

    private void handlePut(HttpExchange exchange, String id) throws SQLException, IOException {
        Optional<Article> existing = repo.findById(id);
        if (existing.isEmpty()) {
            JsonUtil.sendError(exchange, 404, "Artigo não encontrado");
            return;
        }

        String body    = JsonUtil.readBody(exchange);
        Article article = existing.get();

        String title = JsonUtil.extractField(body, "title");
        if (title != null) article.setTitle(title);
        String slug = JsonUtil.extractField(body, "slug");
        if (slug != null) article.setSlug(slug);
        String abs = JsonUtil.extractField(body, "abstract");
        if (abs != null) article.setAbstractText(abs);
        String artBody = JsonUtil.extractField(body, "body");
        if (artBody != null) article.setBody(artBody);
        String coverUrl = JsonUtil.extractField(body, "coverImageUrl");
        if (coverUrl != null) article.setCoverImageUrl(coverUrl);
        String courseId = JsonUtil.extractField(body, "courseId");
        if (courseId != null) article.setCourseId(courseId);
        String statusStr = JsonUtil.extractField(body, "status");
        if (statusStr != null) {
            try { article.setStatus(ContentStatus.valueOf(statusStr)); }
            catch (IllegalArgumentException ignored) {}
        }

        repo.update(article);
        JsonUtil.sendJson(exchange, 200, article.toJson());
    }

    private void handleDelete(HttpExchange exchange, String id) throws SQLException, IOException {
        boolean deleted = repo.delete(id);
        if (deleted) JsonUtil.sendJson(exchange, 200, "{\"deleted\":true}");
        else JsonUtil.sendError(exchange, 404, "Artigo não encontrado");
    }

    private String extractId(String path) {
        String[] parts = path.split("/");
        return parts.length >= 4 ? parts[3] : null;
    }
}
