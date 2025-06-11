package com.modelisation.model.shapes;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Classe représentant un cercle
 * Implémentation concrète de Shape
 */
public class Circle extends Shape implements AbstractCircle {
    private double radius;

    public Circle(double centerX, double centerY, double radius, Color color, double strokeWidth) {
        super(centerX, centerY, color, strokeWidth);
        this.radius = radius;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(color);
        gc.setLineWidth(strokeWidth);
        // Dessiner le cercle (x,y représentent le centre, donc on ajuste pour le coin supérieur gauche)
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    @Override
    public boolean contains(double pointX, double pointY) {
        // Vérifier si le point est dans le cercle en calculant la distance au centre
        double distance = Math.sqrt(Math.pow(pointX - x, 2) + Math.pow(pointY - y, 2));
        return distance <= radius;
    }

    @Override
    public double getArea() {
        return Math.PI * radius * radius;
    }

    @Override
    public double getPerimeter() {
        return 2 * Math.PI * radius;
    }

    // Getters et Setters
    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }

    @Override
    public String toString() {
        return String.format("Circle[centerX=%.2f, centerY=%.2f, radius=%.2f, color=%s]", 
                           x, y, radius, color);
    }
}
