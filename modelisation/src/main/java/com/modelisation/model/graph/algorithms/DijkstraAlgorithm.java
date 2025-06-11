package com.modelisation.model.graph.algorithms;

import com.modelisation.model.graph.Graph;
import com.modelisation.model.graph.Node;

import java.util.*;

/**
 * Implémentation de l'algorithme de Dijkstra pour le plus court chemin
 * Strategy Pattern - Stratégie concrète pour l'algorithme de Dijkstra
 */
public class DijkstraAlgorithm implements ShortestPathStrategy {
    
    @Override
    public List<Node> findShortestPath(Graph graph, Node source, Node target) {
        ShortestPathResult result = findShortestPaths(graph, source);
        return result.getPathTo(target);
    }
    
    @Override
    public ShortestPathResult findShortestPaths(Graph graph, Node source) {
        // Vérifier les préconditions
        if (source == null) {
            return new ShortestPathResult(null, false, "Nœud source null");
        }
        
        if (!graph.getNodes().contains(source)) {
            return new ShortestPathResult(source, false, "Nœud source non trouvé dans le graphe");
        }
        
        // Réinitialiser les propriétés d'algorithme
        graph.resetAlgorithmProperties();
        
        // Initialiser les distances
        source.setDistance(0.0);
        
        // File de priorité pour les nœuds à traiter
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(
            Comparator.comparingDouble(Node::getDistance)
        );
        
        // Ajouter tous les nœuds à la file de priorité
        for (Node node : graph.getNodes()) {
            priorityQueue.offer(node);
        }
        
        // Ensemble des nœuds visités
        Set<Node> visited = new HashSet<>();
        
        while (!priorityQueue.isEmpty()) {
            // Extraire le nœud avec la plus petite distance
            Node current = priorityQueue.poll();
            
            // Si la distance est infinie, tous les nœuds restants sont inaccessibles
            if (current.getDistance() == Double.POSITIVE_INFINITY) {
                break;
            }
            
            // Marquer comme visité
            visited.add(current);
            current.setVisited(true);
            
            // Examiner tous les voisins
            Map<Node, Double> neighbors = graph.getNeighbors(current);
            
            for (Map.Entry<Node, Double> entry : neighbors.entrySet()) {
                Node neighbor = entry.getKey();
                double edgeWeight = entry.getValue();
                
                // Ignorer les nœuds déjà visités
                if (visited.contains(neighbor)) {
                    continue;
                }
                
                // Vérifier les poids négatifs (non supportés par Dijkstra)
                if (edgeWeight < 0) {
                    return new ShortestPathResult(source, false, 
                        "L'algorithme de Dijkstra ne supporte pas les poids négatifs");
                }
                
                // Calculer la nouvelle distance
                double newDistance = current.getDistance() + edgeWeight;
                
                // Si on a trouvé un chemin plus court
                if (newDistance < neighbor.getDistance()) {
                    // Retirer et remettre dans la file de priorité pour mettre à jour l'ordre
                    priorityQueue.remove(neighbor);
                    
                    neighbor.setDistance(newDistance);
                    neighbor.setPrevious(current);
                    
                    priorityQueue.offer(neighbor);
                }
            }
        }
        
        return new ShortestPathResult(source);
    }
    
    @Override
    public String getAlgorithmName() {
        return "Dijkstra";
    }
    
    @Override
    public boolean supportsNegativeWeights() {
        return false;
    }
    
    /**
     * Variante de Dijkstra qui s'arrête dès que le nœud cible est atteint
     * Plus efficace quand on cherche seulement un chemin vers un nœud spécifique
     */
    public List<Node> findShortestPathOptimized(Graph graph, Node source, Node target) {
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
        
        // Initialiser les distances
        source.setDistance(0.0);
        
        // File de priorité pour les nœuds à traiter
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(
            Comparator.comparingDouble(Node::getDistance)
        );
        
        priorityQueue.offer(source);
        Set<Node> visited = new HashSet<>();
        
        while (!priorityQueue.isEmpty()) {
            Node current = priorityQueue.poll();
            
            // Si on a atteint la cible, on peut s'arrêter
            if (current.equals(target)) {
                break;
            }
            
            // Si déjà visité, ignorer
            if (visited.contains(current)) {
                continue;
            }
            
            visited.add(current);
            current.setVisited(true);
            
            // Examiner tous les voisins
            Map<Node, Double> neighbors = graph.getNeighbors(current);
            
            for (Map.Entry<Node, Double> entry : neighbors.entrySet()) {
                Node neighbor = entry.getKey();
                double edgeWeight = entry.getValue();
                
                if (visited.contains(neighbor) || edgeWeight < 0) {
                    continue;
                }
                
                double newDistance = current.getDistance() + edgeWeight;
                
                if (newDistance < neighbor.getDistance()) {
                    neighbor.setDistance(newDistance);
                    neighbor.setPrevious(current);
                    priorityQueue.offer(neighbor);
                }
            }
        }
        
        // Reconstruire le chemin
        if (target.getDistance() == Double.POSITIVE_INFINITY) {
            return null; // Aucun chemin trouvé
        }
        
        List<Node> path = new ArrayList<>();
        Node current = target;
        
        while (current != null) {
            path.add(0, current);
            current = current.getPrevious();
        }
        
        return path;
    }
}
