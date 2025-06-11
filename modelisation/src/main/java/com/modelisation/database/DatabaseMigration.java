package com.modelisation.database;

import java.sql.*;

/**
 * Utilitaire de migration automatique de la base de données
 * Crée automatiquement la base de données et les tables si elles n'existent pas
 */
public class DatabaseMigration {
    
    private final DatabaseConfig config;
    
    public DatabaseMigration() {
        this.config = DatabaseConfig.getInstance();
    }
    
    /**
     * Exécute la migration complète : création de la base et des tables
     */
    public boolean runMigration() {
        System.out.println("=== DÉBUT DE LA MIGRATION AUTOMATIQUE ===");
        
        try {
            // Étape 1 : Créer la base de données si elle n'existe pas
            if (createDatabaseIfNotExists()) {
                System.out.println("✅ Base de données vérifiée/créée avec succès");
            } else {
                System.err.println("❌ Échec de la création de la base de données");
                return false;
            }
            
            // Étape 2 : Créer les tables
            if (createTables()) {
                System.out.println("✅ Tables créées/vérifiées avec succès");
            } else {
                System.err.println("❌ Échec de la création des tables");
                return false;
            }
            
            System.out.println("=== MIGRATION TERMINÉE AVEC SUCCÈS ===");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la migration : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Crée la base de données si elle n'existe pas
     */
    private boolean createDatabaseIfNotExists() {
        String serverUrl = String.format("jdbc:mysql://%s:%s/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                config.getHost(), config.getPort());
        
        try (Connection conn = DriverManager.getConnection(serverUrl, config.getUsername(), config.getPassword())) {
            
            String createDbSql = "CREATE DATABASE IF NOT EXISTS `" + config.getDatabase() + "` " +
                               "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
            
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(createDbSql);
                System.out.println("Base de données '" + config.getDatabase() + "' vérifiée/créée");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la base de données : " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Crée toutes les tables nécessaires
     */
    private boolean createTables() {
        try (Connection conn = DriverManager.getConnection(config.getJdbcUrl(), 
                                                          config.getUsername(), 
                                                          config.getPassword())) {
            
            // Table des logs
            createApplicationLogsTable(conn);
            
            // Table des dessins
            createDrawingsTable(conn);
            
            // Table des formes
            createShapesTable(conn);
            
            // Table des sessions de plus court chemin
            createShortestPathSessionsTable(conn);
            
            // Créer les index
            createIndexes(conn);
            
            return true;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création des tables : " + e.getMessage());
            return false;
        }
    }
    
    private void createApplicationLogsTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS application_logs (
                id INT AUTO_INCREMENT PRIMARY KEY,
                timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                level ENUM('DEBUG', 'INFO', 'WARNING', 'ERROR') NOT NULL,
                message TEXT NOT NULL,
                exception_details TEXT NULL,
                session_id VARCHAR(255) NULL,
                user_action VARCHAR(255) NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("✓ Table 'application_logs' créée/vérifiée");
        }
    }
    
    private void createDrawingsTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS drawings (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                description TEXT NULL,
                json_data LONGTEXT NOT NULL,
                shape_count INT NOT NULL DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                session_id VARCHAR(255) NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("✓ Table 'drawings' créée/vérifiée");
        }
    }
    
    private void createShapesTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS shapes (
                id INT AUTO_INCREMENT PRIMARY KEY,
                drawing_id INT NOT NULL,
                shape_type ENUM('CIRCLE', 'RECTANGLE', 'LINE') NOT NULL,
                shape_label VARCHAR(10) NOT NULL,
                position_x DOUBLE NOT NULL,
                position_y DOUBLE NOT NULL,
                param1 DOUBLE NULL,
                param2 DOUBLE NULL,
                color VARCHAR(20) NOT NULL,
                stroke_width DOUBLE NOT NULL DEFAULT 2.0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (drawing_id) REFERENCES drawings(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("✓ Table 'shapes' créée/vérifiée");
        }
    }
    
    private void createShortestPathSessionsTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS shortest_path_sessions (
                id INT AUTO_INCREMENT PRIMARY KEY,
                drawing_id INT NOT NULL,
                algorithm_used ENUM('DIJKSTRA', 'BFS') NOT NULL,
                start_shape_label VARCHAR(10) NOT NULL,
                end_shape_label VARCHAR(10) NOT NULL,
                path_length INT NOT NULL,
                total_distance DOUBLE NOT NULL,
                path_nodes TEXT NOT NULL,
                execution_time_ms BIGINT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (drawing_id) REFERENCES drawings(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("✓ Table 'shortest_path_sessions' créée/vérifiée");
        }
    }
    
    private void createIndexes(Connection conn) throws SQLException {
        String[] indexes = {
            "CREATE INDEX IF NOT EXISTS idx_logs_timestamp ON application_logs(timestamp)",
            "CREATE INDEX IF NOT EXISTS idx_logs_level ON application_logs(level)",
            "CREATE INDEX IF NOT EXISTS idx_drawings_created ON drawings(created_at)",
            "CREATE INDEX IF NOT EXISTS idx_shapes_drawing ON shapes(drawing_id)"
        };
        
        try (Statement stmt = conn.createStatement()) {
            for (String indexSql : indexes) {
                try {
                    stmt.executeUpdate(indexSql);
                } catch (SQLException e) {
                    // Ignorer si l'index existe déjà
                    if (!e.getMessage().contains("Duplicate key name")) {
                        throw e;
                    }
                }
            }
            System.out.println("✓ Index créés/vérifiés");
        }
    }
    
    /**
     * Vérifie si la migration est nécessaire
     */
    public boolean isMigrationNeeded() {
        try (Connection conn = DriverManager.getConnection(config.getJdbcUrl(), 
                                                          config.getUsername(), 
                                                          config.getPassword())) {
            
            // Vérifier si la table principale existe
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet tables = metaData.getTables(null, null, "drawings", null)) {
                return !tables.next(); // Retourne true si la table n'existe pas
            }
            
        } catch (SQLException e) {
            // Si on ne peut pas se connecter à la base, migration nécessaire
            return true;
        }
    }
    
    /**
     * Point d'entrée pour exécuter la migration depuis la ligne de commande
     */
    public static void main(String[] args) {
        DatabaseMigration migration = new DatabaseMigration();
        
        System.out.println("Configuration actuelle :");
        migration.config.printConfiguration();
        System.out.println();
        
        if (migration.runMigration()) {
            System.out.println("🎉 Migration réussie ! L'application peut maintenant utiliser la base de données.");
            System.exit(0);
        } else {
            System.err.println("💥 Échec de la migration. Vérifiez votre configuration MySQL.");
            System.exit(1);
        }
    }
}
