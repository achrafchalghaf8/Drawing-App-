package com.modelisation.model.graph;

import com.modelisation.model.graph.algorithms.DijkstraAlgorithm;
import com.modelisation.model.graph.algorithms.BFSAlgorithm;
import com.modelisation.model.graph.algorithms.ShortestPathStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Tests unitaires pour les classes Graph et les algorithmes de plus court chemin
 */
public class GraphTest {
    
    private Graph graph;
    private Node nodeA, nodeB, nodeC, nodeD;
    
    @BeforeEach
    public void setUp() {
        graph = new Graph(false); // Graphe non dirigé
        
        // Créer des nœuds
        nodeA = new Node("A", 0, 0, "Node A");
        nodeB = new Node("B", 100, 0, "Node B");
        nodeC = new Node("C", 50, 100, "Node C");
        nodeD = new Node("D", 150, 100, "Node D");
        
        // Ajouter les nœuds au graphe
        graph.addNode(nodeA);
        graph.addNode(nodeB);
        graph.addNode(nodeC);
        graph.addNode(nodeD);
    }
    
    @Test
    public void testGraphConstruction() {
        assertEquals(4, graph.getNodeCount());
        assertEquals(0, graph.getEdgeCount());
        assertFalse(graph.isDirected());
        
        assertTrue(graph.getNodes().contains(nodeA));
        assertTrue(graph.getNodes().contains(nodeB));
        assertTrue(graph.getNodes().contains(nodeC));
        assertTrue(graph.getNodes().contains(nodeD));
    }
    
    @Test
    public void testAddEdges() {
        // Ajouter des arêtes
        assertTrue(graph.addEdge("A", "B", 5.0));
        assertTrue(graph.addEdge("A", "C", 3.0));
        assertTrue(graph.addEdge("B", "D", 2.0));
        assertTrue(graph.addEdge("C", "D", 4.0));
        
        assertEquals(4, graph.getEdgeCount());
        
        // Tenter d'ajouter une arête avec un nœud inexistant
        assertFalse(graph.addEdge("A", "E", 1.0));
    }
    
    @Test
    public void testNeighbors() {
        graph.addEdge("A", "B", 5.0);
        graph.addEdge("A", "C", 3.0);
        
        var neighborsA = graph.getNeighbors(nodeA);
        assertEquals(2, neighborsA.size());
        assertTrue(neighborsA.containsKey(nodeB));
        assertTrue(neighborsA.containsKey(nodeC));
        assertEquals(5.0, neighborsA.get(nodeB));
        assertEquals(3.0, neighborsA.get(nodeC));
    }
    
    @Test
    public void testDijkstraAlgorithm() {
        // Créer un graphe simple : A-B(5)-D(2), A-C(3)-D(4)
        graph.addEdge("A", "B", 5.0);
        graph.addEdge("A", "C", 3.0);
        graph.addEdge("B", "D", 2.0);
        graph.addEdge("C", "D", 4.0);
        
        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm();
        
        // Chemin le plus court de A à D
        List<Node> path = dijkstra.findShortestPath(graph, nodeA, nodeD);
        
        assertNotNull(path);
        assertEquals(3, path.size());
        assertEquals(nodeA, path.get(0));
        assertEquals(nodeC, path.get(1)); // Path A->C->D is chosen due to C being processed before B
        assertEquals(nodeD, path.get(2));
        
        // Vérifier la distance totale (5 + 2 = 7)
        ShortestPathStrategy.ShortestPathResult result = dijkstra.findShortestPaths(graph, nodeA);
        assertEquals(7.0, result.getDistanceTo(nodeD), 0.001);
    }
    
    @Test
    public void testBFSAlgorithm() {
        // Créer un graphe simple
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("A", "C", 1.0);
        graph.addEdge("B", "D", 1.0);
        graph.addEdge("C", "D", 1.0);
        
        BFSAlgorithm bfs = new BFSAlgorithm();
        
        // Chemin le plus court de A à D (en nombre d'arêtes)
        List<Node> path = bfs.findShortestPath(graph, nodeA, nodeD);
        
        assertNotNull(path);
        assertEquals(3, path.size()); // A -> B -> D ou A -> C -> D
        assertEquals(nodeA, path.get(0));
        assertEquals(nodeD, path.get(2));
    }
    
    @Test
    public void testGraphConnectivity() {
        // Graphe non connecté initialement
        assertFalse(graph.isConnected());
        
        // Connecter tous les nœuds
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("B", "C", 1.0);
        graph.addEdge("C", "D", 1.0);
        
        assertTrue(graph.isConnected());
    }
    
    @Test
    public void testNodeContains() {
        assertTrue(nodeA.contains(5, 5)); // Dans le rayon du nœud
        assertFalse(nodeA.contains(50, 50)); // Hors du rayon du nœud
    }
    
    @Test
    public void testEdgeContains() {
        Edge edge = new Edge(nodeA, nodeB, 1.0);
        
        // Point sur la ligne entre A(0,0) et B(100,0)
        assertTrue(edge.contains(50, 0));
        assertTrue(edge.contains(25, 2)); // Proche de la ligne (tolérance)
        
        // Point loin de la ligne
        assertFalse(edge.contains(50, 50));
    }
    
    @Test
    public void testRemoveNode() {
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("A", "C", 1.0);
        
        assertEquals(4, graph.getNodeCount());
        assertEquals(2, graph.getEdgeCount());
        
        // Supprimer le nœud A
        assertTrue(graph.removeNode("A"));
        
        assertEquals(3, graph.getNodeCount());
        assertEquals(0, graph.getEdgeCount()); // Les arêtes connectées à A sont supprimées
        
        assertNull(graph.getNode("A"));
    }
    
    @Test
    public void testClearGraph() {
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("C", "D", 1.0);
        
        assertEquals(4, graph.getNodeCount());
        assertEquals(2, graph.getEdgeCount());
        
        graph.clear();
        
        assertEquals(0, graph.getNodeCount());
        assertEquals(0, graph.getEdgeCount());
    }
}
