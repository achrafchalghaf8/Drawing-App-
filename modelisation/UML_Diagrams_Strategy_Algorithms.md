# Strategy Pattern - Algorithmes de Plus Court Chemin - Diagramme UML

## Description
Ce diagramme montre l'implémentation du Strategy Pattern pour les algorithmes de plus court chemin (Dijkstra et BFS).

## Diagramme Mermaid

```mermaid
classDiagram
    class ShortestPathStrategy {
        <<interface>>
        +findShortestPath(Graph, Node, Node) List~Node~
        +findShortestPaths(Graph, Node) ShortestPathResult
        +getName() String
    }
    
    class DijkstraAlgorithm {
        +findShortestPath(Graph, Node, Node) List~Node~
        +findShortestPaths(Graph, Node) ShortestPathResult
        +getName() String
        -reconstructPath(Node) List~Node~
        -findShortestPathInternal(Graph, Node, Node) List~Node~
    }
    
    class BFSAlgorithm {
        +findShortestPath(Graph, Node, Node) List~Node~
        +findShortestPaths(Graph, Node) ShortestPathResult
        +getName() String
        -reconstructPath(Node) List~Node~
        -findShortestPathInternal(Graph, Node, Node) List~Node~
    }
    
    class DijkstraStrategy {
        <<deprecated>>
        +findShortestPath(Graph, Node, Node) List~Node~
    }
    
    class ShortestPathResult {
        -sourceNode: Node
        -success: boolean
        -errorMessage: String
        -executionTime: long
        +getSourceNode() Node
        +isSuccess() boolean
        +getErrorMessage() String
        +getExecutionTime() long
    }
    
    class Graph {
        -nodes: List~Node~
        -edges: List~Edge~
        -adjacencyMap: Map~Node, Map~Node, Double~~
        +addNode(Node) void
        +addEdge(Node, Node, double) void
        +getNeighbors(Node) Map~Node, Double~
        +resetAlgorithmProperties() void
        +highlightPath(List~Node~) void
    }
    
    class Node {
        -id: String
        -label: String
        -x: double
        -y: double
        -distance: double
        -previous: Node
        -visited: boolean
        +setDistance(double) void
        +setPrevious(Node) void
        +setVisited(boolean) void
        +getDistance() double
        +getPrevious() Node
        +isVisited() boolean
    }
    
    class DrawingController {
        -currentAlgorithm: ShortestPathStrategy
        -currentAlgorithmName: String
        +setShortestPathAlgorithm(String) void
        +calculateShortestPath() void
        -saveShortestPathSession(String, String, int, double, String, long) void
    }
    
    ShortestPathStrategy <|.. DijkstraAlgorithm
    ShortestPathStrategy <|.. BFSAlgorithm
    ShortestPathStrategy <|.. DijkstraStrategy
    
    ShortestPathStrategy --> ShortestPathResult : returns
    ShortestPathStrategy --> Graph : uses
    ShortestPathStrategy --> Node : uses
    
    DrawingController --> ShortestPathStrategy : uses
    
    Graph --> Node : contains
    Graph --> Edge : contains
```

## Utilisation
- Copiez le code Mermaid ci-dessus
- Collez-le dans un éditeur supportant Mermaid (VS Code, GitHub, GitLab, etc.)
- Ou utilisez un outil en ligne comme mermaid.live
