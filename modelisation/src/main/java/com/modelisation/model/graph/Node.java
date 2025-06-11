package com.modelisation.model.graph;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Objects;

/**
 * Classe représentant un nœud dans un graphe
 * Utilisée pour les algorithmes de plus court chemin
 */
public class Node {
    private String id;
    private double x, y;
    private String label;
    private Color color;
    private double radius;
    private boolean selected;
    private boolean visited;
    
    // Propriétés pour les algorithmes de plus court chemin
    private double distance;
    private Node previous;
    
    public Node(String id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.label = id;
        this.color = Color.LIGHTBLUE;
        this.radius = 20.0;
        this.selected = false;
        this.visited = false;
        this.distance = Double.POSITIVE_INFINITY;
        this.previous = null;
    }
    
    public Node(String id, double x, double y, String label) {
        this(id, x, y);
        this.label = label;
    }
    
    /**
     * Dessine le nœud sur le canvas
     * @param gc Contexte graphique
     */
    public void draw(GraphicsContext gc) {
        // Couleur selon l'état
        Color fillColor = color;
        if (selected) {
            fillColor = Color.ORANGE;
        } else if (visited) {
            fillColor = Color.LIGHTGREEN;
        }
        
        // Dessiner le cercle
        gc.setFill(fillColor);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        
        // Contour
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2.0);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
        
        // Label
        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font(12));
        
        // Centrer le texte
        javafx.scene.text.Text text = new javafx.scene.text.Text(label);
        text.setFont(gc.getFont());
        double textWidth = text.getBoundsInLocal().getWidth();
        double textHeight = text.getBoundsInLocal().getHeight();
        
        gc.fillText(label, x - textWidth / 2, y + textHeight / 4);
    }
    
    /**
     * Vérifie si un point est dans le nœud
     * @param pointX coordonnée X du point
     * @param pointY coordonnée Y du point
     * @return true si le point est dans le nœud
     */
    public boolean contains(double pointX, double pointY) {
        double distance = Math.sqrt(Math.pow(pointX - x, 2) + Math.pow(pointY - y, 2));
        return distance <= radius;
    }
    
    /**
     * Calcule la distance euclidienne vers un autre nœud
     * @param other L'autre nœud
     * @return La distance euclidienne
     */
    public double distanceTo(Node other) {
        return Math.sqrt(Math.pow(other.x - this.x, 2) + Math.pow(other.y - this.y, 2));
    }
    
    /**
     * Réinitialise les propriétés d'algorithme
     */
    public void resetAlgorithmProperties() {
        this.distance = Double.POSITIVE_INFINITY;
        this.previous = null;
        this.visited = false;
        this.selected = false;
    }
    
    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    
    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }
    
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
    
    public boolean isVisited() { return visited; }
    public void setVisited(boolean visited) { this.visited = visited; }
    
    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
    
    public Node getPrevious() { return previous; }
    public void setPrevious(Node previous) { this.previous = previous; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Node node = (Node) obj;
        return Objects.equals(id, node.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Node[id='%s', position=(%.2f,%.2f), label='%s']", 
                           id, x, y, label);
    }
}
