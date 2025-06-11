import com.modelisation.model.logging.FileLogger;
import com.modelisation.model.logging.LoggingStrategy;
import com.modelisation.model.logging.ConsoleLogger;
import com.modelisation.model.logging.DatabaseLoggingStrategy;

class TestLoggingSwitch {
    private LoggingStrategy currentLogger;
    
    public static void main(String[] args) {
        TestLoggingSwitch test = new TestLoggingSwitch();
        
        System.out.println("=== TEST DE BASCULEMENT DE LOGGING ===");
        
        // Simuler le comportement de MainView
        test.setLoggingStrategy("Console");
        test.testLogging("Test avec Console");
        
        test.setLoggingStrategy("Fichier");
        test.testLogging("Test avec Fichier");
        
        test.setLoggingStrategy("Base de données");
        test.testLogging("Test avec Base de données");
        
        // Retour au fichier
        test.setLoggingStrategy("Fichier");
        test.testLogging("Retour au fichier");
        
        // Fermer le logger
        if (test.currentLogger != null) {
            test.currentLogger.close();
        }
        
        System.out.println("=== FIN DU TEST ===");
    }
    
    private void setLoggingStrategy(String strategy) {
        System.out.println("\n--- Basculement vers: " + strategy + " ---");
        
        // Fermer l'ancien logger
        if (currentLogger != null) {
            System.out.println("Fermeture de l'ancien logger...");
            currentLogger.close();
        }
        
        // Créer le nouveau logger
        switch (strategy) {
            case "Console":
                currentLogger = new ConsoleLogger();
                break;
            case "Fichier":
                FileLogger fileLogger = new FileLogger();
                System.out.println("Nouveau fichier de log: " + fileLogger.getLogFilePath());
                currentLogger = fileLogger;
                break;
            case "Base de données":
                currentLogger = new DatabaseLoggingStrategy();
                break;
            default:
                currentLogger = new ConsoleLogger();
        }
        
        // Logger le changement
        currentLogger.log(LoggingStrategy.LogLevel.INFO, 
                         "Stratégie de logging changée: " + strategy);
        
        System.out.println("Logger configuré: " + currentLogger.getClass().getSimpleName());
    }
    
    private void testLogging(String message) {
        System.out.println("Test de logging: " + message);
        if (currentLogger != null) {
            currentLogger.log(LoggingStrategy.LogLevel.INFO, message);
            currentLogger.log(LoggingStrategy.LogLevel.WARNING, "Avertissement: " + message);
        }
    }
}
