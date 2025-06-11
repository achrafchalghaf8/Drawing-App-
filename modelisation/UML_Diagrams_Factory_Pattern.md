# Factory Pattern - Diagramme UML

## Description
Ce diagramme montre l'implémentation du Factory Pattern pour la création de formes géométriques 2D et 3D.

## Diagramme Mermaid

```mermaid
classDiagram
    class ShapeFactory {
        <<enumeration>> ShapeType
        +RECTANGLE
        +CIRCLE
        +LINE
        +createShape(ShapeType, DimensionType, params) Shape
        +createRectangle(x, y, width, height, color, strokeWidth) Shape
        +createCircle(x, y, radius, color, strokeWidth) Shape
        +createLine(x1, y1, x2, y2, color, strokeWidth) Shape
    }
    
    class AbstractShapeFactory {
        <<abstract>>
        +createRectangle(x, y, width, height, color, strokeWidth)* Shape
        +createCircle(x, y, radius, color, strokeWidth)* Shape
    }
    
    class Factory2D {
        +createRectangle(x, y, width, height, color, strokeWidth) Rectangle
        +createCircle(x, y, radius, color, strokeWidth) Circle
    }
    
    class Factory3D {
        +createRectangle(x, y, width, height, color, strokeWidth) Rectangle3D
        +createCircle(x, y, radius, color, strokeWidth) Circle3D
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
    
    class Rectangle {
        -width: double
        -height: double
        +draw(GraphicsContext) void
        +contains(x, y) boolean
    }
    
    class Rectangle3D {
        -width: double
        -height: double
        -depthFactor: double
        +draw(GraphicsContext) void
        +contains(x, y) boolean
    }
    
    class Circle {
        -radius: double
        +draw(GraphicsContext) void
        +contains(x, y) boolean
    }
    
    class Circle3D {
        -radius: double
        +draw(GraphicsContext) void
        +contains(x, y) boolean
    }
    
    class Line {
        -endX: double
        -endY: double
        +draw(GraphicsContext) void
        +contains(x, y) boolean
    }
    
    class DimensionType {
        <<enumeration>>
        +D2
        +D3
    }
    
    ShapeFactory --> AbstractShapeFactory : uses
    AbstractShapeFactory <|-- Factory2D
    AbstractShapeFactory <|-- Factory3D
    
    Factory2D --> Rectangle : creates
    Factory2D --> Circle : creates
    Factory3D --> Rectangle3D : creates
    Factory3D --> Circle3D : creates
    ShapeFactory --> Line : creates
    
    Shape <|-- Rectangle
    Shape <|-- Rectangle3D
    Shape <|-- Circle
    Shape <|-- Circle3D
    Shape <|-- Line
    
    ShapeFactory --> DimensionType : uses
```

## Utilisation
- Copiez le code Mermaid ci-dessus
- Collez-le dans un éditeur supportant Mermaid (VS Code, GitHub, GitLab, etc.)
- Ou utilisez un outil en ligne comme mermaid.live
