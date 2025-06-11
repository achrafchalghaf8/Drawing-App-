package com.modelisation.model.graph;

import javafx.scene.canvas.GraphicsContext;
import com.modelisation.model.logging.LoggingStrategy;

import java.util.*;

/**
 * Classe représentant un graphe avec des nœuds et des arêtes
 * Utilisée pour les algorithmes de plus court chemin
 */
public class Graph {
    private Map<String, Node> nodes;
    private List<Edge> edges;
    private boolean directed;
    private LoggingStrategy logger;
    
    public Graph() {
        this(false);
    }
    
    public Graph(boolean directed) {
        this.nodes = new HashMap<>();
        this.edges = new ArrayList<>();
        this.directed = directed;
        this.logger = null; // Initialize logger to null
    }

    public void setLogger(LoggingStrategy logger) {
        this.logger = logger;
    }
    
    /**
     * Ajoute un nœud au graphe
     * @param node Nœud à ajouter
     * @return true si le nœud a été ajouté, false s'il existait déjà
     */
    public boolean addNode(Node node) {
        if (nodes.containsKey(node.getId())) {
            return false;
        }
        nodes.put(node.getId(), node);
        return true;
    }
    
    /**
     * Supprime un nœud du graphe
     * @param nodeId ID du nœud à supprimer
     * @return true si le nœud a été supprimé
     */
    public boolean removeNode(String nodeId) {
        Node node = nodes.remove(nodeId);
        if (node != null) {
            // Supprimer toutes les arêtes connectées à ce nœud
            edges.removeIf(edge -> edge.getSource().equals(node) || edge.getTarget().equals(node));
            return true;
        }
        return false;
    }
    
    /**
     * Ajoute une arête au graphe
     * @param edge Arête à ajouter
     * @return true si l'arête a été ajoutée
     */
    public boolean addEdge(Edge edge) {
        // Vérifier que les nœuds existent
        if (!nodes.containsValue(edge.getSource()) || !nodes.containsValue(edge.getTarget())) {
            return false;
        }
        
        // Vérifier que l'arête n'existe pas déjà
        if (edges.contains(edge)) {
            return false;
        }
        
        edges.add(edge);
        return true;
    }
    
    /**
     * Ajoute une arête entre deux nœuds
     * @param sourceId ID du nœud source
     * @param targetId ID du nœud cible
     * @param weight Poids de l'arête
     * @return true si l'arête a été ajoutée
     */
    public boolean addEdge(String sourceId, String targetId, double weight) {
        Node source = nodes.get(sourceId);
        Node target = nodes.get(targetId);
        
        if (source == null || target == null) {
            return false;
        }
        
        Edge edge = new Edge(source, target, weight, directed);
        return addEdge(edge);
    }
    
    /**
     * Supprime une arête du graphe
     * @param edge Arête à supprimer
     * @return true si l'arête a été supprimée
     */
    public boolean removeEdge(Edge edge) {
        return edges.remove(edge);
    }
    
    /**
     * Obtient un nœud par son ID
     * @param nodeId ID du nœud
     * @return Le nœud ou null s'il n'existe pas
     */
    public Node getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    /**
     * Returns the size of the internal nodes map.
     * Used for debugging to directly check the map's state.
     * @return The number of entries in the nodes map.
     */
    public int getNodesMapSize() {
        return this.nodes.size();
    }

    /**
     * Returns an unmodifiable view of the internal nodes map's values (the nodes themselves).
     * This is an alternative to getNodes() if direct access to the collection is needed without creating a new list each time.
     * @return An unmodifiable collection of nodes.
     */
    public Collection<Node> getNodeCollection() {
        return Collections.unmodifiableCollection(this.nodes.values());
    }
    
    /**
     * Obtient toutes les arêtes connectées à un nœud
     * @param node Le nœud
     * @return Liste des arêtes connectées
     */
    public List<Edge> getEdgesForNode(Node node) {
        List<Edge> nodeEdges = new ArrayList<>();
        for (Edge edge : edges) {
            if (edge.getSource().equals(node) || edge.getTarget().equals(node)) {
                nodeEdges.add(edge);
            }
        }
        return nodeEdges;
    }
    
    /**
     * Obtient les nœuds voisins d'un nœud donné
     * @param node Le nœud
     * @return Liste des nœuds voisins avec leurs distances
     */
    public Map<Node, Double> getNeighbors(Node node) {
        Map<Node, Double> neighbors = new HashMap<>();
        
        for (Edge edge : edges) {
            if (edge.getSource().equals(node)) {
                neighbors.put(edge.getTarget(), edge.getWeight());
            } else if (!directed && edge.getTarget().equals(node)) {
                neighbors.put(edge.getSource(), edge.getWeight());
            }
        }
        
        return neighbors;
    }
    
    /**
     * Trouve un nœud à une position donnée
     * @param x Coordonnée X
     * @param y Coordonnée Y
     * @return Le nœud trouvé ou null
     */
    public Node findNodeAt(double x, double y) {
        for (Node node : nodes.values()) {
            if (node.contains(x, y)) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * Trouve une arête à une position donnée
     * @param x Coordonnée X
     * @param y Coordonnée Y
     * @return L'arête trouvée ou null
     */
    public Edge findEdgeAt(double x, double y) {
        for (Edge edge : edges) {
            if (edge.contains(x, y)) {
                return edge;
            }
        }
        return null;
    }
    
    /**
     * Réinitialise toutes les propriétés d'algorithme des nœuds
     */
    public void resetAlgorithmProperties() {
        for (Node node : nodes.values()) {
            node.resetAlgorithmProperties();
        }
        
        for (Edge edge : edges) {
            edge.setHighlighted(false);
        }
    }
    
    /**
     * Met en évidence un chemin
     * @param path Liste des nœuds formant le chemin
     */
    public void highlightPath(List<Node> path) {
        if (path == null || path.isEmpty()) {
            return;
        }

        // Note: Highlights are assumed to have been reset by the caller (e.g., DrawingController)
        // before calling this method.
        for (int i = 0; i < path.size() - 1; i++) {
            Node current = path.get(i);
            Node next = path.get(i + 1);

            for (Edge edge : edges) {
                if ((edge.getSource().equals(current) && edge.getTarget().equals(next)) ||
                    (!directed && edge.getSource().equals(next) && edge.getTarget().equals(current))) {
                    edge.setHighlighted(true);
                    break;
                }
            }
        }
    }

    /**
     * Sets the de-emphasized state for edges that are not part of a highlighted path.
     * @param deemphasize true to de-emphasize non-highlighted edges, false to remove de-emphasis.
     */
    public void setDeemphasizeNonHighlightedEdges(boolean deemphasize) {
        for (Edge edge : edges) {
            if (deemphasize) {
                if (!edge.isHighlighted()) {
                    edge.setDeemphasized(true);
                } else {
                    edge.setDeemphasized(false); // Ensure highlighted path edges are not de-emphasized
                }
            } else {
                edge.setDeemphasized(false);
            }
        }
    }
    
    /**
     * Dessine le graphe sur le canvas
     * @param gc Contexte graphique
     */
    public void draw(GraphicsContext gc) {
        // Dessiner d'abord les arêtes
        for (Edge edge : edges) {
            edge.draw(gc);
        }
        
        // Puis dessiner les nœuds par-dessus
        for (Node node : nodes.values()) {
            node.draw(gc);
        }
    }
    
    /**
     * Efface le graphe
     */
    public void clear() {
        nodes.clear();
        edges.clear();
    }
    
    /**
     * Vérifie si le graphe est connexe
     * @return true si le graphe est connexe
     */
    public boolean isConnected() {
        if (nodes.isEmpty()) {
            return true;
        }
        
        // Utiliser BFS pour vérifier la connectivité
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        
        Node startNode = nodes.values().iterator().next();
        queue.offer(startNode);
        visited.add(startNode);
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            Map<Node, Double> neighbors = getNeighbors(current);
            
            for (Node neighbor : neighbors.keySet()) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }
        
        return visited.size() == nodes.size();
    }
    
    // Getters
    public Collection<Node> getNodes() {
        return nodes.values();
    }
    
    public List<Edge> getEdges() {
        return new ArrayList<>(edges);
    }
    
    public boolean isDirected() {
        return directed;
    }
    
    public int getNodeCount() {
        return nodes.size();
    }
    
    public int getEdgeCount() {
        return edges.size();
    }
    
    /**
     * Réinitialise l'état de surbrillance de tous les nœuds et arêtes.
     * Utilisé pour effacer les chemins précédents avant d'en dessiner un nouveau.
     */
    public void resetHighlights() {
        for (Node node : nodes.values()) {
            node.setSelected(false);
            // Note: Node class does not have a generic 'highlighted' field, 'selected' is used for this purpose.
            // If a separate 'highlighted' state is needed for nodes beyond selection, it should be added to Node.java.
        }
        for (Edge edge : edges) {
            edge.setHighlighted(false);
            edge.setDeemphasized(false); // Also reset de-emphasized state
        }
        if (this.logger != null) {
            this.logger.log(LoggingStrategy.LogLevel.DEBUG, "Graph highlights have been reset.");
        }
    }

    @Override
    public String toString() {
        return String.format("Graph[nodes=%d, edges=%d, directed=%s]", 
                           nodes.size(), edges.size(), directed);
    }
}
