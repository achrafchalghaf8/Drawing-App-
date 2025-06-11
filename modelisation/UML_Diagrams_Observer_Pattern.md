# Observer Pattern - Notifications de Changements - Diagramme UML

## Description
Ce diagramme montre l'implémentation du Observer Pattern pour les notifications de changements entre le modèle Drawing et la vue DrawingCanvas.

## Diagramme Mermaid

```mermaid
classDiagram
    class Observable {
        <<Java Built-in>>
        -observers: Vector~Observer~
        +addObserver(Observer) void
        +deleteObserver(Observer) void
        +notifyObservers() void
        +notifyObservers(Object) void
        +setChanged() void
        +clearChanged() void
        +hasChanged() boolean
        +countObservers() int
    }
    
    class Observer {
        <<Java Built-in Interface>>
        +update(Observable, Object) void
    }
    
    class Drawing {
        -shapes: List~Shape~
        -name: String
        -description: String
        -logger: LoggingStrategy
        +addShape(Shape) void
        +removeShape(Shape) boolean
        +removeShape(int) boolean
        +clear() void
        +getShapes() List~Shape~
        +getShapeCount() int
        +setName(String) void
        +setDescription(String) void
    }
    
    class DrawingCanvas {
        -drawing: Drawing
        -currentShapeType: ShapeType
        -currentColor: Color
        -currentStrokeWidth: double
        -logger: LoggingStrategy
        -drawingController: DrawingController
        -isDrawing: boolean
        -previewShape: Shape
        +setDrawing(Drawing) void
        +redraw() void
        +update(Observable, Object) void
        -setupMouseEvents() void
        -drawShapeLabel(GraphicsContext, Shape, String) void
    }
    
    class DrawingController {
        -currentDrawing: Drawing
        -mainView: MainView
        +addShape(Shape) void
        +removeShape(Shape) void
        +clearDrawing() void
        +logAction(String) void
        +logError(String, Throwable) void
    }
    
    class MainView {
        -drawingCanvas: DrawingCanvas
        -shapePalette: ShapePalette
        -controller: DrawingController
        +start(Stage) void
        +getDrawingCanvas() DrawingCanvas
    }
    
    class Shape {
        <<abstract>>
        -id: String
        -x: double
        -y: double
        -color: Color
        -strokeWidth: double
        +draw(GraphicsContext)* void
        +contains(x, y)* boolean
        +toString() String
    }
    
    Observable <|-- Drawing
    Observer <|.. DrawingCanvas
    
    Drawing --> Shape : contains
    Drawing --> LoggingStrategy : uses
    
    DrawingCanvas --> Drawing : observes
    DrawingCanvas --> DrawingController : notifies
    
    DrawingController --> Drawing : modifies
    DrawingController --> MainView : updates
    
    MainView --> DrawingCanvas : contains
```

## Notes
- **Drawing** notifie les observateurs lors de :
  - SHAPE_ADDED
  - SHAPE_REMOVED  
  - DRAWING_CLEARED

- **DrawingCanvas** réagit aux notifications :
  - Redessine le canvas
  - Met à jour l'affichage
  - Gère les événements souris

## Utilisation
- Copiez le code Mermaid ci-dessus
- Collez-le dans un éditeur supportant Mermaid (VS Code, GitHub, GitLab, etc.)
- Ou utilisez un outil en ligne comme mermaid.live
