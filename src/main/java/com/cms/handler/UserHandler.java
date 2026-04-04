package com.cms.handler;

import com.cms.model.User;
import com.cms.model.enums.UserRole;
import com.cms.repository.UserRepository;
import com.cms.server.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Endpoints:
 *   GET    /api/users          — lista todos
 *   GET    /api/users/{id}     — busca por ID
 *   POST   /api/users          — cria novo usuário
 *   PUT    /api/users/{id}     — atualiza
 *   DELETE /api/users/{id}     — remove
 */
public class UserHandler implements HttpHandler {

    private final UserRepository repo = new UserRepository();

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
            System.err.println("[UserHandler] " + e.getMessage());
            JsonUtil.sendError(exchange, 500, "Erro interno");
        }
    }

    private void handleGetAll(HttpExchange exchange) throws SQLException, IOException {
        List<User> users = repo.findAll();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < users.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(users.get(i).toJson());
        }
        sb.append("]");
        JsonUtil.sendJson(exchange, 200, sb.toString());
    }

    private void handleGetById(HttpExchange exchange, String id) throws SQLException, IOException {
        Optional<User> user = repo.findById(id);
        if (user.isPresent()) JsonUtil.sendJson(exchange, 200, user.get().toJson());
        else JsonUtil.sendError(exchange, 404, "Usuário não encontrado");
    }

    private void handlePost(HttpExchange exchange) throws SQLException, IOException {
        String body     = JsonUtil.readBody(exchange);
        String fullName = JsonUtil.extractField(body, "fullName");
        String email    = JsonUtil.extractField(body, "email");
        String password = JsonUtil.extractField(body, "passwordHash");

        if (fullName == null || email == null || password == null) {
            JsonUtil.sendError(exchange, 400, "fullName, email e passwordHash são obrigatórios");
            return;
        }

        User user = new User(fullName, email, password);
        String roleStr = JsonUtil.extractField(body, "role");
        if (roleStr != null) {
            try { user.setRole(UserRole.valueOf(roleStr)); }
            catch (IllegalArgumentException ignored) {}
        }

        repo.save(user);
        JsonUtil.sendJson(exchange, 201, user.toJson());
    }

    private void handlePut(HttpExchange exchange, String id) throws SQLException, IOException {
        Optional<User> existing = repo.findById(id);
        if (existing.isEmpty()) {
            JsonUtil.sendError(exchange, 404, "Usuário não encontrado");
            return;
        }

        String body = JsonUtil.readBody(exchange);
        User user   = existing.get();

        String fullName = JsonUtil.extractField(body, "fullName");
        if (fullName != null) user.setFullName(fullName);

        String avatarUrl = JsonUtil.extractField(body, "avatarUrl");
        if (avatarUrl != null) user.setAvatarUrl(avatarUrl);

        String bio = JsonUtil.extractField(body, "bio");
        if (bio != null) user.setBio(bio);

        String roleStr = JsonUtil.extractField(body, "role");
        if (roleStr != null) {
            try { user.setRole(UserRole.valueOf(roleStr)); }
            catch (IllegalArgumentException ignored) {}
        }

        repo.update(user);
        JsonUtil.sendJson(exchange, 200, user.toJson());
    }

    private void handleDelete(HttpExchange exchange, String id) throws SQLException, IOException {
        boolean deleted = repo.delete(id);
        if (deleted) JsonUtil.sendJson(exchange, 200, "{\"deleted\":true}");
        else JsonUtil.sendError(exchange, 404, "Usuário não encontrado");
    }

    private String extractId(String path) {
        // /api/users/{id}
        String[] parts = path.split("/");
        return parts.length >= 4 ? parts[3] : null;
    }
}
