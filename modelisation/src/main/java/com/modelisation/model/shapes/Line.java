package com.modelisation.model.shapes;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Classe représentant une ligne
 * Implémentation concrète de Shape
 */
public class Line extends Shape {
    private double endX;
    private double endY;

    public Line(double startX, double startY, double endX, double endY, Color color, double strokeWidth) {
        super(startX, startY, color, strokeWidth);
        this.endX = endX;
        this.endY = endY;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(color);
        gc.setLineWidth(strokeWidth);
        gc.strokeLine(x, y, endX, endY);
    }

    @Override
    public boolean contains(double pointX, double pointY) {
        // Vérifier si le point est proche de la ligne (tolérance de 5 pixels)
        double tolerance = 5.0;
        
        // Calculer la distance du point à la ligne
        double A = endY - y;
        double B = x - endX;
        double C = endX * y - x * endY;
        
        double distance = Math.abs(A * pointX + B * pointY + C) / Math.sqrt(A * A + B * B);
        
        // Vérifier aussi que le point est dans le segment (pas juste sur la ligne infinie)
        double minX = Math.min(x, endX);
        double maxX = Math.max(x, endX);
        double minY = Math.min(y, endY);
        double maxY = Math.max(y, endY);
        
        return distance <= tolerance && 
               pointX >= minX - tolerance && pointX <= maxX + tolerance &&
               pointY >= minY - tolerance && pointY <= maxY + tolerance;
    }

    @Override
    public double getArea() {
        return 0; // Une ligne n'a pas d'aire
    }

    @Override
    public double getPerimeter() {
        return getLength(); // Pour une ligne, le "périmètre" est sa longueur
    }

    /**
     * Calcule la longueur de la ligne
     * @return la longueur de la ligne
     */
    public double getLength() {
        return Math.sqrt(Math.pow(endX - x, 2) + Math.pow(endY - y, 2));
    }

    // Getters et Setters
    public double getEndX() { return endX; }
    public void setEndX(double endX) { this.endX = endX; }

    public double getEndY() { return endY; }
    public void setEndY(double endY) { this.endY = endY; }

    @Override
    public String toString() {
        return String.format("Line[start=(%.2f,%.2f), end=(%.2f,%.2f), color=%s]", 
                           x, y, endX, endY, color);
    }
}
