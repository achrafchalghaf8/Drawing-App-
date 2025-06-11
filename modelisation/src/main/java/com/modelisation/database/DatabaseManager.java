package com.modelisation.database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Gestionnaire de connexion et d'opérations sur la base de données MySQL
 */
public class DatabaseManager {
    
    private static DatabaseManager instance;
    private Connection connection;
    private final DatabaseConfig config;
    private String sessionId;
    
    private DatabaseManager() {
        this.config = DatabaseConfig.getInstance();
        this.sessionId = UUID.randomUUID().toString();

        // Exécuter la migration automatique
        runAutoMigration();

        initializeConnection();
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Exécute la migration automatique de la base de données
     */
    private void runAutoMigration() {
        try {
            DatabaseMigration migration = new DatabaseMigration();

            System.out.println("🔄 Vérification de la base de données...");

            if (migration.isMigrationNeeded()) {
                System.out.println("📦 Migration de la base de données nécessaire, exécution en cours...");

                if (migration.runMigration()) {
                    System.out.println("✅ Migration automatique réussie !");
                } else {
                    System.err.println("❌ Échec de la migration automatique");
                }
            } else {
                System.out.println("✅ Base de données déjà configurée");
            }

        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la vérification de la base de données : " + e.getMessage());
            System.err.println("L'application continuera avec les fonctionnalités limitées");
        }
    }
    
    private void initializeConnection() {
        try {
            // Charger le driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Établir la connexion
            connection = DriverManager.getConnection(
                config.getJdbcUrl(),
                config.getUsername(),
                config.getPassword()
            );
            
            System.out.println("✅ Connexion à MySQL établie avec succès !");
            config.printConfiguration();
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion à MySQL: " + e.getMessage());
            System.err.println("Vérifiez que MySQL est démarré et que la base de données existe.");
        }
    }
    
    public Connection getConnection() {
        try {
            // Vérifier si la connexion est toujours valide
            if (connection == null || connection.isClosed()) {
                initializeConnection();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de la connexion: " + e.getMessage());
            initializeConnection();
        }
        return connection;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * Enregistre un log dans la base de données
     */
    public void logToDatabase(String level, String message, String exceptionDetails, String userAction) {
        String sql = "INSERT INTO application_logs (level, message, exception_details, session_id, user_action) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, level);
            stmt.setString(2, message);
            stmt.setString(3, exceptionDetails);
            stmt.setString(4, sessionId);
            stmt.setString(5, userAction);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            // Ne pas créer de boucle infinie en loggant l'erreur de log
            System.err.println("Erreur lors de l'enregistrement du log: " + e.getMessage());
        }
    }
    
    /**
     * Sauvegarde un dessin dans la base de données
     */
    public int saveDrawing(String name, String description, String jsonData, int shapeCount) {
        String sql = "INSERT INTO drawings (name, description, json_data, shape_count, session_id) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, jsonData);
            stmt.setInt(4, shapeCount);
            stmt.setString(5, sessionId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int drawingId = generatedKeys.getInt(1);
                        logToDatabase("INFO", "Dessin sauvegardé: " + name + " (ID: " + drawingId + ")", null, "SAVE_DRAWING");
                        return drawingId;
                    }
                }
            }
            
        } catch (SQLException e) {
            logToDatabase("ERROR", "Erreur lors de la sauvegarde du dessin: " + name, e.getMessage(), "SAVE_DRAWING");
            System.err.println("Erreur lors de la sauvegarde du dessin: " + e.getMessage());
        }
        
        return -1; // Erreur
    }
    
    /**
     * Met à jour un dessin existant
     */
    public boolean updateDrawing(int drawingId, String name, String description, String jsonData, int shapeCount) {
        String sql = "UPDATE drawings SET name = ?, description = ?, json_data = ?, shape_count = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, jsonData);
            stmt.setInt(4, shapeCount);
            stmt.setInt(5, drawingId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                logToDatabase("INFO", "Dessin mis à jour: " + name + " (ID: " + drawingId + ")", null, "UPDATE_DRAWING");
                return true;
            }
            
        } catch (SQLException e) {
            logToDatabase("ERROR", "Erreur lors de la mise à jour du dessin: " + name, e.getMessage(), "UPDATE_DRAWING");
            System.err.println("Erreur lors de la mise à jour du dessin: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Enregistre une session de calcul de plus court chemin
     */
    public void saveShortestPathSession(int drawingId, String algorithm, String startLabel, String endLabel, 
                                      int pathLength, double totalDistance, String pathNodes, long executionTimeMs) {
        String sql = "INSERT INTO shortest_path_sessions (drawing_id, algorithm_used, start_shape_label, end_shape_label, path_length, total_distance, path_nodes, execution_time_ms) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, drawingId);
            stmt.setString(2, algorithm);
            stmt.setString(3, startLabel);
            stmt.setString(4, endLabel);
            stmt.setInt(5, pathLength);
            stmt.setDouble(6, totalDistance);
            stmt.setString(7, pathNodes);
            stmt.setLong(8, executionTimeMs);
            
            stmt.executeUpdate();
            
            logToDatabase("INFO", 
                String.format("Plus court chemin calculé: %s → %s (Algorithme: %s, Distance: %.2f, Temps: %dms)", 
                    startLabel, endLabel, algorithm, totalDistance, executionTimeMs), 
                null, "SHORTEST_PATH_CALCULATION");
            
        } catch (SQLException e) {
            logToDatabase("ERROR", "Erreur lors de l'enregistrement de la session plus court chemin", e.getMessage(), "SHORTEST_PATH_CALCULATION");
            System.err.println("Erreur lors de l'enregistrement de la session plus court chemin: " + e.getMessage());
        }
    }
    
    /**
     * Teste la connexion à la base de données
     */
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                // Test simple avec une requête
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT 1");
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.err.println("Test de connexion échoué: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Ferme la connexion à la base de données
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connexion à la base de données fermée.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
        }
    }
}
