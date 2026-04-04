package com.cms.handler;

import com.cms.model.Course;
import com.cms.repository.CourseRepository;
import com.cms.server.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Endpoints:
 *   GET    /api/courses         — lista todos
 *   GET    /api/courses/{id}    — busca por ID
 *   POST   /api/courses         — cria
 *   PUT    /api/courses/{id}    — atualiza
 *   DELETE /api/courses/{id}    — remove
 */
public class CourseHandler implements HttpHandler {

    private final CourseRepository repo = new CourseRepository();

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
            System.err.println("[CourseHandler] " + e.getMessage());
            JsonUtil.sendError(exchange, 500, "Erro interno");
        }
    }

    private void handleGetAll(HttpExchange exchange) throws SQLException, IOException {
        List<Course> courses = repo.findAll();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < courses.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(courses.get(i).toJson());
        }
        sb.append("]");
        JsonUtil.sendJson(exchange, 200, sb.toString());
    }

    private void handleGetById(HttpExchange exchange, String id) throws SQLException, IOException {
        Optional<Course> course = repo.findById(id);
        if (course.isPresent()) JsonUtil.sendJson(exchange, 200, course.get().toJson());
        else JsonUtil.sendError(exchange, 404, "Curso não encontrado");
    }

    private void handlePost(HttpExchange exchange) throws SQLException, IOException {
        String body = JsonUtil.readBody(exchange);
        String name = JsonUtil.extractField(body, "name");
        String slug = JsonUtil.extractField(body, "slug");

        if (name == null || slug == null) {
            JsonUtil.sendError(exchange, 400, "name e slug são obrigatórios");
            return;
        }

        Course course = new Course(name, slug);
        String code  = JsonUtil.extractField(body, "code");
        if (code != null) course.setCode(code);
        String desc  = JsonUtil.extractField(body, "description");
        if (desc != null) course.setDescription(desc);
        String dur   = JsonUtil.extractField(body, "durationSemesters");
        if (dur != null) course.setDurationSemesters(Integer.parseInt(dur));
        String coordId = JsonUtil.extractField(body, "coordinatorId");
        if (coordId != null) course.setCoordinatorId(coordId);

        repo.save(course);
        JsonUtil.sendJson(exchange, 201, course.toJson());
    }

    private void handlePut(HttpExchange exchange, String id) throws SQLException, IOException {
        Optional<Course> existing = repo.findById(id);
        if (existing.isEmpty()) {
            JsonUtil.sendError(exchange, 404, "Curso não encontrado");
            return;
        }

        String body   = JsonUtil.readBody(exchange);
        Course course = existing.get();

        String name = JsonUtil.extractField(body, "name");
        if (name != null) course.setName(name);
        String slug = JsonUtil.extractField(body, "slug");
        if (slug != null) course.setSlug(slug);
        String code = JsonUtil.extractField(body, "code");
        if (code != null) course.setCode(code);
        String desc = JsonUtil.extractField(body, "description");
        if (desc != null) course.setDescription(desc);
        String dur  = JsonUtil.extractField(body, "durationSemesters");
        if (dur != null) course.setDurationSemesters(Integer.parseInt(dur));
        String coordId = JsonUtil.extractField(body, "coordinatorId");
        if (coordId != null) course.setCoordinatorId(coordId);

        repo.update(course);
        JsonUtil.sendJson(exchange, 200, course.toJson());
    }

    private void handleDelete(HttpExchange exchange, String id) throws SQLException, IOException {
        boolean deleted = repo.delete(id);
        if (deleted) JsonUtil.sendJson(exchange, 200, "{\"deleted\":true}");
        else JsonUtil.sendError(exchange, 404, "Curso não encontrado");
    }

    private String extractId(String path) {
        String[] parts = path.split("/");
        return parts.length >= 4 ? parts[3] : null;
    }
}
