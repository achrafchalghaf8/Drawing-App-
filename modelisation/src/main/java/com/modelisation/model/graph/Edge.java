package com.modelisation.model.graph;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Objects;

/**
 * Classe représentant une arête dans un graphe
 * Utilisée pour connecter les nœuds dans les algorithmes de graphe
 */
public class Edge {
    private Node source;
    private Node target;
    private double weight;
    private String label;
    private Color color;
    private double strokeWidth;
    private boolean directed;
    private boolean highlighted;
    private boolean deemphasized; // New field for de-emphasized state
    
    public Edge(Node source, Node target) {
        this(source, target, 1.0, false);
    }
    
    public Edge(Node source, Node target, double weight) {
        this(source, target, weight, false);
    }
    
    public Edge(Node source, Node target, double weight, boolean directed) {
        this.source = source;
        this.target = target;
        this.weight = weight;
        this.directed = directed;
        this.color = Color.BLACK;
        this.strokeWidth = 2.0;
        this.highlighted = false;
        this.deemphasized = false; // Initialize deemphasized
        this.label = String.format("%.1f", weight);
    }
    
    /**
     * Dessine l'arête sur le canvas
     * @param gc Contexte graphique
     */
    public void draw(GraphicsContext gc) {
        Color currentDrawColor;
        double currentDrawWidth;

        if (highlighted) {
            currentDrawColor = Color.GREEN; // Shortest path edges are now green
            currentDrawWidth = strokeWidth * 2.0; // Make them thicker
        } else if (deemphasized) {
            currentDrawColor = Color.LIGHTGRAY;
            currentDrawWidth = strokeWidth * 0.5; // Make them thinner
        } else {
            currentDrawColor = color; // Default color (usually BLACK)
            currentDrawWidth = strokeWidth; // Default width
        }
        
        gc.setStroke(currentDrawColor);
        gc.setLineWidth(currentDrawWidth);
        
        // Calculer les points de connexion sur les bords des nœuds
        double[] connectionPoints = calculateConnectionPoints();
        double startX = connectionPoints[0];
        double startY = connectionPoints[1];
        double endX = connectionPoints[2];
        double endY = connectionPoints[3];
        
        // Dessiner la ligne
        gc.strokeLine(startX, startY, endX, endY);
        
        // Dessiner une flèche si l'arête est dirigée
        if (directed) {
            // Arrowhead should match the line color and consider its thickness
            drawArrowHead(gc, startX, startY, endX, endY, currentDrawColor, currentDrawWidth);
        }
        
        // Dessiner le poids au milieu de l'arête
        // Only draw weight if not de-emphasized, or if it's part of the highlighted path
        if ((highlighted || !deemphasized) && weight != 1.0) {
            drawWeight(gc, startX, startY, endX, endY);
        }
    }
    
    /**
     * Calcule les points de connexion sur les bords des nœuds
     * @return [startX, startY, endX, endY]
     */
    private double[] calculateConnectionPoints() {
        double dx = target.getX() - source.getX();
        double dy = target.getY() - source.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance == 0) {
            return new double[]{source.getX(), source.getY(), target.getX(), target.getY()};
        }
        
        // Normaliser le vecteur direction
        double unitX = dx / distance;
        double unitY = dy / distance;
        
        // Calculer les points sur les bords des cercles
        double startX = source.getX() + unitX * source.getRadius();
        double startY = source.getY() + unitY * source.getRadius();
        double endX = target.getX() - unitX * target.getRadius();
        double endY = target.getY() - unitY * target.getRadius();
        
        return new double[]{startX, startY, endX, endY};
    }
    
    /**
     * Dessine une pointe de flèche pour les arêtes dirigées
     */
    private void drawArrowHead(GraphicsContext gc, double startX, double startY, 
                              double endX, double endY, Color color, double width) {
        double arrowLength = 15.0;
        double arrowAngle = Math.PI / 6; // 30 degrés
        
        // Calculer l'angle de la ligne
        double angle = Math.atan2(endY - startY, endX - startX);
        
        // Calculer les points de la pointe de flèche
        double arrowX1 = endX - arrowLength * Math.cos(angle - arrowAngle);
        double arrowY1 = endY - arrowLength * Math.sin(angle - arrowAngle);
        double arrowX2 = endX - arrowLength * Math.cos(angle + arrowAngle);
        double arrowY2 = endY - arrowLength * Math.sin(angle + arrowAngle);
        
        // Dessiner la pointe de flèche
        gc.setStroke(color);
        gc.setLineWidth(width);
        gc.strokeLine(endX, endY, arrowX1, arrowY1);
        gc.strokeLine(endX, endY, arrowX2, arrowY2);
    }
    
    /**
     * Dessine le poids de l'arête
     */
    private void drawWeight(GraphicsContext gc, double startX, double startY, 
                           double endX, double endY) {
        // Position au milieu de l'arête
        double midX = (startX + endX) / 2;
        double midY = (startY + endY) / 2;
        
        // Fond blanc pour le texte
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.0);
        
        double textWidth = 30;
        double textHeight = 15;
        gc.fillRect(midX - textWidth/2, midY - textHeight/2, textWidth, textHeight);
        gc.strokeRect(midX - textWidth/2, midY - textHeight/2, textWidth, textHeight);
        
        // Texte du poids
        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font(10));
        gc.fillText(label, midX - 10, midY + 3);
    }
    
    /**
     * Vérifie si un point est proche de l'arête
     * @param pointX coordonnée X du point
     * @param pointY coordonnée Y du point
     * @return true si le point est proche de l'arête
     */
    public boolean contains(double pointX, double pointY) {
        double tolerance = 5.0;
        
        double[] connectionPoints = calculateConnectionPoints();
        double startX = connectionPoints[0];
        double startY = connectionPoints[1];
        double endX = connectionPoints[2];
        double endY = connectionPoints[3];
        
        // Calculer la distance du point à la ligne
        double A = endY - startY;
        double B = startX - endX;
        double C = endX * startY - startX * endY;
        
        double distance = Math.abs(A * pointX + B * pointY + C) / Math.sqrt(A * A + B * B);
        
        // Vérifier que le point est dans le segment
        double minX = Math.min(startX, endX);
        double maxX = Math.max(startX, endX);
        double minY = Math.min(startY, endY);
        double maxY = Math.max(startY, endY);
        
        return distance <= tolerance && 
               pointX >= minX - tolerance && pointX <= maxX + tolerance &&
               pointY >= minY - tolerance && pointY <= maxY + tolerance;
    }
    
    /**
     * Obtient l'autre nœud de l'arête
     * @param node Un des nœuds de l'arête
     * @return L'autre nœud, ou null si le nœud donné n'appartient pas à cette arête
     */
    public Node getOtherNode(Node node) {
        if (node.equals(source)) {
            return target;
        } else if (node.equals(target)) {
            return source;
        }
        return null;
    }
    
    // Getters et Setters
    public Node getSource() { return source; }
    public void setSource(Node source) { this.source = source; }
    
    public Node getTarget() { return target; }
    public void setTarget(Node target) { this.target = target; }
    
    public double getWeight() { return weight; }
    public void setWeight(double weight) { 
        this.weight = weight; 
        this.label = String.format("%.1f", weight);
    }
    
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    
    public double getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(double strokeWidth) { this.strokeWidth = strokeWidth; }
    
    public boolean isDirected() { return directed; }
    public void setDirected(boolean directed) { this.directed = directed; }
    
    public boolean isHighlighted() { return highlighted; }
    public void setHighlighted(boolean highlighted) { this.highlighted = highlighted; }

    public boolean isDeemphasized() { return deemphasized; }
    public void setDeemphasized(boolean deemphasized) { this.deemphasized = deemphasized; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Edge edge = (Edge) obj;
        return Objects.equals(source, edge.source) && Objects.equals(target, edge.target);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(source, target);
    }
    
    @Override
    public String toString() {
        String direction = directed ? " -> " : " -- ";
        return String.format("Edge[%s%s%s, weight=%.2f]", 
                           source.getId(), direction, target.getId(), weight);
    }
}
