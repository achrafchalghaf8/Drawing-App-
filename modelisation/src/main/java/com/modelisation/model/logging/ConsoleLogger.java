package com.modelisation.model.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implémentation de LoggingStrategy pour la journalisation dans la console
 * Strategy Pattern - Stratégie concrète pour le logging console
 */
public class ConsoleLogger implements LoggingStrategy {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public void log(LogLevel level, String message) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String logEntry = String.format("[%s] %s - %s", timestamp, level.getLabel(), message);
        
        // Utiliser System.err pour les erreurs, System.out pour le reste
        if (level == LogLevel.ERROR) {
            System.err.println(logEntry);
        } else {
            System.out.println(logEntry);
        }
    }
    
    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        log(level, message + " - Exception: " + throwable.getMessage());
        
        // Afficher la stack trace pour les erreurs
        if (level == LogLevel.ERROR) {
            throwable.printStackTrace();
        }
    }
    
    @Override
    public void close() {
        // Rien à fermer pour la console
        log(LogLevel.INFO, "Console logger fermé");
    }
}
