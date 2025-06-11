import com.modelisation.model.logging.FileLogger;
import com.modelisation.model.logging.LoggingStrategy;

class TestLogger {
    public static void main(String[] args) {
        System.out.println("Test du FileLogger...");
        
        // Créer un FileLogger
        FileLogger logger = new FileLogger();
        
        System.out.println("Fichier de log: " + logger.getLogFilePath());
        
        // Tester le logging
        logger.log(LoggingStrategy.LogLevel.INFO, "Test de logging - message 1");
        logger.log(LoggingStrategy.LogLevel.WARNING, "Test de logging - message 2");
        logger.log(LoggingStrategy.LogLevel.ERROR, "Test de logging - message 3");
        
        // Fermer le logger
        logger.close();
        
        System.out.println("Test terminé. Vérifiez le fichier de log.");
    }
}
