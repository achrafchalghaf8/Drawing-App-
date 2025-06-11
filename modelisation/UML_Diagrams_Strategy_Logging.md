# Strategy Pattern - Système de Logging - Diagramme UML

## Description
Ce diagramme montre l'implémentation du Strategy Pattern pour le système de logging avec différentes stratégies (Console, Fichier, Base de données).

## Diagramme Mermaid

```mermaid
classDiagram
    class LoggingStrategy {
        <<interface>>
        +log(LogLevel, String) void
        +log(LogLevel, String, Throwable) void
        +close() void
    }
    
    class LogLevel {
        <<enumeration>>
        +DEBUG
        +INFO
        +WARNING
        +ERROR
        +getLabel() String
    }
    
    class ConsoleLogger {
        -TIMESTAMP_FORMAT: DateTimeFormatter
        +log(LogLevel, String) void
        +log(LogLevel, String, Throwable) void
        +close() void
    }
    
    class FileLogger {
        -TIMESTAMP_FORMAT: DateTimeFormatter
        -LOG_DIR: String
        -writer: BufferedWriter
        -logFile: File
        +log(LogLevel, String) void
        +log(LogLevel, String, Throwable) void
        +close() void
        -initializeLogFile() void
        -createLogDirectory() void
    }
    
    class DatabaseLoggingStrategy {
        -databaseManager: DatabaseManager
        -isEnabled: boolean
        +log(LogLevel, String) void
        +log(LogLevel, String, Throwable) void
        +close() void
        +reconnect() void
        +isDatabaseEnabled() boolean
        +logUserAction(LogLevel, String, String) void
        -formatForConsole(LogLevel, String, Throwable) String
        -determineUserAction(String) String
    }
    
    class DatabaseLogger {
        <<deprecated>>
        -databaseManager: DatabaseManager
        +log(LogLevel, String) void
        +log(LogLevel, String, Throwable) void
        +close() void
    }
    
    class MainView {
        -currentLogger: LoggingStrategy
        -loggingStrategyComboBox: ComboBox~String~
        +setLoggingStrategy(String) void
        +getCurrentLogger() LoggingStrategy
    }
    
    class DrawingController {
        -logger: LoggingStrategy
        +logAction(String) void
        +logError(String, Throwable) void
    }
    
    class Drawing {
        -logger: LoggingStrategy
        +setLogger(LoggingStrategy) void
        +addShape(Shape) void
        +removeShape(Shape) boolean
    }
    
    LoggingStrategy <|.. ConsoleLogger
    LoggingStrategy <|.. FileLogger
    LoggingStrategy <|.. DatabaseLoggingStrategy
    LoggingStrategy <|.. DatabaseLogger
    
    LoggingStrategy --> LogLevel : uses
    
    MainView --> LoggingStrategy : uses
    DrawingController --> LoggingStrategy : uses
    Drawing --> LoggingStrategy : uses
    
    DatabaseLoggingStrategy --> DatabaseManager : uses
```

## Utilisation
- Copiez le code Mermaid ci-dessus
- Collez-le dans un éditeur supportant Mermaid (VS Code, GitHub, GitLab, etc.)
- Ou utilisez un outil en ligne comme mermaid.live
