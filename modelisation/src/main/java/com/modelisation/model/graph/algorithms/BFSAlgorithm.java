package com.modelisation.model.graph.algorithms;

import com.modelisation.model.graph.Graph;
import com.modelisation.model.graph.Node;

import java.util.*;

/**
 * Implémentation de l'algorithme BFS (Breadth-First Search) pour le plus court chemin
 * Strategy Pattern - Stratégie concrète pour l'algorithme BFS
 * Optimal pour les graphes non pondérés ou avec des poids uniformes
 */
public class BFSAlgorithm implements ShortestPathStrategy {
    
    @Override
    public List<Node> findShortestPath(Graph graph, Node source, Node target) {
        if (source == null || target == null) {
            return null;
        }
        
        if (!graph.getNodes().contains(source) || !graph.getNodes().contains(target)) {
            return null;
        }
        
        // Si source et target sont identiques
        if (source.equals(target)) {
            return Arrays.asList(source);
        }
        
        // Réinitialiser les propriétés d'algorithme
        graph.resetAlgorithmProperties();
        
        // BFS avec file FIFO
        Queue<Node> queue = new LinkedList<>();
        Set<Node> visited = new HashSet<>();
        
        // Initialiser la recherche
        source.setDistance(0.0);
        source.setPrevious(null);
        queue.offer(source);
        visited.add(source);
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            current.setVisited(true);
            
            // Si on a atteint la cible, reconstruire le chemin
            if (current.equals(target)) {
                return reconstructPath(target);
            }
            
            // Examiner tous les voisins
            Map<Node, Double> neighbors = graph.getNeighbors(current);
            
            for (Map.Entry<Node, Double> entry : neighbors.entrySet()) {
                Node neighbor = entry.getKey();
                double edgeWeight = entry.getValue();
                
                // Si le voisin n'a pas encore été visité
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    neighbor.setDistance(current.getDistance() + edgeWeight);
                    neighbor.setPrevious(current);
                    queue.offer(neighbor);
                }
            }
        }
        
        // Aucun chemin trouvé
        return null;
    }
    
    @Override
    public ShortestPathResult findShortestPaths(Graph graph, Node source) {
        if (source == null) {
            return new ShortestPathResult(null, false, "Nœud source null");
        }
        
        if (!graph.getNodes().contains(source)) {
            return new ShortestPathResult(source, false, "Nœud source non trouvé dans le graphe");
        }
        
        // Réinitialiser les propriétés d'algorithme
        graph.resetAlgorithmProperties();
        
        // BFS avec file FIFO
        Queue<Node> queue = new LinkedList<>();
        Set<Node> visited = new HashSet<>();
        
        // Initialiser la recherche
        source.setDistance(0.0);
        source.setPrevious(null);
        queue.offer(source);
        visited.add(source);
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            current.setVisited(true);
            
            // Examiner tous les voisins
            Map<Node, Double> neighbors = graph.getNeighbors(current);
            
            for (Map.Entry<Node, Double> entry : neighbors.entrySet()) {
                Node neighbor = entry.getKey();
                double edgeWeight = entry.getValue();
                
                // Si le voisin n'a pas encore été visité
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    neighbor.setDistance(current.getDistance() + edgeWeight);
                    neighbor.setPrevious(current);
                    queue.offer(neighbor);
                }
            }
        }
        
        return new ShortestPathResult(source);
    }
    
    /**
     * Variante BFS pour graphes non pondérés (tous les poids = 1)
     * Plus efficace car elle ne considère que le nombre d'arêtes
     */
    public List<Node> findShortestPathUnweighted(Graph graph, Node source, Node target) {
        if (source == null || target == null) {
            return null;
        }
        
        if (!graph.getNodes().contains(source) || !graph.getNodes().contains(target)) {
            return null;
        }
        
        if (source.equals(target)) {
            return Arrays.asList(source);
        }
        
        // Réinitialiser les propriétés d'algorithme
        graph.resetAlgorithmProperties();
        
        Queue<Node> queue = new LinkedList<>();
        Set<Node> visited = new HashSet<>();
        
        source.setDistance(0.0);
        source.setPrevious(null);
        queue.offer(source);
        visited.add(source);
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            current.setVisited(true);
            
            if (current.equals(target)) {
                return reconstructPath(target);
            }
            
            // Pour BFS non pondéré, on ignore les poids des arêtes
            Map<Node, Double> neighbors = graph.getNeighbors(current);
            
            for (Node neighbor : neighbors.keySet()) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    neighbor.setDistance(current.getDistance() + 1); // Distance = nombre d'arêtes
                    neighbor.setPrevious(current);
                    queue.offer(neighbor);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Reconstruit le chemin depuis le nœud cible vers la source
     */
    private List<Node> reconstructPath(Node target) {
        List<Node> path = new ArrayList<>();
        Node current = target;
        
        while (current != null) {
            path.add(0, current);
            current = current.getPrevious();
        }
        
        return path.isEmpty() ? null : path;
    }
    
    /**
     * Vérifie si le graphe est biparti en utilisant BFS
     * Utile pour certains types d'analyses de graphe
     */
    public boolean isBipartite(Graph graph) {
        if (graph.getNodes().isEmpty()) {
            return true;
        }
        
        Map<Node, Integer> colors = new HashMap<>();
        
        for (Node startNode : graph.getNodes()) {
            if (colors.containsKey(startNode)) {
                continue; // Déjà coloré dans une composante précédente
            }
            
            Queue<Node> queue = new LinkedList<>();
            queue.offer(startNode);
            colors.put(startNode, 0);
            
            while (!queue.isEmpty()) {
                Node current = queue.poll();
                int currentColor = colors.get(current);
                
                Map<Node, Double> neighbors = graph.getNeighbors(current);
                
                for (Node neighbor : neighbors.keySet()) {
                    if (!colors.containsKey(neighbor)) {
                        colors.put(neighbor, 1 - currentColor);
                        queue.offer(neighbor);
                    } else if (colors.get(neighbor) == currentColor) {
                        return false; // Conflit de couleur
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Trouve toutes les composantes connexes du graphe
     */
    public List<Set<Node>> findConnectedComponents(Graph graph) {
        List<Set<Node>> components = new ArrayList<>();
        Set<Node> visited = new HashSet<>();
        
        for (Node startNode : graph.getNodes()) {
            if (visited.contains(startNode)) {
                continue;
            }
            
            Set<Node> component = new HashSet<>();
            Queue<Node> queue = new LinkedList<>();
            
            queue.offer(startNode);
            visited.add(startNode);
            component.add(startNode);
            
            while (!queue.isEmpty()) {
                Node current = queue.poll();
                
                Map<Node, Double> neighbors = graph.getNeighbors(current);
                
                for (Node neighbor : neighbors.keySet()) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        component.add(neighbor);
                        queue.offer(neighbor);
                    }
                }
            }
            
            components.add(component);
        }
        
        return components;
    }
    
    @Override
    public String getAlgorithmName() {
        return "BFS (Breadth-First Search)";
    }
    
    @Override
    public boolean supportsNegativeWeights() {
        return true; // BFS peut gérer les poids négatifs
    }
}
