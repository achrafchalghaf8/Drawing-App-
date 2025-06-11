package com.modelisation.model.logging;

import com.modelisation.database.DatabaseManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Stratégie de logging qui enregistre les logs dans la base de données MySQL
 */
public class DatabaseLoggingStrategy implements LoggingStrategy {
    
    private final DatabaseManager databaseManager;
    private final DateTimeFormatter formatter;
    private boolean isEnabled;
    
    public DatabaseLoggingStrategy() {
        this.databaseManager = DatabaseManager.getInstance();
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.isEnabled = true;
        
        // Tester la connexion au démarrage
        if (databaseManager.testConnection()) {
            System.out.println("✅ DatabaseLoggingStrategy initialisé avec succès");
            log(LogLevel.INFO, "Système de logging base de données démarré");
        } else {
            System.err.println("❌ Impossible de se connecter à la base de données pour le logging");
            this.isEnabled = false;
        }
    }
    
    @Override
    public void log(LogLevel level, String message) {
        log(level, message, null);
    }
    
    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        if (!isEnabled) {
            // Fallback vers la console si la base de données n'est pas disponible
            System.out.println(formatForConsole(level, message, throwable));
            return;
        }
        
        try {
            String exceptionDetails = null;
            if (throwable != null) {
                exceptionDetails = throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
                if (throwable.getCause() != null) {
                    exceptionDetails += " (Cause: " + throwable.getCause().getMessage() + ")";
                }
            }
            
            // Enregistrer dans la base de données
            databaseManager.logToDatabase(
                level.name(),
                message,
                exceptionDetails,
                determineUserAction(message)
            );
            
            // Aussi afficher dans la console pour le debug
            System.out.println(formatForConsole(level, message, throwable));
            
        } catch (Exception e) {
            // En cas d'erreur avec la base de données, utiliser la console
            System.err.println("Erreur lors de l'enregistrement du log en base: " + e.getMessage());
            System.out.println(formatForConsole(level, message, throwable));
            
            // Désactiver temporairement le logging en base
            this.isEnabled = false;
        }
    }
    
    /**
     * Formate le message pour l'affichage console (backup)
     */
    private String formatForConsole(LogLevel level, String message, Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(LocalDateTime.now().format(formatter)).append("] ");
        sb.append(level.name()).append(" - ");
        sb.append(message);
        
        if (throwable != null) {
            sb.append(" | Exception: ").append(throwable.getClass().getSimpleName());
            sb.append(": ").append(throwable.getMessage());
        }
        
        return sb.toString();
    }
    
    /**
     * Détermine l'action utilisateur basée sur le message de log
     */
    private String determineUserAction(String message) {
        if (message == null) return null;
        
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("forme ajoutée") || lowerMessage.contains("shape added")) {
            return "CREATE_SHAPE";
        } else if (lowerMessage.contains("forme supprimée") || lowerMessage.contains("shape deleted")) {
            return "DELETE_SHAPE";
        } else if (lowerMessage.contains("shortest path") || lowerMessage.contains("plus court chemin")) {
            return "SHORTEST_PATH";
        } else if (lowerMessage.contains("sauvegardé") || lowerMessage.contains("saved")) {
            return "SAVE_FILE";
        } else if (lowerMessage.contains("chargé") || lowerMessage.contains("loaded")) {
            return "LOAD_FILE";
        } else if (lowerMessage.contains("algorithme") || lowerMessage.contains("algorithm")) {
            return "CHANGE_ALGORITHM";
        } else if (lowerMessage.contains("type de forme") || lowerMessage.contains("shape type")) {
            return "CHANGE_SHAPE_TYPE";
        } else if (lowerMessage.contains("mode") && lowerMessage.contains("activé")) {
            return "CHANGE_MODE";
        } else if (lowerMessage.contains("dessin") || lowerMessage.contains("drawing")) {
            return "DRAWING_ACTION";
        }
        
        return "GENERAL";
    }
    
    /**
     * Réactive le logging en base de données après une erreur
     */
    public void reconnect() {
        if (databaseManager.testConnection()) {
            this.isEnabled = true;
            log(LogLevel.INFO, "Connexion base de données rétablie pour le logging");
        }
    }
    
    /**
     * Vérifie si le logging en base est actif
     */
    public boolean isDatabaseEnabled() {
        return isEnabled;
    }
    
    @Override
    public void close() {
        if (isEnabled) {
            log(LogLevel.INFO, "Fermeture du système de logging base de données");
        }
        // La connexion sera fermée par le DatabaseManager
    }
    
    /**
     * Enregistre une action spécifique avec plus de détails
     */
    public void logUserAction(LogLevel level, String message, String actionType) {
        if (!isEnabled) {
            System.out.println(formatForConsole(level, message, null));
            return;
        }
        
        try {
            databaseManager.logToDatabase(
                level.name(),
                message,
                null,
                actionType
            );
            
            System.out.println(formatForConsole(level, message, null));
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement de l'action utilisateur: " + e.getMessage());
            System.out.println(formatForConsole(level, message, null));
        }
    }
}
