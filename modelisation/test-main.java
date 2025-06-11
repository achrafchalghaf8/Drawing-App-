import com.modelisation.model.logging.FileLogger;
import com.modelisation.model.logging.LoggingStrategy;
import com.modelisation.model.logging.DatabaseLoggingStrategy;

class TestMain {
    public static void main(String[] args) {
        System.out.println("=== TEST DE L'APPLICATION ===");
        
        // Test 1: FileLogger
        System.out.println("1. Test FileLogger...");
        try {
            FileLogger fileLogger = new FileLogger();
            System.out.println("   Fichier: " + fileLogger.getLogFilePath());
            fileLogger.log(LoggingStrategy.LogLevel.INFO, "Application démarrée - Test FileLogger");
            fileLogger.log(LoggingStrategy.LogLevel.INFO, "Test de création de formes");
            fileLogger.log(LoggingStrategy.LogLevel.WARNING, "Test d'avertissement");
            fileLogger.close();
            System.out.println("   ✅ FileLogger OK");
        } catch (Exception e) {
            System.out.println("   ❌ FileLogger ERREUR: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Test 2: DatabaseLoggingStrategy
        System.out.println("2. Test DatabaseLoggingStrategy...");
        try {
            DatabaseLoggingStrategy dbLogger = new DatabaseLoggingStrategy();
            dbLogger.log(LoggingStrategy.LogLevel.INFO, "Test de connexion base de données");
            dbLogger.log(LoggingStrategy.LogLevel.INFO, "Test d'enregistrement en base");
            dbLogger.close();
            System.out.println("   ✅ DatabaseLoggingStrategy OK");
        } catch (Exception e) {
            System.out.println("   ❌ DatabaseLoggingStrategy ERREUR: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== FIN DU TEST ===");
    }
}
