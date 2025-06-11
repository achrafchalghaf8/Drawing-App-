package com.modelisation.model.logging;

import com.modelisation.database.DatabaseManager;

/**
 * Implémentation de LoggingStrategy pour la journalisation dans une base de données
 * Strategy Pattern - Stratégie concrète pour le logging en base de données
 */
public class DatabaseLogger implements LoggingStrategy {

    private final DatabaseManager databaseManager;

    public DatabaseLogger() {
        this.databaseManager = DatabaseManager.getInstance();
    }
    
    @Override
    public void log(LogLevel level, String message) {
        try {
            databaseManager.logToDatabase(level.name(), message, null, "GENERAL");
        } catch (Exception e) {
            // En cas d'erreur avec la base de données, fallback vers la console
            System.err.println("Erreur lors de l'enregistrement du log en base de données: " + e.getMessage());
            System.err.println("Log original: [" + level.name() + "] " + message);
        }
    }
    
    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        String exceptionDetails = throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
        if (throwable.getCause() != null) {
            exceptionDetails += " (Cause: " + throwable.getCause().getMessage() + ")";
        }

        try {
            databaseManager.logToDatabase(level.name(), message, exceptionDetails, "EXCEPTION");
        } catch (Exception e) {
            // En cas d'erreur avec la base de données, fallback vers la console
            System.err.println("Erreur lors de l'enregistrement du log en base de données: " + e.getMessage());
            System.err.println("Log original: [" + level.name() + "] " + message + " - Exception: " + exceptionDetails);
        }
    }
    
    @Override
    public void close() {
        try {
            log(LogLevel.INFO, "Database logger fermé");
        } catch (Exception e) {
            System.err.println("Erreur lors de la fermeture du database logger: " + e.getMessage());
        }
        // Note: On ne ferme pas le DatabaseManager ici car il peut être utilisé ailleurs
    }
}
