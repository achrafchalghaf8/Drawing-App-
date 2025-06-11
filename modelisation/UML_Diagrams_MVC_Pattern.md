# MVC Pattern - Architecture Générale - Diagramme UML

## Description
Ce diagramme montre l'implémentation du pattern MVC (Model-View-Controller) dans l'application de modélisation.

## Diagramme Mermaid

```mermaid
classDiagram
    %% MODEL LAYER
    class Drawing {
        <<Model>>
        -shapes: List~Shape~
        -name: String
        -description: String
        -logger: LoggingStrategy
        +addShape(Shape) void
        +removeShape(Shape) boolean
        +clear() void
        +getShapes() List~Shape~
        +getShapeCount() int
        +setName(String) void
        +setDescription(String) void
    }
    
    class Shape {
        <<Model - Abstract>>
        -id: String
        -x: double
        -y: double
        -color: Color
        -strokeWidth: double
        +draw(GraphicsContext)* void
        +contains(x, y)* boolean
        +toString() String
    }
    
    class Graph {
        <<Model>>
        -nodes: List~Node~
        -edges: List~Edge~
        -adjacencyMap: Map~Node, Map~Node, Double~~
        +addNode(Node) void
        +addEdge(Node, Node, double) void
        +getNeighbors(Node) Map~Node, Double~
        +highlightPath(List~Node~) void
    }
    
    %% VIEW LAYER
    class MainView {
        <<View>>
        -primaryStage: Stage
        -drawingCanvas: DrawingCanvas
        -shapePalette: ShapePalette
        -controller: DrawingController
        -menuBar: MenuBar
        -toolBar: ToolBar
        -statusBar: StatusBar
        +start(Stage) void
        +getDrawingCanvas() DrawingCanvas
        +getStatusBar() StatusBar
    }
    
    class DrawingCanvas {
        <<View>>
        -drawing: Drawing
        -currentShapeType: ShapeType
        -currentColor: Color
        -currentStrokeWidth: double
        -drawingController: DrawingController
        +setDrawing(Drawing) void
        +redraw() void
        +update(Observable, Object) void
        -setupMouseEvents() void
    }
    
    class ShapePalette {
        <<View>>
        -shapeToggleGroup: ToggleGroup
        -colorPicker: ColorPicker
        -strokeWidthSlider: Slider
        -dimensionToggleGroup: ToggleGroup
        +getSelectedShapeType() ShapeType
        +getSelectedColor() Color
        +getStrokeWidth() double
        +setShapeSelectionListener(ShapeSelectionListener) void
    }
    
    %% CONTROLLER LAYER
    class DrawingController {
        <<Controller>>
        -currentDrawing: Drawing
        -mainView: MainView
        -databaseManager: DatabaseManager
        -persistenceManager: DrawingPersistenceManager
        -currentAlgorithm: ShortestPathStrategy
        +newDrawing() void
        +saveToFile(File) void
        +openFromFile() void
        +saveToDatabase() void
        +openFromDatabase() void
        +calculateShortestPath() void
        +setShortestPathAlgorithm(String) void
        +logAction(String) void
        +logError(String, Throwable) void
    }
    
    %% SUPPORTING CLASSES
    class DatabaseManager {
        <<Model - Singleton>>
        +getInstance()$ DatabaseManager
        +saveDrawing(String, String, String, int) int
        +loadDrawing(int) String
        +testConnection() boolean
    }
    
    class ShapeFactory {
        <<Model - Factory>>
        +createShape(ShapeType, DimensionType, params) Shape
        +createRectangle(x, y, width, height, color, strokeWidth) Shape
        +createCircle(x, y, radius, color, strokeWidth) Shape
    }
    
    class LoggingStrategy {
        <<Model - Strategy Interface>>
        +log(LogLevel, String) void
        +log(LogLevel, String, Throwable) void
        +close() void
    }
    
    %% RELATIONSHIPS
    %% Model relationships
    Drawing --> Shape : contains
    Drawing --> LoggingStrategy : uses
    Graph --> Node : contains
    Graph --> Edge : contains
    
    %% View relationships
    MainView --> DrawingCanvas : contains
    MainView --> ShapePalette : contains
    MainView --> DrawingController : uses
    DrawingCanvas --> Drawing : observes
    DrawingCanvas --> DrawingController : notifies
    
    %% Controller relationships
    DrawingController --> Drawing : manipulates
    DrawingController --> MainView : updates
    DrawingController --> DatabaseManager : uses
    DrawingController --> ShapeFactory : uses
    DrawingController --> LoggingStrategy : uses
    
    %% MVC Flow
    MainView -.-> DrawingController : user actions
    DrawingController -.-> Drawing : modifies
    Drawing -.-> DrawingCanvas : notifies (Observer)
    DrawingCanvas -.-> MainView : updates display
```

## Architecture MVC

### **Model (Modèle)**
- **Drawing** : Modèle principal contenant les formes
- **Shape** : Classes de formes géométriques
- **Graph** : Modèle de graphe pour algorithmes
- **DatabaseManager** : Gestion de la persistance
- **LoggingStrategy** : Stratégies de logging

### **View (Vue)**
- **MainView** : Interface principale
- **DrawingCanvas** : Zone de dessin
- **ShapePalette** : Palette d'outils
- **MenuBar, ToolBar, StatusBar** : Composants UI

### **Controller (Contrôleur)**
- **DrawingController** : Contrôleur principal
- Gère les interactions utilisateur
- Coordonne Model et View
- Implémente la logique métier

## Utilisation
- Copiez le code Mermaid ci-dessus
- Collez-le dans un éditeur supportant Mermaid (VS Code, GitHub, GitLab, etc.)
- Ou utilisez un outil en ligne comme mermaid.live
