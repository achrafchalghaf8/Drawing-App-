package com.modelisation.model.graph.algorithms;

import com.modelisation.model.graph.Graph;
import com.modelisation.model.graph.Node;

import java.util.*;

/**
 * Implémentation de ShortestPathStrategy utilisant l'algorithme de Dijkstra.
 */
public class DijkstraStrategy implements ShortestPathStrategy {

    @Override
    public String getAlgorithmName() {
        return "Dijkstra's Algorithm";
    }

    @Override
    public boolean supportsNegativeWeights() {
        return false; // Dijkstra's algorithm does not support negative weight edges.
    }

    @Override
    public List<Node> findShortestPath(Graph graph, Node sourceNode, Node targetNode) {
        System.out.println(String.format("Dijkstra.findShortestPath: Received Graph Hash: %s, SourceNode ID: %s, Hash: %s, TargetNode ID: %s, Hash: %s", System.identityHashCode(graph), sourceNode != null ? sourceNode.getId() : "null", System.identityHashCode(sourceNode), targetNode != null ? targetNode.getId() : "null", System.identityHashCode(targetNode)));
        if (graph == null || sourceNode == null || targetNode == null) {
            // Consider logging this event if a logger is available
            return Collections.emptyList(); // Or throw an IllegalArgumentException
        }

        // Use the findShortestPaths method to get all paths from the source
        ShortestPathResult result = findShortestPaths(graph, sourceNode);

        // If the calculation was successful, get the specific path to the target
        if (result.isSuccessful()) {
            // Find the actual target node instance within the graph's nodes
            Node actualTargetNode = null;
            for (Node node : graph.getNodes()) {
                System.out.println(String.format("Dijkstra.findShortestPath: Checking graph node ID: %s, Hash: %s against targetNode ID: %s, Hash: %s", node.getId(), System.identityHashCode(node), targetNode.getId(), System.identityHashCode(targetNode)));
                if (node.getId().equals(targetNode.getId())) {
                    actualTargetNode = node;
                    break;
                }
            }

            System.out.println(String.format("Dijkstra.findShortestPath: ActualTargetNode found: ID: %s, Hash: %s", (actualTargetNode != null ? actualTargetNode.getId() : "null"), System.identityHashCode(actualTargetNode)));
            if (actualTargetNode == null) {
                System.err.println("DijkstraStrategy.findShortestPath: Target node (ID: " + targetNode.getId() + ") not found in graph after running algorithm.");
                return Collections.emptyList();
            }

            List<Node> path = result.getPathTo(actualTargetNode);
            // getPathTo can return null if the target is unreachable
            return path != null ? path : Collections.emptyList();
        } else {
            // Handle the case where findShortestPaths failed
            // A logger would be appropriate here to log result.getErrorMessage()
            System.err.println("DijkstraStrategy.findShortestPath: findShortestPaths was not successful. Error: " + (result.getErrorMessage() != null ? result.getErrorMessage() : "Unknown error"));
            return Collections.emptyList();
        }
    }

    @Override
    public ShortestPathResult findShortestPaths(Graph graph, Node sourceParameter) {
        System.out.println(String.format("Dijkstra.findShortestPaths: Received Graph Hash: %s, SourceParameter ID: %s, Hash: %s", System.identityHashCode(graph), sourceParameter != null ? sourceParameter.getId() : "null", System.identityHashCode(sourceParameter)));
        if (graph == null || sourceParameter == null) {
            return new ShortestPathResult(null, false, "Graph or source parameter node cannot be null.");
        }

        Node actualSourceNode = null;
        // Initialize all nodes within the graph and find the actual source node instance
        for (Node node : graph.getNodes()) {
            System.out.println(String.format("Dijkstra.findShortestPaths: Iterating graph node ID: %s, Hash: %s. Comparing with SourceParameter ID: %s, Hash: %s", node.getId(), System.identityHashCode(node), sourceParameter.getId(), System.identityHashCode(sourceParameter)));
            node.resetAlgorithmProperties(); // Resets distance to INF, previous to null, visited/selected to false
            if (node.getId().equals(sourceParameter.getId())) {
                actualSourceNode = node;
            }
        }

        System.out.println(String.format("Dijkstra.findShortestPaths: ActualSourceNode found: ID: %s, Hash: %s", (actualSourceNode != null ? actualSourceNode.getId() : "null"), System.identityHashCode(actualSourceNode)));
        if (actualSourceNode == null) {
            System.err.println("Dijkstra: Source node with ID " + sourceParameter.getId() + " not found within the provided graph's nodes.");
            return new ShortestPathResult(null, false, "Source node (ID: " + sourceParameter.getId() + ") not found in graph.");
        }

        actualSourceNode.setDistance(0.0);
        System.out.println("Dijkstra: Actual source node found in graph: " + actualSourceNode.getLabel() + " set to distance 0.0");

        // Priority queue to store nodes to visit, ordered by distance
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(Node::getDistance));
        priorityQueue.add(actualSourceNode); // Add the graph's instance of the source node

        Set<Node> settledNodes = new HashSet<>();
        // Corrected logging to use actualSourceNode
        System.out.println("Dijkstra: Initializing. Source: " + actualSourceNode.getLabel() + " has distance " + actualSourceNode.getDistance());
        
        // Use a final variable for the lambda expression
        final Node finalActualSourceNode = actualSourceNode;
        graph.getNodes().forEach(n -> {
            // Log initial distances for all nodes, including the source, after reset and source re-initialization
            System.out.println("Dijkstra: Node " + n.getLabel() + " initial distance: " + n.getDistance() + (n.equals(finalActualSourceNode) ? " (Source Node)" : ""));
        });

        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.poll();
            System.out.println("Dijkstra: Polled " + currentNode.getLabel() + " with distance " + currentNode.getDistance());

            if (settledNodes.contains(currentNode)) {
                System.out.println("Dijkstra: Node " + currentNode.getLabel() + " already settled. Skipping.");
                continue;
            }
            settledNodes.add(currentNode);
            System.out.println("Dijkstra: Settled " + currentNode.getLabel());

            // If current node's distance is infinity, remaining nodes are unreachable
            if (currentNode.getDistance() == Double.POSITIVE_INFINITY) {
                System.out.println("Dijkstra: Current node " + currentNode.getLabel() + " is at infinity. Breaking.");
                break; 
            }

            // Explore neighbors using the graph's adjacency list representation
            System.out.println("Dijkstra: Exploring neighbors of " + currentNode.getLabel() + " using graph.getNeighbors()");
            Map<Node, Double> neighbors = graph.getNeighbors(currentNode);

            for (Map.Entry<Node, Double> entry : neighbors.entrySet()) {
                Node neighbor = entry.getKey();
                double edgeWeight = entry.getValue();

                if (settledNodes.contains(neighbor)) {
                    continue; // Skip already settled neighbors
                }

                double newDist = currentNode.getDistance() + edgeWeight;
                System.out.println("Dijkstra: Considering neighbor " + neighbor.getLabel() + " of " + currentNode.getLabel() + ". Edge weight: " + edgeWeight + ". Current neighbor dist: " + neighbor.getDistance() + ", newDist via " + currentNode.getLabel() + ": " + newDist);

                if (newDist < neighbor.getDistance()) {
                    System.out.println("Dijkstra: Updating distance for " + neighbor.getLabel() + " from " + neighbor.getDistance() + " to " + newDist + ". Previous: " + currentNode.getLabel());
                    neighbor.setDistance(newDist);
                    neighbor.setPrevious(currentNode);
                    
                    // Re-add to queue with updated distance. 
                    // remove() is important for PriorityQueue to re-evaluate the position based on the new distance.
                    boolean removed = priorityQueue.remove(neighbor);
                    priorityQueue.add(neighbor);
                    System.out.println("Dijkstra: Neighbor " + neighbor.getLabel() + (removed ? " updated in PQ." : " added to PQ (was not present or remove failed)."));
                }
            }
        }
        System.out.println("Dijkstra: Algorithm finished. Final distances:");
        for (Node node : graph.getNodes()) {
            System.out.println("Dijkstra: Node " + node.getLabel() + ", Distance: " + node.getDistance() + ", Previous: " + (node.getPrevious() != null ? node.getPrevious().getLabel() : "null"));
        }
        // The distances and previous nodes are now set on each Node object in the graph.
        // The ShortestPathResult class uses these directly.
        return new ShortestPathResult(actualSourceNode); // Return result associated with the graph's source node instance
    }
}
