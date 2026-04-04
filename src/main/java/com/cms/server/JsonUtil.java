package com.cms.server;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Lightweight JSON helpers.
 * 
 * All JSON reading/writing is isolated here. When you later add
 * a library like Gson, you only change THIS file — nothing else.
 * 
 * This is the "Single Responsibility Principle" in action:
 * one class, one job — handling JSON.
 */
public class JsonUtil {

    /**
     * Sends a JSON string as an HTTP response.
     * 
     * HTTP responses have two parts:
     *   1. Headers — metadata (Content-Type, status code, CORS)
     *   2. Body — the actual data (your JSON)
     * You MUST send headers BEFORE the body.
     */
    public static void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

        exchange.sendResponseHeaders(statusCode, bytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    /**
     * Reads the entire request body as a String.
     * Used for POST/PUT requests where the client sends JSON.
     */
    public static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        is.close();
        return body;
    }

    /**
     * Sends a standardized error response.
     * Always returns: {"error": "your message here"}
     */
    public static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String json = "{\"error\":\"" + escapeJson(message) + "\"}";
        sendJson(exchange, statusCode, json);
    }

    /**
     * Escapes special characters inside JSON string values.
     * Without this, a quote inside your content would break the JSON.
     * 
     * Example: He said "hello" → He said \"hello\"
     */
    public static String escapeJson(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Alias for readRequestBody — uniform name used across all handlers.
     */
    public static String readBody(HttpExchange exchange) throws IOException {
        return readRequestBody(exchange);
    }

    /**
     * Alias for extractJsonValue — uniform name used across all handlers.
     */
    public static String extractField(String json, String key) {
        return extractJsonValue(json, key);
    }

    /**
     * Sets CORS headers without sending a response.
     * Call this at the start of every handler before branching on method.
     */
    public static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    /**
     * Dead-simple JSON value extractor.
     * Finds "key":"value" and returns the value.
     *
     * LIMITATION: only works for flat string fields.
     * Will be replaced by Gson later.
     */
    public static String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;

        int colonIndex = json.indexOf(":", keyIndex + searchKey.length());
        if (colonIndex == -1) return null;

        int valueStart = json.indexOf("\"", colonIndex + 1);
        if (valueStart == -1) return null;

        int valueEnd = valueStart + 1;
        while (valueEnd < json.length()) {
            if (json.charAt(valueEnd) == '"' && json.charAt(valueEnd - 1) != '\\') {
                break;
            }
            valueEnd++;
        }

        return json.substring(valueStart + 1, valueEnd);
    }
}