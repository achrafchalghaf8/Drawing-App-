package com.modelisation.model.graph.algorithms;

import com.modelisation.model.graph.Graph;
import com.modelisation.model.graph.Node;

import java.util.List;

/**
 * Interface Strategy pour les algorithmes de plus court chemin
 * Permet de changer dynamiquement l'algorithme utilisé
 */
public interface ShortestPathStrategy {
    
    /**
     * Calcule le plus court chemin entre deux nœuds
     * @param graph Le graphe
     * @param source Nœud source
     * @param target Nœud cible
     * @return Le chemin le plus court sous forme de liste de nœuds, ou null si aucun chemin n'existe
     */
    List<Node> findShortestPath(Graph graph, Node source, Node target);
    
    /**
     * Calcule les plus courts chemins depuis un nœud source vers tous les autres nœuds
     * @param graph Le graphe
     * @param source Nœud source
     * @return Résultat contenant les distances et les chemins
     */
    ShortestPathResult findShortestPaths(Graph graph, Node source);
    
    /**
     * Obtient le nom de l'algorithme
     * @return Nom de l'algorithme
     */
    String getAlgorithmName();
    
    /**
     * Vérifie si l'algorithme supporte les poids négatifs
     * @return true si les poids négatifs sont supportés
     */
    boolean supportsNegativeWeights();
    
    /**
     * Classe pour encapsuler les résultats d'un algorithme de plus court chemin
     */
    class ShortestPathResult {
        private final Node source;
        private final boolean successful;
        private final String errorMessage;
        
        public ShortestPathResult(Node source, boolean successful, String errorMessage) {
            this.source = source;
            this.successful = successful;
            this.errorMessage = errorMessage;
        }
        
        public ShortestPathResult(Node source) {
            this(source, true, null);
        }
        
        /**
         * Obtient le chemin vers un nœud cible
         * @param target Nœud cible
         * @return Liste des nœuds formant le chemin, ou null si aucun chemin n'existe
         */
        public List<Node> getPathTo(Node target) {
            if (!successful || target.getDistance() == Double.POSITIVE_INFINITY) {
                return null;
            }
            
            List<Node> path = new java.util.ArrayList<>();
            Node current = target;
            
            while (current != null) {
                path.add(0, current);
                current = current.getPrevious();
            }
            
            return path.isEmpty() ? null : path;
        }
        
        /**
         * Obtient la distance vers un nœud cible
         * @param target Nœud cible
         * @return Distance vers le nœud cible, ou Double.POSITIVE_INFINITY si inaccessible
         */
        public double getDistanceTo(Node target) {
            return target.getDistance();
        }
        
        /**
         * Vérifie si un nœud est accessible depuis la source
         * @param target Nœud cible
         * @return true si le nœud est accessible
         */
        public boolean isReachable(Node target) {
            return target.getDistance() != Double.POSITIVE_INFINITY;
        }
        
        // Getters
        public Node getSource() { return source; }
        public boolean isSuccessful() { return successful; }
        public String getErrorMessage() { return errorMessage; }
    }
}
