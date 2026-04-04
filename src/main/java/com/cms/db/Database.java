package com.cms.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton de conexão com o PostgreSQL.
 *
 * Configuração via variáveis de ambiente:
 *   DB_URL      - ex: jdbc:postgresql://localhost:5432/ucsal_cms
 *   DB_USER     - ex: postgres
 *   DB_PASSWORD - ex: secret
 *
 * Se as variáveis não estiverem definidas, usa os valores padrão abaixo.
 */
public class Database {

    private static final String DEFAULT_URL      = "jdbc:postgresql://localhost:5432/ucsal_cms";
    private static final String DEFAULT_USER     = "postgres";
    private static final String DEFAULT_PASSWORD = "postgres";

    private Database() {}

    /**
     * Abre e retorna uma nova conexão com o banco.
     * O chamador é responsável por fechar a conexão (use try-with-resources).
     *
     * Exemplo de uso:
     *   try (Connection conn = Database.getConnection()) {
     *       // ...
     *   }
     */
    public static Connection getConnection() throws SQLException {
        String url      = System.getenv().getOrDefault("DB_URL",      DEFAULT_URL);
        String user     = System.getenv().getOrDefault("DB_USER",     DEFAULT_USER);
        String password = System.getenv().getOrDefault("DB_PASSWORD", DEFAULT_PASSWORD);
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Testa a conexão com o banco na inicialização.
     * Lança RuntimeException se não conseguir conectar.
     */
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn.isValid(2)) {
                System.out.println("[DB] Conexão com PostgreSQL estabelecida.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("[DB] Falha ao conectar com o PostgreSQL: " + e.getMessage(), e);
        }
    }
}
