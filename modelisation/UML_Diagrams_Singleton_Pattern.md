# Singleton Pattern - Gestionnaire de Base de Données - Diagramme UML

## Description
Ce diagramme montre l'implémentation du Singleton Pattern pour les gestionnaires de base de données et de configuration.

## Diagramme Mermaid

```mermaid
classDiagram
    class DatabaseManager {
        -instance: DatabaseManager$
        -config: DatabaseConfig
        -connection: Connection
        -sessionId: String
        -DatabaseManager()
        +getInstance()$ DatabaseManager
        +getConnection() Connection
        +testConnection() boolean
        +closeConnection() void
        +saveDrawing(String, String, String, int) int
        +loadDrawing(int) String
        +saveShortestPathSession(int, String, String, String, int, double, String, long) void
        +logToDatabase(String, String, String, String) void
        -initializeConnection() void
        -runAutoMigration() void
    }
    
    class DatabaseConfig {
        -instance: DatabaseConfig$
        -host: String
        -port: String
        -database: String
        -username: String
        -password: String
        -DatabaseConfig()
        +getInstance()$ DatabaseConfig
        +getHost() String
        +getPort() String
        +getDatabase() String
        +getUsername() String
        +getPassword() String
        +getJdbcUrl() String
        +printConfiguration() void
        -loadFromProperties() void
        -loadFromEnvironment() void
    }
    
    class DatabaseMigration {
        -config: DatabaseConfig
        +runMigration() boolean
        +isMigrationNeeded() boolean
        +main(String[])$ void
        -createDatabaseIfNotExists() boolean
        -createTables() boolean
        -createApplicationLogsTable(Connection) void
        -createDrawingsTable(Connection) void
        -createShapesTable(Connection) void
        -createShortestPathSessionsTable(Connection) void
        -createIndexes(Connection) void
    }
    
    class DrawingPersistenceManager {
        -databaseManager: DatabaseManager
        -objectMapper: ObjectMapper
        -logger: LoggingStrategy
        +saveDrawing(Drawing, String, String) int
        +getDrawingsList() List~DrawingInfo~
        +getDrawingJson(int) String
        +convertDrawingToJson(Drawing) String
        -saveShapesDetails(int, Drawing) void
    }
    
    class DatabaseLoggingStrategy {
        -databaseManager: DatabaseManager
        -isEnabled: boolean
        +log(LogLevel, String, Throwable) void
        +reconnect() void
        +isDatabaseEnabled() boolean
        +logUserAction(LogLevel, String, String) void
    }
    
    class DrawingController {
        -databaseManager: DatabaseManager
        -persistenceManager: DrawingPersistenceManager
        +saveToDatabase() void
        +openFromDatabase() void
    }
    
    DatabaseManager --> DatabaseConfig : uses
    DatabaseManager --> DatabaseMigration : uses
    
    DrawingPersistenceManager --> DatabaseManager : uses
    DatabaseLoggingStrategy --> DatabaseManager : uses
    DrawingController --> DatabaseManager : uses
```

## Notes
- **DatabaseManager** : Singleton Pattern
  - Instance unique
  - Accès global
  - Initialisation paresseuse
  - Thread-safe avec synchronized

- **DatabaseConfig** : Singleton Pattern
  - Configuration unique
  - Chargement depuis properties
  - Variables d'environnement

## Utilisation
- Copiez le code Mermaid ci-dessus
- Collez-le dans un éditeur supportant Mermaid (VS Code, GitHub, GitLab, etc.)
- Ou utilisez un outil en ligne comme mermaid.live
