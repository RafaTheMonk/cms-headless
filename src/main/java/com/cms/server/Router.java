package com.cms.server;

import com.sun.net.httpserver.HttpServer;
import com.cms.handler.ArticleHandler;
import com.cms.handler.ContentHandler;
import com.cms.handler.CourseHandler;
import com.cms.handler.EventHandler;
import com.cms.handler.NewsHandler;
import com.cms.handler.ProjectHandler;
import com.cms.handler.UserHandler;

/**
 * Centralized route registration.
 *
 * Rotas disponíveis:
 *   GET/POST/PUT/DELETE /api/users
 *   GET/POST/PUT/DELETE /api/courses
 *   GET/POST/PUT/DELETE /api/news
 *   GET/POST/PUT/DELETE /api/articles
 *   GET/POST/PUT/DELETE /api/projects
 *   GET/POST/PUT/DELETE /api/events
 *   GET/POST/PUT/DELETE /api/content   (legado)
 *   GET                 /health
 */
public class Router {

    public static void registerRoutes(HttpServer server) {
        server.createContext("/api/users",    new UserHandler());
        server.createContext("/api/courses",  new CourseHandler());
        server.createContext("/api/news",     new NewsHandler());
        server.createContext("/api/articles", new ArticleHandler());
        server.createContext("/api/projects", new ProjectHandler());
        server.createContext("/api/events",   new EventHandler());
        server.createContext("/api/content",  new ContentHandler());

        server.createContext("/health", exchange -> {
            String response = "{\"status\":\"ok\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        });
    }
}