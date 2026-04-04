package com.cms;

import com.sun.net.httpserver.HttpServer;
import com.cms.db.Database;
import com.cms.server.Router;

import java.net.InetSocketAddress;

/**
 * Entry point for the Headless CMS.
 * 
 * Uses Java's built-in HttpServer — no external frameworks.
 * The HttpServer class ships with the JDK since Java 6.
 * It listens for HTTP connections on a given port and dispatches
 * requests to registered handlers.
 */
public class App {

    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        // Verifica conexão com o PostgreSQL antes de subir o servidor
        Database.testConnection();

        // Creates a server socket bound to port 8080
        // The 0 means "use system default backlog" (queue of pending connections)
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Register all API routes
        Router.registerRoutes(server);

        // null = use the default executor (single-threaded)
        // For production, swap to a thread pool:
        //   server.setExecutor(Executors.newFixedThreadPool(10));
        server.setExecutor(null);

        server.start();
        System.out.println("=================================");
        System.out.println("  Headless CMS is running!");
        System.out.println("  http://localhost:" + PORT);
        System.out.println("=================================");
        System.out.println("");
        System.out.println("Endpoints:");
        System.out.println("  GET    /health                → status");
        System.out.println("  CRUD   /api/users             → usuários");
        System.out.println("  CRUD   /api/courses           → cursos");
        System.out.println("  CRUD   /api/news              → notícias");
        System.out.println("  CRUD   /api/articles          → artigos");
        System.out.println("  CRUD   /api/projects          → projetos");
        System.out.println("  CRUD   /api/events            → eventos");
    }
}