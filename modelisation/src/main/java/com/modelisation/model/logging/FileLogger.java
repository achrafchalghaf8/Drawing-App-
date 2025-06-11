package com.modelisation.model.logging;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implémentation de LoggingStrategy pour la journalisation dans un fichier
 * Strategy Pattern - Stratégie concrète pour le logging fichier
 */
public class FileLogger implements LoggingStrategy {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private String logFilePath;
    private BufferedWriter writer;
    
    public FileLogger(String logFilePath) {
        this.logFilePath = logFilePath;
        initializeLogFile();
    }
    
    public FileLogger() {
        // Nom de fichier UNIQUE avec timestamp précis (incluant millisecondes)
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
        this.logFilePath = "logs/drawing_app_" + timestamp + ".log";

        // Debug: afficher le répertoire de travail
        System.out.println("FileLogger - Répertoire de travail: " + System.getProperty("user.dir"));
        System.out.println("FileLogger - Nouveau fichier de log: " + this.logFilePath);

        // FORCER la création d'un nouveau fichier à chaque fois
        forceCreateNewLogFile();

        // Vérification finale que le fichier existe
        Path logFile = Paths.get(logFilePath);
        if (Files.exists(logFile)) {
            System.out.println("FileLogger - ✅ Nouveau fichier créé avec succès: " + logFile.toAbsolutePath());
        } else {
            System.err.println("FileLogger - ❌ ERREUR: Fichier non créé: " + logFile.toAbsolutePath());
        }
    }
    
    /**
     * Force la création d'un nouveau fichier de log à chaque fois
     */
    private void forceCreateNewLogFile() {
        try {
            // Créer le répertoire logs s'il n'existe pas
            Path logDir = Paths.get(logFilePath).getParent();
            if (logDir != null && !Files.exists(logDir)) {
                Files.createDirectories(logDir);
                System.out.println("FileLogger - Répertoire logs créé: " + logDir.toAbsolutePath());
            }

            // Avec timestamp incluant millisecondes, le fichier devrait être unique
            // Mais si par hasard il existe, ajouter un suffixe
            Path logFile = Paths.get(logFilePath);
            if (Files.exists(logFile)) {
                // Très rare, mais au cas où...
                String nameWithoutExt = logFilePath.substring(0, logFilePath.lastIndexOf(".log"));
                logFilePath = nameWithoutExt + "_NOUVEAU.log";
                logFile = Paths.get(logFilePath);
                System.out.println("FileLogger - Fichier existait, nouveau nom: " + logFilePath);
            }

            // Créer le nouveau fichier
            Files.createFile(logFile);
            System.out.println("FileLogger - ✅ NOUVEAU fichier créé: " + logFile.toAbsolutePath());

            // Initialiser le writer
            this.writer = new BufferedWriter(new FileWriter(logFilePath, false)); // false = nouveau fichier

            // Écrire un en-tête avec informations de session
            writer.write("=== NOUVEAU FICHIER DE LOG ===\n");
            writer.write("=== Session démarrée le " + LocalDateTime.now().format(TIMESTAMP_FORMAT) + " ===\n");
            writer.write("=== Fichier: " + logFile.getFileName() + " ===\n");
            writer.write("=== Répertoire: " + logFile.getParent().toAbsolutePath() + " ===\n");
            writer.write("=== Basculement vers logging fichier ===\n");
            writer.write("\n");
            writer.flush();

            System.out.println("FileLogger - ✅ Fichier prêt pour logging: " + logFile.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("FileLogger - ❌ ERREUR lors de la création forcée du fichier: " + e.getMessage());
            e.printStackTrace();

            // Fallback vers l'ancienne méthode
            initializeLogFile();
        }
    }

    private void initializeLogFile() {
        try {
            // Créer le répertoire logs s'il n'existe pas
            Path logDir = Paths.get(logFilePath).getParent();
            if (logDir != null && !Files.exists(logDir)) {
                Files.createDirectories(logDir);
                System.out.println("FileLogger - Répertoire logs créé: " + logDir.toAbsolutePath());
            }

            // Créer le fichier s'il n'existe pas
            Path logFile = Paths.get(logFilePath);
            if (!Files.exists(logFile)) {
                Files.createFile(logFile);
                System.out.println("FileLogger - Fichier de log créé: " + logFile.toAbsolutePath());
            }

            // Initialiser le writer
            this.writer = new BufferedWriter(new FileWriter(logFilePath, true));

            // Écrire un en-tête
            writer.write("=== Session de logging démarrée le " +
                        LocalDateTime.now().format(TIMESTAMP_FORMAT) + " ===\n");
            writer.flush();

            System.out.println("FileLogger - Fichier de log initialisé avec succès: " + logFile.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("Erreur lors de l'initialisation du fichier de log: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void log(LogLevel level, String message) {
        try {
            if (writer != null) {
                String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
                String logEntry = String.format("[%s] %s - %s%n", timestamp, level.getLabel(), message);
                
                writer.write(logEntry);
                writer.flush(); // S'assurer que le message est écrit immédiatement
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture dans le fichier de log: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        try {
            if (writer != null) {
                String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
                String logEntry = String.format("[%s] %s - %s - Exception: %s%n", 
                                               timestamp, level.getLabel(), message, throwable.getMessage());
                
                writer.write(logEntry);
                
                // Écrire la stack trace pour les erreurs
                if (level == LogLevel.ERROR) {
                    writer.write("Stack trace:\n");
                    for (StackTraceElement element : throwable.getStackTrace()) {
                        writer.write("  at " + element.toString() + "\n");
                    }
                }
                
                writer.flush();
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture dans le fichier de log: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void close() {
        try {
            if (writer != null) {
                writer.write("=== Session de logging terminée le " + 
                           LocalDateTime.now().format(TIMESTAMP_FORMAT) + " ===\n");
                writer.close();
                writer = null;
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la fermeture du fichier de log: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public String getLogFilePath() {
        return logFilePath;
    }
}
